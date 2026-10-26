"""Safety checks for local pairing boundaries. No TV connection required."""
import collections
import json
import threading
import unittest
import urllib.error
import urllib.request
from server import Handler, Bridge
from http.server import ThreadingHTTPServer

class RemoteTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.server=ThreadingHTTPServer(('127.0.0.1',0),Handler)
        cls.server.allowed_host=f'127.0.0.1:{cls.server.server_port}'
        cls.server.pin='123456';cls.server.sessions={};cls.server.state_lock=threading.Lock()
        cls.server.attempts=collections.defaultdict(collections.deque);cls.server.failures=collections.defaultdict(collections.deque)
        cls.thread=threading.Thread(target=cls.server.serve_forever,daemon=True);cls.thread.start()
    @classmethod
    def tearDownClass(cls):cls.server.shutdown();cls.server.server_close();cls.thread.join()
    def post(self,path,data,token='',**headers):
        request=urllib.request.Request('http://'+self.server.allowed_host+path,data=json.dumps(data).encode(),headers={'Content-Type':'application/json','X-Nahata-Client':'1',**({'Authorization':'Bearer '+token} if token else {}),**headers})
        try:
            with urllib.request.urlopen(request) as response:return response.status,json.load(response)
        except urllib.error.HTTPError as e:return e.code,json.load(e)
    def test_unauthenticated_controls_are_rejected(self):self.assertEqual(self.post('/command',{'action':'home'})[0],401)
    def test_foreign_origin_is_rejected(self):self.assertEqual(self.post('/pair',{'code':'123456'},Origin='https://untrusted.example')[0],403)
    def test_wrong_host_is_rejected(self):self.assertEqual(self.post('/pair',{'code':'123456'},Host='untrusted.example')[0],403)
    def test_pair_and_revoke(self):
        status,data=self.post('/pair',{'code':'123456'});self.assertEqual(status,200)
        token=data['token'];self.assertGreater(len(token),30)
        self.assertEqual(self.post('/unpair',{},token)[0],200)
        self.assertEqual(self.post('/command',{'action':'home'},token)[0],401)
    def test_bad_pairing_code(self):self.assertEqual(self.post('/pair',{'code':'999999'})[0],401)
    def test_text_is_shell_quoted(self):
        bridge=Bridge('unused','unused');calls=[];bridge.run=lambda *args:calls.append(args)
        bridge.command({'action':'text','text':"hello'; reboot; echo '"})
        import shlex
        parts=shlex.split(calls[0][1]);self.assertEqual(parts[:2],['input','text']);self.assertEqual(len(parts),3)
        self.assertIn('reboot',parts[2])  # Remains literal text, never a shell command.
    def test_unicode_is_explicitly_rejected(self):
        with self.assertRaises(ValueError):Bridge('unused','unused').command({'action':'text','text':'नाहटा'})

if __name__=='__main__':unittest.main()
