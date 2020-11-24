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

import android.content.Context;

import java.io.File;
import java.io.FileDescriptor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 29, 2018
 */

public class Flasher {

    private static final String ZIPFILE_EXTRACTED = Utils.getInternalDataStorage() + "/flash/META-INF/com/google/android/update-binary";
    private static final String FLASH_FOLDER = Utils.getInternalDataStorage() + "/flash";
    private static final String CLEANING_COMMAND = "rm -r '" + FLASH_FOLDER + "'";
    public static String mZipName, mFlashingOutput;

    public static StringBuilder mFlashingResult = null;

    public static boolean mFlashing, mWritableRoot = true;

    public static void prepareFolder(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }

    private static boolean isWritableRoot() {
        return !Utils.mount("rw", "/").contains("' is read-only");
    }

    public static long fileSize(File file) {
        return file.length();
    }

    public static void manualFlash(Context context) {
        /*
         * Flashing recovery zip without rebooting to custom recovery
         * Credits to osm0sis @ xda-developers.com
         */
        FileDescriptor fd = new FileDescriptor();
        int RECOVERY_API = 3;
        String path = "/data/local/tmp/flash.zip";
        String flashingCommand = "sh '" + ZIPFILE_EXTRACTED + "' '" + RECOVERY_API + "' '" +
                fd + "' '" + path + "'";
        if (Utils.exist(FLASH_FOLDER)) {
            Utils.runCommand(CLEANING_COMMAND);
        } else {
            prepareFolder(FLASH_FOLDER);
        }
        mFlashingResult.append("** Extracting ").append(mZipName).append(" into working folder: ");
        Utils.runAndGetError(Utils.magiskBusyBox() + " unzip " + path + " -d '" + FLASH_FOLDER + "'");
        if (Utils.exist(ZIPFILE_EXTRACTED)) {
            mFlashingResult.append(" Done *\n\n");
            mFlashingResult.append("** Preparing a recovery-like environment for flashing...\n\n");
            Utils.runCommand("cd '" + FLASH_FOLDER + "'");
            mFlashingResult.append("** Mounting Root filesystem: ");
            if (!isWritableRoot()) {
                mWritableRoot = false;
                mFlashingResult.append("Failed *\nPlease Note: Flashing may not work properly on this device!\n\n");
            } else {
                mFlashingResult.append("Done *\n\n");
                mFlashingResult.append(Utils.runAndGetError(Utils.isMagiskBinaryExist("mkdir") ? Utils.magiskBusyBox() + " mkdir /tmp" : "mkdir /tmp")).append(" \n");
                mFlashingResult.append(Utils.runAndGetError(Utils.isMagiskBinaryExist("mke2fs") ? Utils.magiskBusyBox() + " mke2fs -F tmp.ext4 500000" : "mke2fs -F tmp.ext4 500000")).append(" \n");
                mFlashingResult.append(Utils.runAndGetError(Utils.isMagiskBinaryExist("mount") ? Utils.magiskBusyBox() + " mount -o loop tmp.ext4 /tmp/" : "mount -o loop tmp.ext4 /tmp/")).append(" \n\n");
            }
            // Remove latest log file
            Utils.delete(context.getFilesDir().getPath() + "/flasher_log");
            mFlashingResult.append("** Flashing ").append(mZipName).append(" ...\n\n");
            mFlashingOutput = Utils.runAndGetOutput(flashingCommand);
            mFlashingResult.append(mFlashingOutput.isEmpty() ? "Unfortunately, flashing " + mZipName + " failed due to some unknown reasons!" : mFlashingOutput);
        } else {
            mFlashingResult.append(" Failed *\n\n");
            mFlashingResult.append("** Flashing Failed *");
        }
        Utils.runCommand(CLEANING_COMMAND);
        Utils.delete("/data/local/tmp/flash.zip");
        if (mWritableRoot) {
            mFlashingResult.append("\n\n** Unmount Root filesystem: ");
            mFlashingResult.append(Utils.mount("ro", "/"));
            mFlashingResult.append(" Done *");
        }
        // Save latest log file
        Utils.create(Flasher.mFlashingResult.toString(), context.getFilesDir().getPath() + "/flasher_log");
    }

}