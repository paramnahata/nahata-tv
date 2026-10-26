# Roadmap — planned, not implemented

Implemented since the first preview: local laptop browser remote, static original scenic wallpaper, dark/light themes, themed dialogs, selective Home apps and verified HDMI 2 switching on the development TV. Remaining items below need device-specific validation or future implementation.

## Launcher polish

- Original adaptive icon, Android TV banner and shareable screenshots.
- Local wallpaper picker, contrast controls, font scaling and reduced-motion preference.
- Favourite reordering, user-defined categories and configuration import/export with validation.
- Hindi and English resources, accessibility review and RTL checks.
- Documented device compatibility matrix and automated build/lint pipeline.
- Verified per-input shortcuts using public TV APIs where firmware permits.

## Phone control

- First validate existing Google TV remote on Android and iOS.
- Evaluate a TV-hosted companion to remove the existing laptop bridge dependency, with authenticated pairing, explicit access revocation and no public ADB exposure.
- Unicode typing and independent phone-hardware checks remain pending; the current bridge supports English typing and swipe-to-focus navigation.
- Bluetooth HID is an optional Android-phone path; iOS parity and gyroscope pointer are not promised.

## Performance and advertising

- Measure startup, focus and scrolling on actual low-memory Android 9 hardware.
- Nahata TV's own screen has no promotional recommendations. It cannot remove advertisements inside Jio, YouTube or streaming apps.
- No blind debloating, root, firmware modifications or network filtering included.

## Release checklist

Build and lint; emulator navigation; user-approved physical-device testing; recovery check; release signing; screenshots; changelog; security policy; then publish source and binaries to a user-selected GitHub repository. Do not publish private diagnostic files or keys.
