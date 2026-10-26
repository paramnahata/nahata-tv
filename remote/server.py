"""Local, paired Nahata Remote bridge. No third-party Python dependencies."""
import argparse
import collections
import hmac
import ipaddress
import json
import pathlib
import secrets
import shlex
import subprocess
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

ROOT = pathlib.Path(__file__).resolve().parent
KEYS = {'up':19,'down':20,'left':21,'right':22,'ok':23,'back':4,'home':3,
        'volume_up':24,'volume_down':25,'mute':164,'play':85,'delete':67,'enter':66}
APPS = {'netflix':'com.netflix.ninja','prime':'com.amazon.amazonvideo.livingroom',
        'youtube':'com.google.android.youtube.tv','live':'com.mediatek.wwtv.tvcenter'}

class Bridge:
    def __init__(self, adb, tv):
        self.adb, self.tv = adb, tv
        self.lock = threading.Lock()

    def run(self, *command):
        with self.lock:
            proc = subprocess.run([self.adb, '-s', self.tv, *command], capture_output=True,
                                  timeout=8, text=True, creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
        if proc.returncode:
            raise RuntimeError('TV is offline or ADB authorization is required.')
        return proc.stdout.strip()

    def command(self, data):
        action = data.get('action')
        if action in KEYS:
            self.run('shell','input','keyevent',str(KEYS[action]))
        elif action == 'jio':
            uri='content://android.media.tv/passthrough/com.hisense.tv.hitvinput%2F.hdmi.HdmiTvInputService%2FHW5'
            self.run('shell', 'am','start','-a','android.intent.action.VIEW','-d',shlex.quote(uri))
        elif action == 'app' and data.get('app') in APPS:
            self.run('shell','monkey','-p',APPS[data['app']],'-c','android.intent.category.LEANBACK_LAUNCHER','1')
        elif action == 'text':
            value=data.get('text','')
            if not isinstance(value,str) or not value or len(value)>500 or any(ord(c)<32 or ord(c)>126 for c in value):
                raise ValueError('Use 1–500 English letters, numbers or symbols. Unicode typing is not supported yet.')
            self.run('shell', 'input text '+shlex.quote(value.replace(' ','%s')))
        else:
            raise ValueError('Unsupported control.')

class Handler(BaseHTTPRequestHandler):
    server_version='NahataRemote'
    def log_message(self, *args):
        pass  # Never log pairing codes, sessions or typed text.
    def reply(self, status, body, kind='application/json'):
        raw=json.dumps(body).encode() if kind=='application/json' else body
        self.send_response(status)
        self.send_header('Content-Type',kind)
        self.send_header('Content-Length',str(len(raw)))
        self.send_header('Cache-Control','no-store')
        self.send_header('X-Content-Type-Options','nosniff')
        self.send_header('Referrer-Policy','no-referrer')
        self.send_header('X-Frame-Options','DENY')
        self.send_header('Content-Security-Policy',"default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'")
        self.end_headers()
        self.wfile.write(raw)
    def valid_host(self):
        return self.headers.get('Host')==self.server.allowed_host
    def do_GET(self):
        if not self.valid_host(): return self.reply(403,{'error':'Invalid host'})
        assets={'/':('index.html','text/html; charset=utf-8'),'/app.js':('app.js','text/javascript; charset=utf-8'),'/style.css':('style.css','text/css; charset=utf-8'),'/icon.svg':('icon.svg','image/svg+xml')}
        if self.path not in assets:return self.reply(404,{'error':'Not found'})
        path,kind=assets[self.path]
        self.reply(200,(ROOT/path).read_bytes(),kind)
    def do_POST(self):
        if not self.valid_host() or self.headers.get('X-Nahata-Client')!='1' or self.headers.get('Content-Type')!='application/json':
            return self.reply(403,{'error':'Request rejected'})
        origin=self.headers.get('Origin')
        if origin and origin!='http://'+self.server.allowed_host:
            return self.reply(403,{'error':'Wrong origin'})
        try:
            size=int(self.headers.get('Content-Length','0'))
            if not 0<size<=4096:raise ValueError('Invalid request size')
            data=json.loads(self.rfile.read(size))
            if not isinstance(data,dict):raise ValueError('Invalid request')
            now=time.monotonic();ip=self.client_address[0]
            with self.server.state_lock:
                attempts=self.server.attempts[ip]
                while attempts and now-attempts[0]>60:attempts.popleft()
                if len(attempts)>=90:return self.reply(429,{'error':'Please slow down'})
                attempts.append(now)
                if self.path=='/pair':
                    failures=self.server.failures[ip]
                    while failures and now-failures[0]>300:failures.popleft()
                    if len(failures)>=5:return self.reply(429,{'error':'Too many incorrect codes. Try again in five minutes.'})
                    if not hmac.compare_digest(str(data.get('code','')),self.server.pin):
                        failures.append(now);return self.reply(401,{'error':'Incorrect pairing code'})
                    if len(self.server.sessions)>=100:self.server.sessions.clear()
                    token=secrets.token_urlsafe(32);self.server.sessions[token]=now
                    return self.reply(200,{'token':token})
                token=self.headers.get('Authorization','').removeprefix('Bearer ')
                since=self.server.sessions.get(token)
                if since is None or now-since>43200:return self.reply(401,{'error':'Pair this phone again'})
            if self.path=='/status':
                state=self.server.bridge.run('get-state')
                return self.reply(200,{'connected':state=='device','name':'Nahata TV'})
            if self.path=='/command':
                self.server.bridge.command(data);return self.reply(200,{'ok':True})
            if self.path=='/unpair':
                with self.server.state_lock:self.server.sessions.pop(token,None)
                return self.reply(200,{'ok':True})
            return self.reply(404,{'error':'Not found'})
        except (ValueError,TypeError,json.JSONDecodeError):self.reply(400,{'error':'Invalid request or unsupported text. Use English text, up to 500 characters.'})
        except (RuntimeError,subprocess.TimeoutExpired):self.reply(503,{'error':'TV disconnected. Check its Wi-Fi and ADB connection, then reconnect.'})
        except (ConnectionError,BrokenPipeError):pass

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--adb',required=True);p.add_argument('--tv',required=True)
    p.add_argument('--host',required=True,help='Laptop private Wi-Fi IPv4 address')
    p.add_argument('--port',type=int,default=8765)
    p.add_argument('--pairing-file',type=pathlib.Path,help='Optional private local output file; never publish it')
    args=p.parse_args()
    ip=ipaddress.ip_address(args.host)
    if not ip.is_private or ip.is_unspecified: p.error('Bind to a specific private local address, never all interfaces.')
    server=ThreadingHTTPServer((args.host,args.port),Handler)
    server.daemon_threads=True;server.allowed_host=f'{args.host}:{args.port}'
    server.bridge=Bridge(args.adb,args.tv);server.pin=f'{secrets.randbelow(1000000):06d}'
    server.sessions={};server.attempts=collections.defaultdict(collections.deque);server.failures=collections.defaultdict(collections.deque);server.state_lock=threading.Lock()
    details={'url':'http://'+server.allowed_host,'code':server.pin}
    if args.pairing_file:args.pairing_file.write_text(json.dumps(details),encoding='utf-8')
    print('Open '+details['url']+' on your phone. Pairing code: '+server.pin,flush=True)
    print('Local trusted Wi-Fi only. Keep the laptop awake. Ctrl+C stops and revokes all sessions.',flush=True)
    try:server.serve_forever()
    except KeyboardInterrupt:pass
    finally:server.server_close()

if __name__=='__main__':main()
