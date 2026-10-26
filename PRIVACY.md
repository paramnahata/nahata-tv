# Privacy

Nahata TV stores Home app selections, welcome text and appearance preferences locally on the TV. It reads launchable app names and icons to display the home screen. The Android app has no Internet permission and sends no analytics. Opening the website hands a link to another app, whose privacy policy applies. Android backups are disabled for this build.

The optional Home override Accessibility service listens only for stock-launcher window events. It cannot retrieve window content and does not read screen text, record keystrokes, capture screenshots or transmit events. Original launcher mode pauses routing until Nahata TV is opened again. It can be disabled in Android Accessibility settings.

The separate laptop remote bridge serves controls over trusted local Wi-Fi. Paired session tokens are held in laptop memory and phone session storage. Pairing codes and typed text are not logged or sent to a cloud service. HTTP traffic is not encrypted; use only your trusted local Wi-Fi and do not expose the bridge publicly. See remote/README.md for limitations and session revocation.
