# Web16 Android App

- Repository name: `Web16`
- 16 independent Android WebViews, landscape orientation.
- Tiles show serial numbers 01–16 only; URL is not displayed in the tiles.
- Enter a screen number and HTTPS URL in the setup bar, then tap Open.
- Links are saved locally and loaded again on next launch.
- GitHub Actions builds a debug APK.

## Build using a phone
1. Create a GitHub repository named `Web16`.
2. Upload the *contents* of this folder to the repository root.
3. Open Actions and run `Build Web16 APK`.
4. Download the `Web16-debug-APK` artifact from the successful run.
5. Extract the ZIP and install the APK.

Some sites block WebView or require sign-in. Sixteen simultaneous WebViews can use substantial RAM and battery.
