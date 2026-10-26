# Nahata Remote

An original mobile browser remote for Android and iPhone. Local Python bridge controls the TV using an already-authorized ADB connection. No third-party Python dependencies, cloud account or analytics. The laptop must stay awake and connected. This is a browser remote, not a standalone phone app or an offline-installable PWA.

## Start

Use Python 3.10+ and Android Platform Tools. Replace the example addresses and ADB path with your own:

```text
python server.py --adb /path/to/adb --tv 192.168.1.50:5555 --host 192.168.1.20
```

The bridge prints a phone URL and six-digit pairing code. Open that URL on the same trusted Wi-Fi and enter the code. Sessions are stored in browser session storage, expire after 12 hours and are revoked when the bridge stops. Disconnect revokes the current phone. Restart rotates the pairing code and revokes all sessions. Five incorrect codes block attempts from that address for five minutes.

HTTP is limited to trusted local Wi-Fi: traffic is not encrypted. Never forward this port or ADB to the Internet. For untrusted/shared networks, use the official paired Google TV remote instead. This bridge binds a specific private address, rejects foreign Host/Origin headers and requires custom JSON headers and session authentication for all controls. No CORS is enabled. Pairing codes and typed text are not logged.

## Controls

- D-pad, press-and-hold directions, OK, Back, Home, media play/pause, volume/mute.
- Swipe pad moves Android focus; it is not a free-moving pointer or gyroscope mouse.
- Keyboard sends English text/symbols to a focused text field; Unicode/emoji typing is not yet supported. Paste manually in the text area; no automatic clipboard access.
- Netflix, Prime, YouTube and Live TV shortcuts; Jio HDMI 2 is verified on the development Hisense firmware and requires adaptation on other TVs.
- Status polls while visible and refreshes when the phone returns to the page. If ADB disconnects, reconnect the TV on the laptop and tap the status indicator.

Mobile Safari/Chrome interaction needs device testing; desktop browser and HTTP checks are listed in the release validation notes. Actual latency depends on TV firmware, Wi-Fi and ADB.
