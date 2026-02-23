# Tasker Click Plugin

This plugin allows Tasker to click UI elements by their ID using an Accessibility Service.

## Restricted Setting Issue (Android 13+)

If you see a "Restricted Setting" error when trying to enable the accessibility service, this is a security feature introduced in Android 13 for apps installed via side-loading (not from the Play Store).

**To fix this:**
1. Go to **Settings > Apps > See all apps > Tasker Click Plugin**.
2. Tap the **3-dot menu** in the top-right corner.
3. Select **Allow restricted settings**.
4. You may need to authenticate with your fingerprint or PIN.

After allowing restricted settings, you can go back to **Settings > Accessibility > Downloaded apps** and enable the **Tasker Click Plugin** service.
