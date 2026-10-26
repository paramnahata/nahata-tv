# नाहटा · Nahata TV

**Your home. Your screen.** An original, quiet Android TV launcher developed by **Param Nahata**.

[Website](https://paramnahata.com) · [Feature roadmap](docs/ROADMAP.md) · [Privacy](PRIVACY.md)

## Current development version: 0.4.0

The Home now uses one centred lower glass dock, a sliding focus highlight and minimal app-title transitions. Appearance offers Midnight Dark and Pearl Light, with theme-matched native dialogs. Background switches between the bundled original Alpine landscape and Aurora gradients. No instructional copy or widgets appear on Home. See [ASSETS.md](ASSETS.md) for wallpaper provenance and its generation prompt.

Home shows Netflix, Prime Video, YouTube and Live TV by default. Open the compact Apps icon for the complete collection. Hold OK on any app to Show on Home or Hide on Home; hiding never uninstalls it or removes it from Apps. Wi-Fi, input selection, remote help, personalisation, settings and About use compact accessible icon controls. The small Jio shortcut opens HDMI 2 on the verified development TV.

Includes glass-style panels, a static aurora gradient, horizontal app sliders and a verified HDMI 2 shortcut on the development Hisense. Back on the Home root stays in Nahata TV. Back in Apps returns Home.

Some Hisense firmware ignores the preferred Home selection because its system launcher has higher priority. An optional **Nahata TV Home override** accessibility service routes stock-launcher window events back to Nahata. Enable it only with informed consent. Its configuration is limited to `typeWindowStateChanged` from `com.google.android.tvlauncher`; it cannot retrieve screen content. Personalise → Switch to original launcher pauses the override; open Nahata TV from Apps to reactivate it. No stock launcher is disabled. Disable the service in system Accessibility settings to remove the override completely.

[Nahata Remote](remote/README.md) is an included local browser remote for Android/iPhone with an authenticated laptop bridge. The laptop must remain awake. It is not a standalone phone app or an offline PWA. See the remote README for trusted-Wi-Fi restrictions and English-only typing limitations.

See [CHANGELOG.md](CHANGELOG.md) for current changes. The section below describes the original preview, not the complete current feature set.

## Implemented in 0.1.0

- Brief Devanagari logo reveal and staggered home entrance; respects system animation-off setting.
- Native D-pad focus, installed TV and regular app discovery, alphabetical grid and local search.
- Long-press app actions: favourites, hide, and Android app information.
- Restore hidden apps, spacious/compact grid, saffron/mint accent and custom welcome message.
- TV settings and system input-settings shortcuts with a firmware fallback message.
- Persistent local preferences, About page and “Developed by Param Nahata” credit.
- No Internet permission, accounts, advertisements, analytics or external runtime libraries.

## Status

Early development build. The launcher and HDMI 2 shortcut have been tested on a Hisense HiSmartTV A4 running Android 9; this does not establish compatibility with other firmware. Phone hardware testing and broader lifecycle validation remain necessary. This project does not contain Projectivy code and is not affiliated with Projectivy or Google.

## Build

Use Android Studio with JDK 17+ and Android SDK 35. Run `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`). The included wrapper uses Gradle 8.13. Run `./gradlew lintDebug` for static checks. A GitHub Actions build workflow is included; it has not yet run on GitHub.

Output: `app/build/outputs/apk/debug/app-debug.apk`. Debug signing is for development; use your own private release key for distribution. Never commit signing keys.

## Install later, when the TV is available

Keep your stock launcher enabled. Install the APK manually, open Nahata TV from Apps and validate navigation and app launching before selecting it in Android's default-home settings. Do not disable firmware packages to force home behaviour.

To revert, disable Nahata TV Home override in system Accessibility settings, then select the original launcher in system default-home settings. If needed, through an already-authorized ADB connection run `adb shell cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity` on the verified Hisense firmware. Remove Nahata TV through system Apps settings after reverting. No factory reset is needed.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Include TV model, Android version, reproduction steps and redacted logs in bug reports. Screenshots and compatibility reports are welcome. Do not submit third-party proprietary code or copyrighted artwork without permission.

## Author and license

Developed by Param Nahata · https://paramnahata.com

MIT licensed. Product name availability and trademark clearance have not been checked. Stars and GitHub trending are organic outcomes, not promised features.
