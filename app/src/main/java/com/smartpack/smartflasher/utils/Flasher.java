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

import android.os.Environment;

import com.smartpack.smartflasher.utils.root.RootFile;
import com.smartpack.smartflasher.utils.root.RootUtils;

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
    public static String mZipName;
    public static String mFlashingOutput;

    public static StringBuilder mFlashingResult = null;

    public static boolean mFlashing;

    private static String mountRootFS(String command) {
        return "mount -o remount," + command + " /";
    }

    private static boolean isUnzipAvailable() {
        return Utils.existFile("/system/bin/unzip") || Utils.existFile("/system/xbin/unzip");
    }

    private static String BusyBoxPath() {
        if (Utils.existFile("/sbin/.magisk/busybox")) {
            return "/sbin/.magisk/busybox";
        } else if (Utils.existFile("/sbin/.core/busybox")) {
            return "/sbin/.core/busybox";
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

    public static void makeInternalStorageFolder(String path) {
        File file = new File(path);
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
        makeInternalStorageFolder(BACKUP_FOLDER);
        String bootPartition = Utils.getInternalDataStorage() + "/backup/" + name;
        String command = "dd if=" + findBootPartition() + " of=" + bootPartition;
        RootUtils.runCommand(command);
    }

    public static void backupRecoveryPartition(String name) {
        makeInternalStorageFolder(BACKUP_FOLDER);
        String recoveryPartition = Utils.getInternalDataStorage() + "/backup/" + name;
        String command = "dd if=" + findRecoveryPartition() + " of=" + recoveryPartition;
        RootUtils.runCommand(command);
    }

    public static long fileSize(File file) {
        return file.length();
    }

    public static void manualFlash() {
        /*
         * Flashing recovery zip without rebooting to custom recovery
         * Credits to osm0sis @ xda-developers.com
         */
        FileDescriptor fd = new FileDescriptor();
        int RECOVERY_API = 3;
        String path = "/data/local/tmp/flash.zip";
        String flashingCommand = "sh '" + ZIPFILE_EXTRACTED + "' '" + RECOVERY_API + "' '" +
                fd + "' '" + path + "'";
        if (Utils.existFile(FLASH_FOLDER)) {
            RootUtils.runCommand(CLEANING_COMMAND);
        }
        makeInternalStorageFolder(FLASH_FOLDER);
        mFlashingResult.append("** Checking for unzip binary: ");
        if (isUnzipAvailable()) {
            mFlashingResult.append("Available *\n\n");
        } else if (BusyBoxPath() != null) {
            mFlashingResult.append("Native Binary Unavailable *\nloop mounting BusyBox binaries to '/system/xbin' *\n\n");
            Utils.mount("-o --bind", BusyBoxPath(), "/system/xbin");
        } else {
            mFlashingResult.append("Unavailable *\n\n");
        }
        mFlashingResult.append("** Extracting ").append(mZipName).append(" into working folder: ");
        RootUtils.runAndGetError("unzip " + path + " -d '" + FLASH_FOLDER + "'");
        if (Utils.existFile(ZIPFILE_EXTRACTED)) {
            mFlashingResult.append(" Done *\n\n");
            mFlashingResult.append("** Preparing a recovery-like environment for flashing...\n");
            RootUtils.runCommand("cd '" + FLASH_FOLDER + "'");
            mFlashingResult.append(RootUtils.runAndGetError(mountRootFS("rw"))).append(" \n");
            mFlashingResult.append(RootUtils.runAndGetError("mkdir /tmp")).append(" \n");
            mFlashingResult.append(RootUtils.runAndGetError("mke2fs -F tmp.ext4 500000")).append(" \n");
            mFlashingResult.append(RootUtils.runAndGetError("mount -o loop tmp.ext4 /tmp/")).append(" \n\n");
            mFlashingResult.append("** Flashing ").append(mZipName).append(" ...\n\n");
            mFlashingOutput = RootUtils.runAndGetOutput(flashingCommand);
            mFlashingResult.append(mFlashingOutput.isEmpty() ? "Unfortunately, flashing " + mZipName + " failed due to some unknown reasons!" : mFlashingOutput);
        } else {
            mFlashingResult.append(" Failed *\n\n");
            mFlashingResult.append("** Flashing Failed *");
        }
        RootUtils.runCommand(CLEANING_COMMAND);
        Utils.delete("/data/local/tmp/flash.zip");
        RootUtils.runCommand(mountRootFS("ro"));
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