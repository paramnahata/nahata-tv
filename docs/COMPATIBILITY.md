# Compatibility and test scope

| Device / setup | Evidence | Status |
| --- | --- | --- |
| Hisense HiSmartTV A4, `xiaoyushan`, Android 9 | Installed and launched on physical TV; Home UI captured | Development-tested |
| Home D-pad, app opening, Settings and About | Earlier device checks; new UI startup captured | Partially tested, not a full regression suite |
| Hisense HDMI 2 | Active TV input session confirmed on the development firmware | Verified on that firmware |
| Show/Hide on Home | Netflix hidden from Home, remained in Apps, then restored | Verified |
| Home override and original mode | Home from Settings returned Nahata; original mode opened stock UI | Verified on development TV |
| Rearrange | Device screenshot later showed reordered Live TV first | User-exercised; cancellation and persistence need broader testing |
| Light scene / other themes | Source and resource build checks, generated assets inspected | No published current light-theme device screenshot |
| Ambient motion | Lifecycle/reduced-motion handling implemented and build checked | Performance has not been benchmarked across TVs |
| Other Android TV / Google TV 9+ | No physical device tests yet | Unverified |
| Samsung Tizen, LG webOS, Hisense VIDAA | Different operating systems | Not supported by this APK |
| Android TV box HDMI switching | Depends on exposed TV input services | Often unavailable; do not assume support |
| Laptop browser remote | Pairing, status, Back/Home tested in desktop browser; seven authentication/safety tests | Laptop-dependent prototype |
| Google TV official mobile remote | Official Android/iPhone product capability | Must be paired and tested on each actual TV |
| Custom native Nahata phone remote | Architecture documented | Not implemented or included in this release |

Please report brand/model, Android API level, launcher version, remote type, reproduction steps and which features work. Redact account names, pairing codes, local addresses and private content from reports.
