/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Smart Flasher, which is a simple app aimed to make flashing
 * recovery zip files much easier. Significant amount of code for this app has been from
 * Kernel Adiutor by Willi Ye <williye97@gmail.com>.
 *
 * Smart Flasher is a free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Smart Flasher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Smart Flasher. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.smartpack.smartflasher.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.smartflasher.BuildConfig;
import com.smartpack.smartflasher.R;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 07, 2020
 */

public class Utils {

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
    }

    /*
     * The following code is partly taken from https://github.com/SmartPack/SmartPack-Kernel-Manager
     * Ref: https://github.com/SmartPack/SmartPack-Kernel-Manager/blob/beta/app/src/main/java/com/smartpack/kernelmanager/utils/root/RootUtils.java
     */
    public static boolean rootAccess() {
        return Shell.rootAccess();
    }

    public static void runCommand(String command) {
        Shell.su(command).exec();
    }

    public static void runAndGetLiveOutput(String command, List<String> output) {
        Shell.su(command).to(output).exec();
    }

    @NonNull
    public static String runAndGetOutput(String command) {
        StringBuilder sb = new StringBuilder();
        try {
            List<String> outputs = Shell.su(command).exec().getOut();
            if (ShellUtils.isValidOutput(outputs)) {
                for (String output : outputs) {
                    sb.append(output).append("\n");
                }
            }
            return removeSuffix(sb.toString()).trim();
        } catch (Exception e) {
            return "";
        }
    }

    @NonNull
    public static String runAndGetError(String command) {
        StringBuilder sb = new StringBuilder();
        List<String> outputs = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        try {
            Shell.su(command).to(outputs, stderr).exec();
            outputs.addAll(stderr);
            if (ShellUtils.isValidOutput(outputs)) {
                for (String output : outputs) {
                    sb.append(output).append("\n");
                }
            }
            return removeSuffix(sb.toString()).trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static String removeSuffix(@Nullable String s) {
        if (s != null && s.endsWith("\n")) {
            return s.substring(0, s.length() - "\n".length());
        }
        return s;
    }

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/ViewUtils.java
     */

    public static int getThemeAccentColor(Context context) {
        return context.getResources().getColor(R.color.ColorBlue);
    }

    public interface OnDialogEditTextListener {
        void onClick(String text);
    }

    public static MaterialAlertDialogBuilder dialogEditText(String text, final DialogInterface.OnClickListener negativeListener,
                                                            final OnDialogEditTextListener onDialogEditTextListener,
                                                            Context context) {
        return dialogEditText(text, negativeListener, onDialogEditTextListener, -1, context);
    }

    public static MaterialAlertDialogBuilder dialogEditText(String text, final DialogInterface.OnClickListener negativeListener,
                                                            final OnDialogEditTextListener onDialogEditTextListener, int inputType,
                                                            Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setPadding(75, 75, 75, 75);

        final AppCompatEditText editText = new AppCompatEditText(context);
        editText.setGravity(Gravity.CENTER);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (text != null) {
            editText.append(text);
        }
        editText.setSingleLine(true);
        if (inputType >= 0) {
            editText.setInputType(inputType);
        }

        layout.addView(editText);

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context).setView(layout);
        if (negativeListener != null) {
            dialog.setNegativeButton(context.getString(R.string.cancel), negativeListener);
        }
        if (onDialogEditTextListener != null) {
            dialog.setPositiveButton(context.getString(R.string.ok), (dialog1, which)
                    -> onDialogEditTextListener.onClick(Objects.requireNonNull(editText.getText()).toString()))
                    .setOnDismissListener(dialog1 -> {
                        if (negativeListener != null) {
                            negativeListener.onClick(dialog1, 0);
                        }
                    });
        }
        return dialog;
    }

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/Prefs.java
     */
    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/Utils.java
     */

    public static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void initializeAppTheme(Context context) {
        if (isDarkTheme(context)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static File getStorageDir(Context context) {
        if (Build.VERSION.SDK_INT >= 29) {
            return context.getExternalFilesDir("");
        } else {
            return new File(Environment.getExternalStorageDirectory(), "Smart_Flasher");
        }
    }

    public static boolean isPackageInstalled(String id, Context context) {
        try {
            context.getPackageManager().getApplicationInfo(id, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static boolean isDonated(Context context) {
        return isPackageInstalled("com.smartpack.donate", context);
    }

    public static String read(String file) {
        return runAndGetOutput("cat '" + file + "'");
    }

    public static boolean exist(String file) {
        String output = runAndGetOutput("[ -e " + file + " ] && echo true");
        return !output.isEmpty() && output.equals("true");
    }

    public static String mount(String command, String mountPoint) {
        return runAndGetError("mount -o remount," + command + " " + mountPoint);
    }

    public static void download(String path, String url) {
        if (isMagiskBinaryExist("wget")) {
            runCommand(magiskBusyBox() + " wget -O " + path + " " + url);
        } else if (isMagiskBinaryExist("curl")) {
            runCommand(magiskBusyBox() + " curl -L -o " + path + " " + url);
        } else if (Utils.exist("/system/bin/curl") || Utils.exist("/system/bin/wget")) {
            runCommand((Utils.exist("/system/bin/curl") ?
                    "curl -L -o " : "wget -O ") + path + " " + url);
        } else {
            /*
             * Based on the following stackoverflow discussion
             * Ref: https://stackoverflow.com/questions/15758856/android-how-to-download-file-from-webserver
             */
            try (InputStream input = new URL(url).openStream();
                 OutputStream output = new FileOutputStream(path)) {
                byte[] data = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean isMagiskBinaryExist(String command) {
        return magiskBusyBox() != null && !runAndGetError("/data/adb/magisk/busybox " + command).contains("applet not found");
    }

    public static String magiskBusyBox() {
        if (Utils.exist("/data/adb/magisk/busybox")) {
            return "/data/adb/magisk/busybox";
        } else {
            return null;
        }
    }

    public static void create(String text, String path) {
        if (!path.startsWith("/storage/")) {
            runCommand("echo '" + text + "' > " + path);
        } else {
            try {
                File logFile = new File(path);
                logFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(logFile);
                OutputStreamWriter myOutWriter =
                        new OutputStreamWriter(fOut);
                myOutWriter.append(text);
                myOutWriter.close();
                fOut.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void delete(String path) {
        if (exist(path)) {
            runCommand(magiskBusyBox() + " rm -r " + path);
        }
    }

    public static String getChecksum(String path) {
        return runAndGetOutput("sha1sum " + path);
    }

    public static void copy(String source, String dest) {
        Utils.runCommand("cp " + source + " " + dest);
    }

    public static void snackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
        snackbar.show();
    }

    public static void launchUrl(String url, Activity activity) {
        if (isNetworkUnavailable(activity)) {
            snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.no_internet));
        } else {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                activity.startActivity(i);
            } catch (ActivityNotFoundException ignored) {
            }
        }
    }

    public static int getOrientation(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode() ?
                Configuration.ORIENTATION_PORTRAIT : activity.getResources().getConfiguration().orientation;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static int getSpanCount(Activity activity) {
        return isTablet(activity) ? getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ?
                4 : 3 : getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
    }

    public static boolean isNetworkUnavailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return (cm.getActiveNetworkInfo() == null) || !cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /*
     * Taken and used almost as such from the following stackoverflow discussion
     * Ref: https://stackoverflow.com/questions/7203668/how-permission-can-be-checked-at-runtime-without-throwing-securityexception
     */
    public static boolean checkWriteStoragePermission(Context context) {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static String readAssetFile(Context context, String file) {
        InputStream input = null;
        BufferedReader buf = null;
        try {
            StringBuilder s = new StringBuilder();
            input = context.getAssets().open(file);
            buf = new BufferedReader(new InputStreamReader(input));

            String str;
            while ((str = buf.readLine()) != null) {
                s.append(str).append("\n");
            }
            return s.toString().trim();
        } catch (IOException ignored) {
        } finally {
            try {
                if (input != null) input.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String rebootCommand() {
        return "am broadcast android.intent.action.ACTION_SHUTDOWN " + "&&" +
                " sync " + "&&" +
                " echo 3 > /proc/sys/vm/drop_caches " + "&&" +
                " sync " + "&&" +
                " sleep 3 " + "&&" +
                " reboot";
    }

    public static void reboot(String string, LinearLayout linearLayout, MaterialTextView textView, Context context) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                linearLayout.setVisibility(View.VISIBLE);
                textView.setText(context.getString(R.string.rebooting) + "...");
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.runCommand(rebootCommand() + string);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                linearLayout.setVisibility(View.GONE);
            }
        }.execute();
    }

    public static boolean languageDefault(Context context) {
        return !getBoolean("use_en", false, context)
                && !getBoolean("use_ko", false, context)
                && !getBoolean("use_am", false, context)
                && !getBoolean("use_fr", false, context)
                && !getBoolean("use_ru", false, context)
                && !getBoolean("use_it", false, context)
                && !getBoolean("use_pt", false, context)
                && !getBoolean("use_ch", false, context)
                && !getBoolean("use_el", false, context);
    }

    public static String getLanguage(Context context) {
        if (getBoolean("use_en", false, context)) {
            return "en_US";
        } else if (getBoolean("use_ko", false, context)) {
            return "ko";
        } else if (getBoolean("use_am", false, context)) {
            return "am";
        } else if (getBoolean("use_fr", false, context)) {
            return "fr";
        } else if (getBoolean("use_ru", false, context)) {
            return "ru";
        } else if (getBoolean("use_it", false, context)) {
            return "it";
        } else if (getBoolean("use_pt", false, context)) {
            return "pt";
        } else if (getBoolean("use_ch", false, context)) {
            return "zh";
        } else if (getBoolean("use_el", false, context)) {
            return "el";
        } else {
            return java.util.Locale.getDefault().getLanguage();
        }
    }

    public static void setLanguage(Context context) {
        Locale myLocale = new Locale(getLanguage(context));
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

}