# Phone remote without an always-on laptop

## Recommended now: Google TV mobile app

Google documents its phone remote for compatible Google TV and Android TV OS devices, on Android and iPhone. Install Google TV, put the phone and TV on the same Wi-Fi, choose Remote / Connect TV, select the TV and enter its on-screen code. No laptop bridge is part of this path. Actual pairing and feature availability must be verified on the target TV.

Official reference: [Google TV app features and remote](https://blog.google/products-and-platforms/platforms/google-tv/seven-app-tips/).

## Recommended custom direction: a native phone client

A future original Nahata Remote should connect directly from the phone to the TV's Android TV Remote service, using authenticated local pairing. The researched Remote v2 approach uses TLS pairing and control connections; native Android/iOS networking can implement this without a web-hosting server. This is an architecture recommendation, **not a shipped app**.

Planned scope: device discovery or manual private address, TV code pairing, reconnect/status, D-pad, Home/Back, media and volume where supported, then text input after real-device verification. Store client keys in platform-secure storage. Android and iOS builds need their respective tooling and hardware testing; a launcher APK is not an iPhone app. A gyroscope cursor is a separate feature, not equivalent to focus navigation.

Protocol reference for research: [tronikos/androidtvremote2](https://github.com/tronikos/androidtvremote2), a separately licensed Python implementation. No source from that project is included here. Any future code reuse must preserve its applicable license and notices.

## Why a Hostinger subfolder does not replace the bridge

Static HTML/CSS/JavaScript and React can host the remote interface, but a browser page cannot directly speak the TV service's raw TLS socket protocol. PHP on a public hosting server also has no automatic route into a customer's private home Wi-Fi. Browser access to local devices additionally varies with local-network permissions, secure-context rules and the local endpoint's protocol support.

A TV-hosted HTTP/WebSocket companion could support a browser remote, but it would need its own authenticated server and correctly permissioned TV controls; it is not included here. Moving only the existing web files to Hostinger does not create that companion.

Hostinger remains useful for a landing/download page, screenshots, documentation and future update metadata. It is not required for a native same-Wi-Fi remote. Do not expose ADB, create a public unauthenticated control endpoint, or bypass browser security checks to make a hosted page appear to work.

Browser reference: [MDN local network access](https://developer.mozilla.org/en-US/docs/Web/Security/Defenses/Local_network_access).

## Included optional prototype

The `remote/` folder is an authenticated **laptop bridge** and mobile browser UI. It requires the laptop awake, Python and an authorised ADB connection. It is useful for development but does not meet a laptop-free requirement. Its limitations, trusted-Wi-Fi use and English-only typing are described in [remote/README.md](../remote/README.md).

Finally, TV remote controls target the Android TV itself. They do not guarantee navigation of an external Jio set-top box over HDMI; HDMI-CEC and set-top-box support are separate, firmware-dependent capabilities.
