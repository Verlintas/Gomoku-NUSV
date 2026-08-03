# iOS Installation Guide (No Paid Developer Account)

iPhones normally only allow apps from the App Store. This project's iOS build is
not on the App Store yet, but you can install it on your own iPhone using any of
the methods below — **none of them require the paid Apple Developer Program
($99/year)**.

> Version: `Gomoku-NUSV-1.4.6-compatibility.ipa` (unsigned, see "Getting the IPA"
> below) or build it yourself from source with Xcode.

---

## Option A: Xcode Free Personal Signing (Official, Recommended)

Sign and install with a free Apple ID directly in Xcode. Most reliable and
fully controlled.

### Prerequisites

- A Mac with Xcode installed (this guide targets Xcode 26)
- A USB cable (a trustworthy one)
- An iPhone (iOS 14+) and a free Apple ID

### Steps

1. **Open the project**
   ```bash
   open iosApp/iosApp.xcodeproj
   ```

2. **Sign in with your Apple ID**
   Xcode menu → Settings → Accounts → click `+` at the bottom left →
   Apple ID → sign in. If asked to choose a team, pick **Personal Team** (free).

3. **Configure signing**
   - Select the `iosApp` project → `iosApp` target in the left panel
   - Open the **Signing & Capabilities** tab
   - Tick **Automatically manage signing**
   - Set **Team** to the Personal Team you just added

4. **Connect your iPhone**
   - Plug the iPhone into the Mac with the USB cable
   - Tap “Trust This Computer” on the iPhone when prompted
   - In Xcode, choose your iPhone as the run destination (if it does not
     appear, check Window → Devices and Simulators)

5. **Run**
   Press `Cmd + R`. The first build takes a few minutes (it compiles the Kotlin
   framework).

6. **Trust the developer certificate**
   On first launch the iPhone warns about an untrusted developer. Go to
   **Settings → General → VPN & Device Management**, tap your Apple ID, and
   tap **Trust**.

### Limitations

- Apps installed with free signing **expire after 7 days** (they refuse to open).
- To renew: reconnect the iPhone and press `Cmd + R` in Xcode again.
- Free signing allows up to 3 apps at a time (not an issue in practice).

---

## Option B: AltStore Sideloading (Wireless Renewal)

AltStore is an open-source sideloading tool. Its biggest advantage is **automatic
7-day renewal** while your computer's AltServer is online — no cable needed each
time.

### Prerequisites

- A computer (Windows or macOS) with **AltServer** installed:
  - Windows: <https://altstore.io> (AltServer for Windows)
  - macOS: AltServer for macOS (macOS 10.15+)
- An Apple ID

### Steps

1. **Install and run AltServer** (on macOS, allow it in System Settings →
   Privacy & Security; on Windows, iCloud / Apple Devices is required).
2. **Connect the iPhone via cable**, open the AltServer menu, select your device →
   **Install AltStore**, and enter your Apple ID and password (an app-specific
   password is more secure).
3. The **AltStore** icon appears on the iPhone's home screen. On first open,
   trust the developer certificate (Settings → General → VPN & Device
   Management).
4. **Install the game**:
   - Download `Gomoku-NUSV-1.4.6-compatibility.ipa` on your computer
   - Import the `.ipa` into the iPhone's **Files** app (iCloud Drive or
     On My iPhone)
   - Open AltStore → **My Apps** tab → tap `+` in the top-left corner → select
     the `.ipa` → **Install**. AltStore re-signs it with your Apple ID
     automatically.
5. **Renewal**: AltStore attempts to refresh apps before they expire — as long
   as your computer's AltServer is on the same network and online. You can also
   refresh manually in AltStore.

### Notes

- The free AltStore plan allows up to 3 apps, which is enough.
- AltServer must be online occasionally; otherwise you need to reconnect the
  device to refresh before the 7-day expiry.
- Sideloading is a permitted personal-use scenario (testing on your own device)
  and does not affect warranty or system security — but only install IPAs from
  trusted sources.

---

## Option C: 爱思助手 (i4.cn — common in mainland China)

i4 Assistant (<https://www.i4.cn>) supports “unsigned install”: it re-signs the
IPA with your Apple ID automatically. Easiest option overall.

### Steps

1. Install and open **爱思助手** on your computer, connect the iPhone via cable
   (trust the computer on the device first).
2. Go to the “Apps” section → “Import & Install” → choose
   `Gomoku-NUSV-1.4.6-compatibility.ipa`.
3. Follow the prompts to enter your Apple ID (the tool guides you through
   creating an app-specific password), and wait for the automatic re-sign and
   install.
4. The app icon appears on the phone. On first open, trust the developer
   certificate (Settings → General → VPN & Device Management).
5. When the 7-day expiry approaches, re-run the “repair / reinstall” in i4 to
   renew.

### Notes

- i4 Assistant is a third-party tool: download it from the official site and
  watch out for bundled offers during installation.
- If you prefer to avoid third-party tools, use Option A or B.

---

## FAQ

**Q: The app won't open — “Untrusted Developer”?**
Go to Settings → General → VPN & Device Management → find the developer
certificate → tap **Trust**.

**Q: The app crashes after 7 days?**
That is normal for free signing. Re-sign / renew using the method you chose.
Your save data is not lost (it lives in the app sandbox and survives reinstall
as long as you don't delete the app).

**Q: Do I need to jailbreak?**
No. None of the three methods requires a jailbreak.

**Q: Can it be published on the App Store?**
That requires the paid Apple Developer Program ($99/year) and App Review;
the current build is not published.

---

## Getting the IPA

- Download from the GitHub Releases page:
  `Gomoku-NUSV-1.4.6-compatibility.ipa`
  (https://github.com/Verlintas/Gomoku-NUSV/releases)
- Or build it yourself (macOS + Xcode):

  ```bash
  # 1. Build the unsigned device version
  xcodebuild -project iosApp/iosApp.xcodeproj -target iosApp \
    -sdk iphoneos -configuration Debug ARCHS=arm64 ONLY_ACTIVE_ARCH=YES \
    CODE_SIGNING_ALLOWED=NO build

  # 2. Package it as an IPA (Payload structure)
  cd iosApp/build/Debug-iphoneos
  mkdir -p /tmp/ipa/Payload
  cp -r Gomoku-NUSV.app /tmp/ipa/Payload/
  cd /tmp/ipa && zip -r Gomoku-NUSV.ipa Payload
  ```

> Disclaimer: sideloading is intended for testing on your own devices. Only
> download the IPA from this repository's official Releases, and avoid modified
> builds from unknown sources.
