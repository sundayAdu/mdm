/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.iot.agent.api;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Browser;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import android.util.Base64;
import android.util.Log;

import org.wso2.iot.agent.AndroidAgentException;
import org.wso2.iot.agent.R;
import org.wso2.iot.agent.beans.AppInstallRequest;
import org.wso2.iot.agent.beans.AppUninstallRequest;
import org.wso2.iot.agent.beans.DeviceAppInfo;
import org.wso2.iot.agent.beans.Operation;
import org.wso2.iot.agent.beans.ServerConfig;
import org.wso2.iot.agent.proxy.IDPTokenManagerException;
import org.wso2.iot.agent.proxy.utils.ServerUtilities;
import org.wso2.iot.agent.utils.AlarmUtils;
import org.wso2.iot.agent.utils.AppManagementRequestUtil;
import org.wso2.iot.agent.utils.CommonUtils;
import org.wso2.iot.agent.utils.Constants;
import org.wso2.iot.agent.utils.Preference;
import org.wso2.iot.agent.utils.StreamHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class handles all the functionalities required for managing application
 * installation and un-installation.
 */
public class ApplicationManager {

    private static final String TAG = ApplicationManager.class.getName();

    private static final int SYSTEM_APPS_DISABLED_FLAG = 0;
    private static final int MAX_URL_HASH = 32;
    private static final int COMPRESSION_LEVEL = 100;
    private static final int BUFFER_SIZE = 1024;
    private static final int READ_FAILED = -1;
    private static final int BUFFER_OFFSET = 0;
    private static final String ACTION_INSTALL_COMPLETE = "INSTALL_COMPLETED";

    private Context context;
    private Resources resources;
    private PackageManager packageManager;
    private DevicePolicyManager policyManager;
    private static long downloadReference = -1;
    private String appUrl;

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long referenceId = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (downloadReference == referenceId) {
                String downloadDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.
                        DIRECTORY_DOWNLOADS).getPath();
                File file = new File(downloadDirectoryPath, resources.getString(R.string.download_mgr_download_file_name));
                if (file.exists()) {
                    Preference.putString(context, context.getResources().getString(
                            R.string.app_install_status), Constants.AppState.DOWNLOAD_COMPLETED);
                    PackageManager pm = context.getPackageManager();
                    PackageInfo info = pm.getPackageArchiveInfo(downloadDirectoryPath + File.separator + resources.
                                                                    getString(R.string.download_mgr_download_file_name),
                                                                PackageManager.GET_ACTIVITIES);
                    if (info != null && info.packageName != null) {
                        Preference.putString(context, context.getResources().getString(R.string.shared_pref_installed_app),
                                info.packageName);
                    }
                    Preference.putString(context, context.getResources().getString(R.string.shared_pref_installed_file),
                            resources.getString(R.string.download_mgr_download_file_name));
                    Preference.putString(context, context.getResources().getString(
                            R.string.app_install_status), Constants.AppState.INSTALLED);
                    startInstallerIntent(Uri.fromFile(new File(downloadDirectoryPath + File.separator +
                            resources.getString(R.string.download_mgr_download_file_name))));
                } else {
                    Preference.putString(context, context.getResources().getString(
                            R.string.app_install_status), Constants.AppState.DOWNLOAD_FAILED);
                    Preference.putString(context, context.getResources().getString(
                            R.string.app_install_failed_message), "App file creation failed on the device.");
                }
            }
        }
    };

    public ApplicationManager(Context context) {
        this.context = context;
        this.resources = context.getResources();
        this.packageManager = context.getPackageManager();
        this.policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    /**
     * Returns a list of all the applications installed on the device.
     *
     * @return - List of applications which installed on the device.
     */
    public Map<String, DeviceAppInfo> getInstalledApps() {
        Map<String, DeviceAppInfo> appList = new HashMap<>();
        List<PackageInfo> packages = packageManager.getInstalledPackages(SYSTEM_APPS_DISABLED_FLAG);
        DeviceAppInfo app;

        for (PackageInfo packageInfo : packages) {
            if (Constants.ALLOW_SYSTEM_APPS_IN_APPS_LIST_RESPONSE) {
                app = new DeviceAppInfo();
                app.setAppname(packageInfo.applicationInfo.
                        loadLabel(packageManager).toString());
                app.setPackagename(packageInfo.packageName);
                app.setVersionName(packageInfo.versionName);
                app.setVersionCode(packageInfo.versionCode);
                app.setIsSystemApp(isSystemPackage(packageInfo));
                app.setIsRunning(isAppRunning(packageInfo.packageName));
                appList.put(packageInfo.packageName, app);
            } else if (!isSystemPackage(packageInfo)) {
                app = new DeviceAppInfo();
                app.setAppname(packageInfo.applicationInfo.
                        loadLabel(packageManager).toString());
                app.setPackagename(packageInfo.packageName);
                app.setVersionName(packageInfo.versionName);
                app.setVersionCode(packageInfo.versionCode);
                app.setIsSystemApp(false);
                app.setIsRunning(isAppRunning(packageInfo.packageName));
                appList.put(packageInfo.packageName, app);
            }
        }
        return appList;
    }

    public boolean isAppRunning(String packageName) {
        boolean isRunning = false;
        try {
            Process process = Runtime.getRuntime().exec("ps");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = in.read(buffer)) > 0) {
                output.append(buffer, 0, read);
                if (output.toString().contains(packageName)) {
                    isRunning = true;
                }
            }
            in.close();
            if (output.toString().contains(packageName)) {
                isRunning = true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Running processes shell command failed execution." + e);
        } finally {
            return isRunning;
        }
    }

    /**
     * Returns a list of all the applications installed on the device by user.
     *
     * @return - List of applications which installed on the device by user.
     */
    public List<String> getInstalledAppsByUser() {
        List<String> packagesInstalledByUser = new ArrayList<>();
        int flags = PackageManager.GET_META_DATA;
        List<ApplicationInfo> applications = packageManager.getInstalledApplications(flags);
        for (ApplicationInfo appInfo : applications) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                packagesInstalledByUser.add(appInfo.packageName);
            }
        }
        return packagesInstalledByUser;
    }

    /**
     * Return the list of the package names of the apps(hidden and visible) that are user owned
     *
     * @return - list of package names of the apps that are not system apps
     */
    public List<String> getAppsOfUser() {
        List<String> packagesInstalledByUser = new ArrayList<>();
        int flags = PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES
                | PackageManager.GET_UNINSTALLED_PACKAGES;
        List<ApplicationInfo> applications = packageManager.getInstalledApplications(flags);
        for (ApplicationInfo appInfo : applications) {
            if (!((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)) {
                packagesInstalledByUser.add(appInfo.packageName);
            }
        }
        return packagesInstalledByUser;
    }

    public boolean isPackageInstalled(String packagename) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packagename, 0);
            if (packageInfo != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * Installs an application to the device.
     *
     * @param fileUri - File URI should be passed in as a String.
     */
    private void startInstallerIntent(Uri fileUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                policyManager.isDeviceOwnerApp(Constants.AGENT_PACKAGE)) {
            installPackage(fileUri);
        } else {
            boolean isUnknownSourcesDisallowed = Preference.getBoolean(context,
                    Constants.PreferenceFlag.DISALLOW_UNKNOWN_SOURCES);
            CommonUtils.allowUnknownSourcesForProfile(context, !isUnknownSourcesDisallowed);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                Uri apkURI = FileProvider.getUriForFile(context,
                        Constants.AGENT_PACKAGE + ".provider", new File(fileUri.getPath()));
                intent.setDataAndType(apkURI, resources.getString(R.string.application_mgr_mime));
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, resources.getString(R.string.application_mgr_mime));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean installPackage(Uri fileUri) {

        InputStream in;
        OutputStream out;
        String packageName = Preference.getString(context, Constants.PreferenceFlag.CURRENT_INSTALLING_APP);
        try {
            File application = new File(fileUri.getPath());
            in = new FileInputStream(application);
            PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName(packageName);
            // set params
            int sessionId = packageInstaller.createSession(params);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            out = session.openWrite(Constants.AGENT_PACKAGE, 0, -1);
            byte[] buffer = new byte[65536];
            int c;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            in.close();
            out.close();

            session.commit(createIntentSender(context, sessionId, packageName));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while installing application '" + packageName + "'", e);
        }
        return false;
    }

    private static IntentSender createIntentSender(Context context, int sessionId, String packageName) {
        Intent intent = new Intent(ACTION_INSTALL_COMPLETE);
        intent.putExtra("packageName", packageName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                0);
        return pendingIntent.getIntentSender();
    }

    /**
     * Returns whether the app is a system app.
     *
     * @param packageInfo - Package of the app which you need the status.
     * @return - App status.
     */
    private boolean isSystemPackage(PackageInfo packageInfo) {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    /**
     * Returns a base64 encoded string for a particular image.
     *
     * @param drawable - Image as a Drawable object.
     * @return - Base64 encoded value of the drawable.
     */
    public String encodeImage(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_LEVEL, outStream);
        byte[] bitmapContent = outStream.toByteArray();
        String encodedImage = Base64.encodeToString(bitmapContent, Base64.NO_WRAP);
        StreamHandler.closeOutputStream(outStream, TAG);

        return encodedImage;
    }

    /**
     * Installs an application to the device.
     *
     * @param url       - APK Url should be passed in as a String.
     * @param schedule  - If update/installation is scheduled, schedule information should be passed.
     * @param operation - App installation operation.
     */
    public void installApp(String url, String schedule, Operation operation) throws AndroidAgentException {
        if (schedule != null && !schedule.trim().isEmpty() && !schedule.equals("undefined") && !schedule.equals("false")) {
            try {
                AlarmUtils.setOneTimeAlarm(context, schedule, Constants.Operation.INSTALL_APPLICATION, operation, url, null);
            } catch (ParseException e) {
                String message = "Scheduling failed due to " + e.getMessage();
                Log.e(TAG, message, e);
                throw new AndroidAgentException(message, e);
            }
            return; //Will call installApp method again upon alarm.
        }

        int operationId = 0;
        String operationCode = Constants.Operation.INSTALL_APPLICATION;

        if (operation != null) {
            // Get ongoing app installation operation details. These preferences are cleared during
            // reply payload creations which followed by application installation complete or error.
            operationId = Preference.getInt(context, context.getResources().getString(
                    R.string.app_install_id));
            operationCode = Preference.getString(context, context.getResources().getString(
                    R.string.app_install_code));

            if (operationId == operation.getId()) {
                Log.w(TAG, "Ignoring received operation " + operationId +
                        " as it has the same operation ID with ongoing operation.");
                return; //No point of putting same operation again to the pending queue. Hence ignoring.
            }

            //Check if there any ongoing operations in the state machine.
            if (operationId != 0 && operationCode != null) {
                AppInstallRequest appInstallRequest = new AppInstallRequest();
                appInstallRequest.setApplicationOperationId(operation.getId());
                appInstallRequest.setApplicationOperationCode(operation.getCode());
                appInstallRequest.setAppUrl(url);
                //Add installation operation to pending queue
                AppManagementRequestUtil.addPendingInstall(context, appInstallRequest);
                if (Constants.DEBUG_MODE_ENABLED) {
                    Log.d(TAG, "Queued operation Id " + appInstallRequest.getApplicationOperationId());
                    Log.d(TAG, "Added request to pending queue as there is another installation ongoing.");
                }
                return; //Will call installApp method again once current installation completed.
            }
            operationId = operation.getId();
            operationCode = operation.getCode();
        }
        setupAppDownload(url, operationId, operationCode);
    }

    /**
     * Cancels ongoing download if there any.
     */
    public void cancelOngoingDownload(){
        if (downloadReference != -1) {
            final DownloadManager downloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager == null) {
                return;
            }
            downloadManager.remove(downloadReference);
            downloadReference = -1;
        }
    }

    /**
     * Start app download for install on device.
     *
     * @param url           - APK Url should be passed in as a String.
     * @param operationId   - Id of the operation.
     * @param operationCode - Requested operation code.
     */
    public void setupAppDownload(String url, int operationId, String operationCode) {
        Preference.putInt(context, context.getResources().getString(
                R.string.app_install_id), operationId);
        Preference.putString(context, context.getResources().getString(
                R.string.app_install_code), operationCode);

        if (url.contains(Constants.APP_DOWNLOAD_ENDPOINT) && Constants.APP_MANAGER_HOST != null) {
            url = url.substring(url.lastIndexOf("/"), url.length());
            this.appUrl = Constants.APP_MANAGER_HOST + Constants.APP_DOWNLOAD_ENDPOINT + url;
        } else if (url.contains(Constants.APP_DOWNLOAD_ENDPOINT)) {
            url = url.substring(url.lastIndexOf("/"), url.length());
            String ipSaved = Constants.DEFAULT_HOST;
            String prefIP = Preference.getString(context, Constants.PreferenceFlag.IP);
            if (prefIP != null) {
                ipSaved = prefIP;
            }
            ServerConfig utils = new ServerConfig();
            if (ipSaved != null && !ipSaved.isEmpty()) {
                utils.setServerIP(ipSaved);
                this.appUrl = utils.getAPIServerURL(context) + Constants.APP_DOWNLOAD_ENDPOINT + url;
            } else {
                String errorText = "There is no valid IP to contact the server";
                Preference.putString(context, context.getResources().getString(
                        R.string.app_install_status), Constants.AppState.DOWNLOAD_FAILED);
                Preference.putString(context, context.getResources().getString(
                        R.string.app_install_failed_message), errorText);
                Log.e(TAG, errorText);
                return;
            }
        } else {
            this.appUrl = url;
        }

        Preference.putLong(context, Constants.PreferenceFlag.DOWNLOAD_INITIATED_AT,
                Calendar.getInstance().getTimeInMillis());
        Preference.putString(context, context.getResources().getString(
                R.string.app_install_status), Constants.AppState.DOWNLOAD_STARTED);
        if (Constants.DEBUG_MODE_ENABLED) {
            Log.d(TAG, "Using download manager to download the application");
        }
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(downloadReceiver, filter);
        removeExistingFile();
        downloadViaDownloadManager(this.appUrl, resources.getString(R.string.download_mgr_download_file_name));
    }

    /**
     * Removes an application from the device.
     *
     * @param packageName - Application package name should be passed in as a String.
     */
    public void uninstallApplication(String packageName, Operation operation, String schedule)
            throws AndroidAgentException {

        if (schedule != null && !schedule.trim().isEmpty() && !schedule.equals("undefined")) {
            try {
                AlarmUtils.setOneTimeAlarm(context, schedule,
                        Constants.Operation.UNINSTALL_APPLICATION, operation, null, packageName);
            } catch (ParseException e) {
                String message = "Scheduling failed due to " + e.getMessage();
                Log.e(TAG, message, e);
                throw new AndroidAgentException(message, e);
            }
            return; //Will call uninstallApplication method again upon alarm.
        }

        if (operation != null) {
            // Get ongoing app uninstallation operation details. These preferences are cleared during
            // reply payload creations which followed by application uninstallation complete or error.
            int operationId = Preference.getInt(context, context.getResources().getString(
                    R.string.app_uninstall_id));
            String operationCode = Preference.getString(context, context.getResources().getString(
                    R.string.app_uninstall_code));

            if (operationId == operation.getId()) {
                Log.w(TAG, "Ignoring received operation " + operationId +
                        " as it has the same operation ID with ongoing operation.");
                return; //No point of putting same operation again to the pending queue. Hence ignoring.
            }

            //Check if there any ongoing operations in the state machine.
            if (operationId != 0 && operationCode != null) {
                AppUninstallRequest appUninstallRequest = new AppUninstallRequest();
                appUninstallRequest.setApplicationOperationId(operation.getId());
                appUninstallRequest.setApplicationOperationCode(operation.getCode());
                appUninstallRequest.setPackageName(packageName);
                //Add uninstallation operation to pending queue
                AppManagementRequestUtil.addPendingUninstall(context, appUninstallRequest);
                if (Constants.DEBUG_MODE_ENABLED) {
                    Log.d(TAG, "Queued operation Id " + appUninstallRequest.getApplicationOperationId());
                    Log.d(TAG, "Added request to pending queue as there is another uninstallation ongoing.");
                }
                return; //Will call uninstallApplication method again once current uninstallation completed.
            } else {
                operationId = operation.getId();
                operationCode = operation.getCode();
            }
            Preference.putInt(context,
                    context.getResources().getString(R.string.app_uninstall_id), operationId);
            Preference.putString(context,
                    context.getResources().getString(R.string.app_uninstall_code), operationCode);
            Preference.putLong(context, Constants.PreferenceFlag.UNINSTALLATION_INITIATED_AT,
                    Calendar.getInstance().getTimeInMillis());
        }

        if(!this.isPackageInstalled(packageName)){
            String message = "Package '" + packageName + "' is not installed in the device or invalid package name";
            if (operation != null) {
                Preference.putInt(context,
                        context.getResources().getString(R.string.app_uninstall_id),
                        operation.getId());
                Preference.putString(context,
                        context.getResources().getString(R.string.app_uninstall_code),
                        operation.getCode());
                Preference.putString(context,
                        context.getResources().getString(R.string.app_uninstall_status),
                        Constants.AppState.UNINSTALL_FAILED);
                Preference.putString(context,
                        context.getResources().getString(R.string.app_uninstall_failed_message),
                        message);
            }
            throw new AndroidAgentException(message);
        }

        if (packageName != null &&
                !packageName.contains(resources.getString(R.string.application_package_prefix))) {
            packageName = resources.getString(R.string.application_package_prefix) + packageName;
        }

        if (Constants.SYSTEM_APP_ENABLED) {
            CommonUtils.callSystemApp(context, Constants.Operation.SILENT_UNINSTALL_APPLICATION, "", packageName);
        } else {
            if (operation != null) {
                Preference.putString(context, context.getResources().getString(R.string.app_uninstall_status),
                        Constants.AppState.UNINSTALLED);
                Preference.putString(context, context.getResources().getString(R.string.app_uninstall_failed_message),
                        null);
            }
            Uri packageURI = Uri.parse(packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(uninstallIntent);
        }
    }

    /**
     * Creates a webclip on the device home screen.
     *
     * @param url   - URL should be passed in as a String.
     * @param title - Title(Web app title) should be passed in as a String.
     */
    public void manageWebAppBookmark(String url, String title, String operationType)
            throws AndroidAgentException {
        final Intent bookmarkIntent = new Intent();
        final Intent actionIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        long urlHash = url.hashCode();
        long uniqueId = (urlHash << MAX_URL_HASH) | actionIntent.hashCode();

        actionIntent.putExtra(Browser.EXTRA_APPLICATION_ID, Long.toString(uniqueId));
        bookmarkIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
        bookmarkIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        bookmarkIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context,
                        R.drawable.ic_bookmark)
        );
        if (operationType != null) {
            if (resources.getString(R.string.operation_install).equalsIgnoreCase(operationType)) {
                bookmarkIntent.
                        setAction(resources.getString(R.string.application_package_launcher_install_action));
            } else if (resources.getString(R.string.operation_uninstall).equalsIgnoreCase(operationType)) {
                bookmarkIntent.
                        setAction(resources.getString(R.string.application_package_launcher_uninstall_action));
            } else {
                throw new AndroidAgentException("Cannot create webclip due to invalid operation type.");
            }
        } else {
            bookmarkIntent.
                    setAction(resources.getString(R.string.application_package_launcher_install_action));
        }
        context.sendBroadcastAsUser(bookmarkIntent, android.os.Process.myUserHandle());
    }

    public List<ApplicationInfo> getInstalledApplications() {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
    }

    public Operation getApplicationStatus(Operation operation, String status, String message) {
        switch (status) {
            case Constants.AppState.DOWNLOAD_STARTED:
                operation.setStatus(context.getResources().getString(R.string.operation_value_progress));
                operation.setOperationResponse("Application download started");
                break;
            case Constants.AppState.DOWNLOAD_RETRY:
                operation.setStatus(context.getResources().getString(R.string.operation_value_progress));
                operation.setOperationResponse(message);
                break;
            case Constants.AppState.DOWNLOAD_COMPLETED:
                operation.setStatus(context.getResources().getString(R.string.operation_value_progress));
                operation.setOperationResponse("Application download completed");
                break;
            case Constants.AppState.DOWNLOAD_FAILED:
                operation.setStatus(context.getResources().getString(R.string.operation_value_error));
                operation.setOperationResponse(message);
                break;
            case Constants.AppState.INSTALL_FAILED:
                operation.setStatus(context.getResources().getString(R.string.operation_value_error));
                operation.setOperationResponse(message);
                break;
            case Constants.AppState.INSTALLED:
                operation.setStatus(context.getResources().getString(R.string.operation_value_completed));
                operation.setOperationResponse("Application installation completed");
                break;
            case Constants.AppState.UNINSTALL_FAILED:
                operation.setStatus(context.getResources().getString(R.string.operation_value_error));
                operation.setOperationResponse(message);
                break;
            case Constants.AppState.UNINSTALLED:
                operation.setStatus(context.getResources().getString(R.string.operation_value_completed));
                operation.setOperationResponse("Application uninstallation completed");
                break;
            default:
                operation.setStatus(context.getResources().getString(R.string.operation_value_error));
                operation.setOperationResponse(message);
        }
        return operation;
    }

    private void removeExistingFile() {
        String directory = Environment.getExternalStorageDirectory().getPath() +
                resources.getString(R.string.application_mgr_download_location);
        File file = new File(directory);
        file.mkdirs();
        File outputFile = new File(file,
                resources.getString(R.string.application_mgr_download_file_name));

        if (outputFile.exists()) {
            outputFile.delete();
        }
    }

    /**
     * Initiate downloading via DownloadManager API.
     *
     * @param url     - File URL.
     * @param appName - Name of the application to be downloaded.
     */
    private void downloadViaDownloadManager(String url, String appName) {
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);

        // Restrict the types of networks over which this download may
        // proceed.
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                | DownloadManager.Request.NETWORK_MOBILE);
        // Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(true);
        // Set the title of this download, to be displayed in notifications
        // (if enabled).
        request.setTitle(resources.getString(R.string.downloader_message_title));
        request.setVisibleInDownloadsUi(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        // Set the local destination for the downloaded file to a path
        // within the application's external files directory
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, appName);
        // Enqueue a new download and same the referenceId
        downloadReference = downloadManager.enqueue(request);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadReference);
                    Cursor cursor = downloadManager.query(query);
                    cursor.moveToFirst();
                    int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.
                            COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.
                            STATUS_SUCCESSFUL) {
                        downloading = false;
                    }
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.
                            STATUS_FAILED) {
                        downloading = false;
                        Preference.putString(context, context.getResources().getString(
                                R.string.app_install_status), Constants.AppState.DOWNLOAD_FAILED);
                        Preference.putString(context, context.getResources().getString(
                                R.string.app_install_failed_message), "App download failed due to a connection issue.");
                    }
                    int downloadProgress = 0;
                    if (bytesTotal > 0) {
                        downloadProgress = (int) ((bytesDownloaded * 100l) / bytesTotal);
                    }
                    Preference.putString(context,
                            context.getResources().getString(R.string.app_download_progress),
                            String.valueOf(downloadProgress));
                    cursor.close();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Download manager monitoring interrupted.", e);
                    }
                }
                Preference.putString(context,
                        context.getResources().getString(R.string.app_download_progress), "100");
                Preference.putString(context, context.getResources().getString(
                        R.string.app_install_status), Constants.AppState.DOWNLOAD_COMPLETED);
            }
        }).start();
    }

}
