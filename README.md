# Tasker Click Plugin

**Tasker Click Plugin** is an Android application and Tasker plugin that allows you to automate UI clicks on Android apps by target View ID using an Accessibility Service.

---

## Features

- **Automated View Clicks:** Trigger automated click actions on UI elements matching a specified resource View ID.
- **State Plugin (View ID Visible):** React when a specific View ID becomes visible/shows on screen.
- **View ID Capture Tool:** Capture View IDs directly from active app windows using an interactive picker or notification trigger.
- **Automatic Clipboard Copy:** Selecting a captured View ID automatically copies it to your clipboard for easy configuration.
- **Tasker Integration:** Seamlessly integrates as both an Action and State/Condition plugin using standard Locale/Tasker plugin APIs.

---

## Prerequisites & Requirements

- **Android Version:** Android 7.0 (API Level 24) or higher.
- **Tasker:** [Tasker app](https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm) installed on your Android device.
- **Accessibility Permission:** The **Tasker Click Plugin** accessibility service must be enabled in Android Settings.

---

## Setup Instructions

### 1. Enable Accessibility Service

1. Open **Settings** on your Android device.
2. Navigate to **Accessibility** > **Downloaded apps** (or **Installed services**).
3. Select **Tasker Click Plugin**.
4. Toggle the switch to **On** and grant the required accessibility permissions.

### 2. Restricted Setting Workaround (Android 13+)

If you see a **"Restricted Setting"** dialog when trying to enable the accessibility service, this is an Android security feature for side-loaded apps installed outside of Google Play.

**To resolve this issue:**

1. Go to **Settings** > **Apps** > **See all apps**.
2. Find and tap **Tasker Click Plugin**.
3. Tap the **3-dot menu** icon in the top-right corner.
4. Select **Allow restricted settings**.
5. Authenticate with your PIN, pattern, or fingerprint if prompted.
6. Return to **Settings** > **Accessibility** > **Downloaded apps** and enable **Tasker Click Plugin**.

---

## How to Use in Tasker

### Action Plugin (Click by ID)

1. Open **Tasker** and open or create a Task.
2. Tap **+** to add a new Action.
3. Select **Plugin** > **Tasker Click Plugin** > **Click by ID**.
4. Tap the **Edit** (pencil) icon to configure the action plugin:
   - **Manual Input:** Enter the target view ID (e.g., `com.example.app:id/button_submit`) into the View ID field.
   - **Pick View ID:** Tap **Pick View ID** to trigger the notification capture helper. Switch to the target application, trigger the capture notification, and select the View ID from the detected list.
5. Save the configuration and go back to save your Tasker task.

### State Plugin (View ID Visible)

1. Open **Tasker** and create a new Profile.
2. Choose **State** > **Plugin** > **Tasker Click Plugin** > **View ID Visible**.
3. Tap the **Edit** (pencil) icon to configure the condition plugin:
   - Enter or pick the View ID to monitor.
4. Save the configuration and associate the Profile with your desired Tasker task.

---

## Building and Testing

### Prerequisites
- JDK 17
- Android SDK (API Level 34)

### Build Debug APK
```bash
./gradlew assembleDebug
```
The generated APK file will be located at `app/build/outputs/apk/debug/app-debug.apk`.

### Run Unit Tests
```bash
./gradlew test
```

---

## License

This project is free and unencumbered software released into the public domain under [The Unlicense](http://unlicense.org/). See the [LICENSE](LICENSE) file for details.
