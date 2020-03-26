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
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.root.RootFile;
import com.smartpack.smartflasher.utils.root.RootUtils;
import com.smartpack.smartflasher.views.dialog.Dialog;

import java.io.File;
import java.io.FileDescriptor;
import java.util.List;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 29, 2018
 */

public class Flasher {

    private static final String ZIPFILE_EXTRACTED = Utils.getInternalDataStorage() + "/flash/META-INF/com/google/android/update-binary";
    private static final String BOOT_PARTITION_INFO = Environment.getDataDirectory() + "/.boot_partition_info";
    private static final String RECOVERY_PARTITION_INFO = Environment.getDataDirectory() + "/.recovery_partition_info";
    private static final String FLASH_FOLDER = Utils.getInternalDataStorage() + "/flash";
    private static final String BACKUP_FOLDER = Utils.getInternalDataStorage() + "/backup";
    private static final String CLEANING_COMMAND = "rm -r '" + FLASH_FOLDER + "'";
    private static final String FLASHER_LOG = Utils.getInternalDataStorage() + "/flasher_log";

    private static StringBuilder mFlashingResult = null;

    private static String mountRootFS(String command) {
        return "mount -o remount," + command + " /";
    }

    private static boolean isUnzipAvailable() {
        return Utils.existFile("/system/bin/unzip") || Utils.existFile("/system/xbin/unzip");
    }

    private static String busyboxUnzip() {
        if (Utils.existFile("/sbin/.core/busybox/unzip")) {
            return "/sbin/.core/busybox/unzip";
        } else if (Utils.existFile("/sbin/.magisk/busybox/unzip")) {
            return "/sbin/.magisk/busybox/unzip";
        } else {
            return null;
        }
    }

    public static boolean hasBootPartitionInfo() {
        return Utils.existFile(BOOT_PARTITION_INFO);
    }

    public static boolean hasRecoveryPartitionInfo() {
        return Utils.existFile(RECOVERY_PARTITION_INFO);
    }

    static void makeInternalStorageFolder() {
        File file = new File(Utils.getInternalDataStorage());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }

    public static File backupPath() {
        return new File(BACKUP_FOLDER);
    }

    public static List<String> backupItems() {
        RootFile file = new RootFile(BACKUP_FOLDER);
        if (!file.exists()) {
            file.mkdir();
        }
        return file.list();
    }

    public static void backupBootPartition(String name) {
        makeInternalStorageFolder();
        if (!Utils.existFile(BACKUP_FOLDER)) {
            File bachupFolderPath = new File(BACKUP_FOLDER);
            if (bachupFolderPath.exists() && bachupFolderPath.isFile()) {
                bachupFolderPath.delete();
            }
            bachupFolderPath.mkdirs();
        }
        String bootPartition = Utils.getInternalDataStorage() + "/backup/" + name;
        String command = "dd if=" + findBootPartition() + " of=" + bootPartition;
        RootUtils.runCommand(command);
    }

    public static void backupRecoveryPartition(String name) {
        makeInternalStorageFolder();
        if (!Utils.existFile(BACKUP_FOLDER)) {
            File bachupFolderPath = new File(BACKUP_FOLDER);
            if (bachupFolderPath.exists() && bachupFolderPath.isFile()) {
                bachupFolderPath.delete();
            }
            bachupFolderPath.mkdirs();
        }
        String recoveryPartition = Utils.getInternalDataStorage() + "/backup/" + name;
        String command = "dd if=" + findRecoveryPartition() + " of=" + recoveryPartition;
        RootUtils.runCommand(command);
    }

    public static long fileSize(File file) {
        return file.length();
    }

    private static String manualFlash(File file) {
        /*
         * Flashing recovery zip without rebooting to custom recovery
         * Credits to osm0sis @ xda-developers.com
         */
        FileDescriptor fd = new FileDescriptor();
        int RECOVERY_API = 3;
        String flashingCommand = "sh '" + ZIPFILE_EXTRACTED + "' '" + RECOVERY_API + "' '" +
                fd + "' '" + file.toString() + "'";
        if (Utils.existFile(FLASH_FOLDER)) {
            RootUtils.runCommand(CLEANING_COMMAND);
        } else {
            RootUtils.runCommand("mkdir '" + FLASH_FOLDER + "'");
        }
        mFlashingResult.append("** Checking for unzip binary! ");
        if (isUnzipAvailable()) {
            mFlashingResult.append("Native binary available...\n");
            RootUtils.runCommand("unzip " + file.toString() + " -d '" + FLASH_FOLDER + "'");
        } else {
            mFlashingResult.append("BusyBox binary available...\n");
            RootUtils.runCommand(busyboxUnzip() + " " + file.toString() + " -d '" + FLASH_FOLDER + "'");
        }
        RootUtils.runCommand("unzip '" + file.toString() + "' -d '" + FLASH_FOLDER + "'");
        if (Utils.existFile(ZIPFILE_EXTRACTED)) {
            mFlashingResult.append("\n** Extracting ").append(file.getName()).append(" into working folder: Done *\n\n");
            mFlashingResult.append("** Preparing a recovery-like environment for flashing...\n\n");
            RootUtils.runCommand("cd '" + FLASH_FOLDER + "'");
            mFlashingResult.append("** Mounting root file system: Done *\n\n");
            RootUtils.runCommand(mountRootFS("rw"));
            RootUtils.runCommand("mkdir /tmp");
            mFlashingResult.append("** Preparing a temporary ext4 image and loop mounting to '/tmp': Done *\n\n");
            RootUtils.runCommand("mke2fs -F tmp.ext4 500000");
            Utils.mount("-o loop", "tmp.ext4", "/tmp/");
            mFlashingResult.append("\n** Flashing ").append(file.getName()).append(" ...\n");
            return RootUtils.runCommand(flashingCommand + " && " + CLEANING_COMMAND + " && " +
                    mountRootFS("ro"));
        } else {
            mFlashingResult.append("** Extracting zip file failed! *\n\n");
            mFlashingResult.append("** Flashing Failed! *\n** Reason: Necessary BusyBox binaries not available! *");
            return RootUtils.runCommand(CLEANING_COMMAND + " && " +
                    mountRootFS("ro"));
        }
    }

    public static void flashingTask(File file, Context context) {
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.flashing) + (" ") + file.getName());
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            protected String doInBackground(Void... voids) {
                if (mFlashingResult == null) {
                    mFlashingResult = new StringBuilder();
                } else {
                    mFlashingResult.setLength(0);
                }
                mFlashingResult.append("## Flasher log created by Smart Flasher\n\n");
                mFlashingResult.append("** Preparing to flash ").append(file.getName()).append("...\n\n");
                mFlashingResult.append("** Path: '").append(file.toString()).append("'\n\n");
                return manualFlash(file);
            }
            @SuppressLint("StaticFieldLeak")
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                boolean flashResult = s != null && !s.isEmpty();
                Dialog flashingResult = new Dialog(context);
                flashingResult.setIcon(R.mipmap.ic_launcher);
                flashingResult.setTitle(context.getString(R.string.last_flash));
                flashingResult.setCancelable(false);
                flashingResult.setMessage(mFlashingResult.toString() + (flashResult ? "\n" + s : ""));
                flashingResult.setNeutralButton(context.getString(R.string.save_log), (dialog, id) -> {
                    Utils.create(mFlashingResult.toString() + "\n" + s, FLASHER_LOG + "_" +
                            file.getName().replace(".zip", ""));
                    Utils.toast(context.getString(R.string.save_log_message, FLASHER_LOG + "_" + file.getName()
                            .replace(".zip", "")), context);
                });
                flashingResult.setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                });
                flashingResult.setPositiveButton(context.getString(R.string.reboot), (dialog, id) -> {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mProgressDialog = new ProgressDialog(context);
                            mProgressDialog.setMessage(context.getString(R.string.rebooting) + ("..."));
                            mProgressDialog.setCancelable(false);
                            mProgressDialog.show();
                        }
                        @Override
                        protected Void doInBackground(Void... voids) {
                            RootUtils.runCommand(Utils.prepareReboot());
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            try {
                                mProgressDialog.dismiss();
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }.execute();
                });
                flashingResult.show();
            }
        }.execute();
    }

    public static void flashBootPartition(File file) {
        String command = "dd if='" + file.toString() + "' of=" + findBootPartition();
        RootUtils.runCommand(command);
    }

    public static void flashRecoveryPartition(File file) {
        String command = "dd if='" + file.toString() + "' of=" + findRecoveryPartition();
        RootUtils.runCommand(command);
    }

    public static void exportBootPartitionInfo() {
        /*
         * Inspired from the "find_block()" function on Magisk by topjohnwu @ xda-developers.com
         * Ref: https://github.com/topjohnwu/Magisk/blob/074b1f8c61e0cd03aea152346ad233d2278354f4/scripts/util_functions.sh#L146
         */
        String Command = "echo $(find /dev/block/ -type l -iname boot$(getprop ro.boot.slot_suffix)) Created by Smart Flasher > " + BOOT_PARTITION_INFO;
        if (!hasBootPartitionInfo()) {
            RootUtils.runCommand(Command);
        }
    }

    public static boolean emptyBootPartitionInfo() {
        return Utils.readFile(BOOT_PARTITION_INFO).isEmpty();
    }

    public static boolean BootPartitionInfo() {
        return Utils.readFile(BOOT_PARTITION_INFO).contains("boot");
    }

    public static String findBootPartition() {
        String partitions = Utils.readFile(BOOT_PARTITION_INFO);
        int i = partitions.indexOf(' ');
        return partitions.substring(0, i);
    }

    public static void exportRecoveryPartitionInfo() {
        String Command = "echo $(find /dev/block/ -type l -iname recovery) Created by Smart Flasher > " + RECOVERY_PARTITION_INFO;
        if (!isABDevice() && !hasRecoveryPartitionInfo()) {
            RootUtils.runCommand(Command);
        }
    }

    public static boolean emptyRecoveryPartitionInfo() {
        return Utils.readFile(RECOVERY_PARTITION_INFO).isEmpty();
    }

    public static boolean RecoveryPartitionInfo() {
        return Utils.readFile(RECOVERY_PARTITION_INFO).contains("recovery");
    }

    public static String findRecoveryPartition() {
        String partitions = Utils.readFile(RECOVERY_PARTITION_INFO);
        int i = partitions.indexOf(' ');
        return partitions.substring(0, i);
    }

    public static boolean isABDevice() {
        return Utils.readFile(BOOT_PARTITION_INFO).contains("boot_a") || Utils.readFile(BOOT_PARTITION_INFO).contains("boot_b");
    }

    public static String getPath() {
        File file = new File(Utils.getInternalDataStorage() + "/backup/");
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
        return file.toString();
    }

}