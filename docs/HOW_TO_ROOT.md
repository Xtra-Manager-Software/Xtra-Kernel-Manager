# How To Grant Root Access to Xtra Kernel Manager

This guide will help you grant root access to Xtra Kernel Manager (XKM) using different root management solutions.

## Table of Contents
- [KernelSU](#kernelsu)
- [Magisk](#magisk)
- [APatch](#apatch)
- [Troubleshooting](#troubleshooting)

---

## KernelSU

KernelSU is a kernel-based root solution that provides root access through a modified kernel.

### Steps to Grant Root Access:

1. **Open KernelSU Manager**
   - Launch the KernelSU app from your app drawer
   - Make sure KernelSU is properly installed and working

2. **Navigate to Superuser Tab**
   - Tap on the "Superuser" tab at the bottom of the screen
   - You'll see a list of apps that have requested root access

3. **Find Xtra Kernel Manager**
   - Look for "Xtra Kernel Manager" or "XKM" in the list
   - If it's not visible, open XKM first to trigger a root request

4. **Grant Root Permission**
   - Tap on the Xtra Kernel Manager entry
   - Toggle the switch to **ON** (enabled)
   - Confirm any prompts that appear

5. **Configure Root Access (Optional)**
   - You can set additional options like:
     - **Grant root access**: Enable/Disable
     - **Notification**: Show notification when app uses root
     - **Logging**: Keep logs of root access

6. **Important: Force Stop KernelSU (If Required)**
   - Some KernelSU versions require force stopping the manager after granting permissions
   - Go to Android Settings → Apps → KernelSU
   - Tap "Force Stop"
   - Return to XKM and tap "Try Again"

### Verification:
- Return to Xtra Kernel Manager
- Tap the "Try Again" button
- XKM should now detect root access and proceed to the home screen

---

## Magisk

Magisk is the most popular root solution that provides systemless root access.

### Steps to Grant Root Access:

1. **Open Magisk Manager**
   - Launch the Magisk app from your app drawer
   - Ensure Magisk is properly installed (you should see "Installed" status)

2. **Wait for Root Request Popup**
   - When you first open XKM, Magisk will show a root request popup
   - If the popup doesn't appear, tap "Grant Permissions" in XKM

3. **Grant Root Permission**
   - When the Magisk popup appears, tap **"Grant"**
   - The popup will show:
     - App name: Xtra Kernel Manager
     - Package: id.xms.xtrakernelmanager
     - Request: Root access

4. **Configure Superuser Settings (Optional)**
   - Open Magisk Manager
   - Tap the hamburger menu (☰) → **Superuser**
   - Find "Xtra Kernel Manager" in the list
   - Tap on it to configure:
     - **Access**: Grant or Deny
     - **Notification**: Show when app uses root
     - **Logging**: Keep command logs
     - **Auto Response**: Automatically grant/deny future requests

5. **Advanced Options**
   - You can also configure:
     - **Mount Namespace Mode**: Global or Isolated
     - **Timeout**: How long to wait for user response (default: 10 seconds)

### Verification:
- Return to Xtra Kernel Manager
- Tap the "Try Again" button
- XKM should now detect root access and proceed to the home screen

---

## APatch

APatch is a kernel-based root solution similar to KernelSU.

### Steps to Grant Root Access:

1. **Open APatch Manager**
   - Launch the APatch app from your app drawer
   - Verify APatch is properly installed

2. **Navigate to Superuser Section**
   - Tap on "Superuser" or "Root Management"
   - You'll see apps that have requested root

3. **Grant Permission to XKM**
   - Find "Xtra Kernel Manager" in the list
   - Toggle the switch to enable root access
   - Confirm any security prompts

4. **Configure Settings (Optional)**
   - Set notification preferences
   - Enable/disable logging
   - Configure auto-grant options

### Verification:
- Return to Xtra Kernel Manager
- Tap "Try Again"
- XKM should detect root and continue

---

## Troubleshooting

### Root Access Not Detected

**Problem**: XKM still shows "Root Access Required" screen after granting permissions.

**Solutions**:

1. **Force Stop Root Manager**
   - Go to Android Settings → Apps
   - Find your root manager (KernelSU/Magisk/APatch)
   - Tap "Force Stop"
   - Return to XKM and tap "Try Again"

2. **Restart XKM**
   - Close Xtra Kernel Manager completely
   - Clear it from recent apps
   - Open XKM again

3. **Reboot Device**
   - Sometimes a reboot is needed for root access to take effect
   - Restart your device
   - Open XKM again

4. **Check Root Manager Status**
   - Open your root manager app
   - Verify it shows as "Installed" or "Active"
   - Check if there are any updates available

5. **Reinstall Root Manager**
   - If nothing works, try reinstalling your root manager
   - Follow the official installation guide for your chosen solution

### Root Request Popup Not Appearing

**Problem**: Magisk/KernelSU doesn't show the root request popup.

**Solutions**:

1. **Check Superuser Settings**
   - Open your root manager
   - Go to Superuser/Root Management
   - Check if XKM is already in the list (might be denied)
   - Change the permission to "Grant"

2. **Clear Root Manager Data**
   - Go to Android Settings → Apps → [Root Manager]
   - Tap "Storage" → "Clear Data" (⚠️ This will reset all root permissions)
   - Open XKM again to trigger a new request

3. **Check Root Manager Logs**
   - Open your root manager
   - Check logs for any errors
   - Look for entries related to XKM

### XKM Crashes When Requesting Root

**Problem**: XKM crashes or closes when trying to request root access.

**Solutions**:

1. **Update Root Manager**
   - Make sure you're using the latest version
   - Check for updates in the root manager app

2. **Update XKM**
   - Check for XKM updates
   - Install the latest version

3. **Check SELinux Status**
   - Some root managers require SELinux to be in Permissive mode
   - Check your root manager's documentation

4. **Check Logs**
   - Use logcat to see crash logs:
     ```bash
     adb logcat | grep XKM
     ```

---

## Additional Notes

### Security Considerations

- **Only grant root access to trusted apps**: Root access gives apps full control over your device
- **Review permissions regularly**: Check which apps have root access periodically
- **Enable notifications**: Get notified when apps use root access
- **Keep root manager updated**: Security patches are important

### Why Does XKM Need Root?

Xtra Kernel Manager requires root access to:
- Modify kernel parameters (CPU governor, frequencies, etc.)
- Adjust GPU settings
- Control thermal management
- Optimize memory and I/O schedulers
- Apply system-level tweaks
- Monitor hardware statistics

Without root access, XKM cannot perform these low-level system modifications.
