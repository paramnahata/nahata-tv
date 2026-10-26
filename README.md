# नाहटा · Nahata TV

**Your home. Your screen.** An original, quiet Android TV launcher developed by **Param Nahata**.

[Website](https://paramnahata.com) · [Feature roadmap](docs/ROADMAP.md) · [Privacy](PRIVACY.md)

## Implemented in 0.1.0

- Brief Devanagari logo reveal and staggered home entrance; respects system animation-off setting.
- Native D-pad focus, installed TV and regular app discovery, alphabetical grid and local search.
- Long-press app actions: favourites, hide, and Android app information.
- Restore hidden apps, spacious/compact grid, saffron/mint accent and custom welcome message.
- TV settings and system input-settings shortcuts with a firmware fallback message.
- Persistent local preferences, About page and “Developed by Param Nahata” credit.
- No Internet permission, accounts, advertisements, analytics or external runtime libraries.

## Status

Early development build. No physical-TV testing has been performed. HDMI switching, default-home selection and remote behaviour require device validation. The Inputs button opens system input settings; it does not promise direct Jio control. This project does not contain Projectivy code and is not affiliated with Projectivy or Google.

## Build

Use Android Studio with JDK 17+ and Android SDK 35. Run `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`). The included wrapper uses Gradle 8.13. Run `./gradlew lintDebug` for static checks. A GitHub Actions build workflow is included; it has not yet run on GitHub.

Output: `app/build/outputs/apk/debug/app-debug.apk`. Debug signing is for development; use your own private release key for distribution. Never commit signing keys.

## Install later, when the TV is available

Keep your stock launcher enabled. Install the APK manually, open Nahata TV from Apps and validate navigation and app launching before selecting it in Android's default-home settings. Do not disable firmware packages to force home behaviour.

To revert, select the original launcher in system default-home settings. If needed, through an already-authorized ADB connection run `adb shell am start -a android.settings.HOME_SETTINGS`. Remove Nahata TV through system Apps settings after reverting. No factory reset is needed.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Include TV model, Android version, reproduction steps and redacted logs in bug reports. Screenshots and compatibility reports are welcome. Do not submit third-party proprietary code or copyrighted artwork without permission.

## Author and license

Developed by Param Nahata · https://paramnahata.com

MIT licensed. Product name availability and trademark clearance have not been checked. Stars and GitHub trending are organic outcomes, not promised features.
