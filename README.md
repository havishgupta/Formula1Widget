# F1 Latest Android App

A Native Android Jetpack Compose application that fetches and displays the latest Formula 1 race results, championship standings, and the season schedule using the Jolpica API.

## Features
- **Latest Race Results:** View the final classification of the most recent F1 race.
- **World Championships:** Check the current World Drivers' Championship (WDC) and World Constructors' Championship (WCC) standings.
- **Season Schedule:** See all upcoming and past races, and click to view the race results for completed rounds.
- **Home Screen Widget:** A convenient, battery-safe widget to see the top 5 drivers (plus Max Verstappen) of the latest race directly from your Android home screen. It features a manual refresh button to get the latest data on demand.

## Installation Instructions

### Option 1: Download the APK (Easiest)
If you just want to install the app on your phone without touching any code, follow these steps:
1. Go to the **Actions** tab of this GitHub repository.
2. Click on the latest successful workflow run (named "Android CI").
3. Scroll down to the bottom of the page to the **Artifacts** section.
4. Download the `app-debug` zip file.
5. Extract the zip file and transfer the `.apk` file to your Android phone.
6. Open the APK on your phone to install it. *(Note: You may need to grant your file manager permission to "Install from unknown sources" since it is not downloaded from the Google Play Store).*

### Option 2: Build with Android Studio (For Developers)
If you want to view the code, modify the app, or run it in an emulator:
1. Clone this repository to your local machine: 
   ```bash
   git clone https://github.com/YOUR_USERNAME/F1-Latest-Android.git
   ```
2. Open **Android Studio**.
3. Select **Open an existing Android Studio project** and navigate to the cloned folder.
4. Wait for Gradle to sync the project dependencies.
5. Connect your Android device via USB (with USB Debugging enabled) or start an Android Virtual Device (Emulator).
6. Click the **Run 'app'** button (the green play icon in the toolbar) to build and deploy the app to your device.
