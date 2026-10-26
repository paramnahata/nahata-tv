# Install, personalise and recover

## Supported starting point

- Android TV / Google TV running Android 9 (API 28) or later, with a D-pad remote.
- This is not firmware. It does not convert VIDAA, Tizen, webOS or a non-Android TV into Android TV.
- Development validation: Hisense HiSmartTV A4, device `xiaoyushan`, Android 9. Other TVs are unverified until reported in [COMPATIBILITY.md](COMPATIBILITY.md).
- The original launcher must remain enabled. No root, bootloader unlock, firmware flashing or factory reset is required.

## Download and verify

Download the APK from the repository's [Releases](https://github.com/paramnahata/nahata-tv/releases), together with `SHA256SUMS.txt`. Use published preview APKs consistently for updates; arbitrary local/CI debug builds may use different signing certificates.

On Windows PowerShell:

```powershell
Get-FileHash .\Nahata-TV-0.5.1-preview.apk -Algorithm SHA256
```

Compare with the published checksum before installing. This release is development-signed, not a production Play Store release.

## Install

Use your TV's supported APK installation flow. Alternatively, install Google's [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) on a computer and enable debugging using your TV manufacturer's instructions. Authorise only your own computer on the TV.

Replace `TV_IP` below with your TV's private local address; these commands are for TVs supporting ADB on port 5555. Some devices instead require their manufacturer-supported USB or wireless-debugging pairing flow.

```text
adb connect TV_IP:5555
adb -s TV_IP:5555 shell getprop ro.build.version.sdk
adb -s TV_IP:5555 shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb -s TV_IP:5555 install -r Nahata-TV-0.5.1-preview.apk
adb -s TV_IP:5555 shell am start -n com.paramnahata.nahatatv/.MainActivity
```

The first command connects to the TV, the next two read its Android API and existing Home component, `install -r` installs or updates only Nahata TV, and the last opens it. Record the original Home component for rollback. Expected minimum API: 28. Expected installation result: `Success`.

If an update reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, the APK signer differs. Do not uninstall system components or force a downgrade. Use an APK signed with the same key, or explicitly decide whether to remove only Nahata TV after recording its settings and restoring stock Home. Removing Nahata TV clears its local preferences.

## Make it yours

- Home begins with installed Netflix, Prime Video, YouTube and Live TV. Missing apps are not installed automatically.
- Apps icon opens the full launchable collection. Hold OK → Show on Home / Hide on Home. All apps remain installed and accessible in Apps.
- Hold OK on a Home app → Rearrange. Left/Right swaps positions, OK saves, Back discards the draft.
- Brush icon → Appearance: Midnight or Pearl. Background: Alpine Day/Night, Sunset or Aurora. Ambient motion can be disabled.
- Brush icon → Choose Jio input: select the actual port connected to your set-top box. The small Jio shortcut then uses that saved input. Other brands and renamed ports should be configured explicitly.
- A TV box does not necessarily expose the physical television's HDMI inputs. The launcher cannot invent missing firmware APIs.

## Home / Back behaviour

First test app launching, input selection and Settings access. Choose Nahata TV using Android's default-home settings if your firmware exposes them. An advanced user can set the preferred Home with:

```text
adb -s TV_IP:5555 shell cmd package set-home-activity --user 0 com.paramnahata.nahatatv/.MainActivity
```

Some firmware still routes Home to its system launcher despite reporting success. The optional Nahata TV Home override Accessibility service is a fallback for **Google TV launcher package `com.google.android.tvlauncher` only**. Review its disclosure and enable it yourself in Android Accessibility settings if needed. It receives only that package's window-state events; it cannot retrieve screen content. Other OEM launchers are not covered by this fallback.

Back at Nahata's root remains on Nahata; Back in Apps returns Home. Behaviour inside third-party apps depends on those apps. Choosing Original launcher mode intentionally pauses the override until Nahata TV is reopened.

## Recover stock Home

1. Disable **Nahata TV Home override** in Android Accessibility settings.
2. Restore the original launcher in system default-home settings. With authorised ADB, substitute the original component you recorded:

```text
adb -s TV_IP:5555 shell cmd package set-home-activity --user 0 ORIGINAL_COMPONENT
adb -s TV_IP:5555 shell input keyevent 3
```

On the development Hisense only, the recorded component is `com.google.android.tvlauncher/.MainActivity`. Do not assume that value on other brands. Nahata TV does not disable or uninstall the original launcher.

## Finish setup securely

Disconnect your computer with `adb disconnect TV_IP:5555` and turn off debugging in TV settings when it is no longer needed. The launcher itself and a compatible Google TV phone remote do not need ADB. The optional laptop browser bridge does need its authorised ADB connection while in use. Never forward ADB or the bridge to the Internet.
