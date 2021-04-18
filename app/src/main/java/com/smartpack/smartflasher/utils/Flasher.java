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
import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 29, 2018
 */

public class Flasher {

    public static String mZipName;

    public static StringBuilder mFlashingResult = null;

    public static List<String> mFlashingOutput;

    public static boolean mFlashing, mMagiskModule = false, mWritableRoot = true;

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

    public static String getOutput() {
        List<String> mData = new ArrayList<>();
        for (String line : Flasher.mFlashingOutput.toString().substring(1, Flasher.mFlashingOutput.toString().length()-1).replace(", ","\n").replace("ui_print","").split("\\r?\\n")) {
            if (!line.startsWith("progress")) {
                mData.add(line);
            }
        }
        return mData.toString().substring(1, mData.toString().length()-1).replace(", ","\n").replaceAll("(?m)^[ \t]*\r?\n", "");
    }

    public static void manualFlash(Context context) {
        /*
         * Flashing recovery zip without rebooting to custom recovery (Credits to osm0sis @ xda-developers.com)
         * Also include code from https://github.com/topjohnwu/Magisk/
         * Ref: https://github.com/topjohnwu/Magisk/blob/a848f10bba4f840248ecf314f7c9d55511d05a0f/app/src/main/java/com/topjohnwu/magisk/core/tasks/FlashZip.kt#L47
         */
        String mScriptPath = new File(Utils.getStorageDir(context), "flash/META-INF/com/google/android/update-binary").getAbsolutePath(),
                FLASH_FOLDER = new File(Utils.getStorageDir(context), "flash").getAbsolutePath(),
                CLEANING_COMMAND = "rm -r '" + FLASH_FOLDER + "'",
                mZipPath = context.getCacheDir() + "/flash.zip";
        String flashingCommand = "BOOTMODE=true sh " + mScriptPath + " dummy 1 " + mZipPath + " && echo success";;
        if (Utils.exist(FLASH_FOLDER)) {
            Utils.runCommand(CLEANING_COMMAND);
        } else {
            prepareFolder(FLASH_FOLDER);
        }
        mFlashingResult.append("** Extracting ").append(mZipName).append(" into working folder: ");
        Utils.runAndGetError(Utils.magiskBusyBox() + " unzip " + mZipPath + " -d '" + FLASH_FOLDER + "'");
        if (Utils.exist(mScriptPath)) {
            mFlashingResult.append(" Done *\n\n");
            mFlashingResult.append("** Checking recovery zip file: ");
            if (Utils.read(mScriptPath.replace("update-binary","updater-script")).equals("#MAGISK")) {
                mFlashingResult.append(" Magisk Module *\n\n");
                mMagiskModule = true;
            } else if (Utils.exist(new File(Utils.getStorageDir(context), "/flash/anykernel.sh").getAbsolutePath())) {
                mFlashingResult.append(" AnyKernel *\n\n");
            } else {
                mFlashingResult.append(" Unknown *\n\n");
            }
            mFlashingResult.append("** Preparing a recovery-like environment for flashing...\n\n");
            Utils.runCommand("cd '" + FLASH_FOLDER + "'");
            if (!mMagiskModule) {
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
            }
            mFlashingResult.append("** Flashing ").append(mZipName).append(" ...\n\n");
            Utils.runAndGetLiveOutput(flashingCommand, mFlashingOutput);
            mFlashingResult.append(mFlashingOutput.isEmpty() ? "Unfortunately, flashing " + mZipName + " failed due to some unknown reasons!" : getOutput().replace("\nsuccess",""));
        } else {
            mFlashingResult.append(" Failed *\n\n");
            mFlashingResult.append("** Flashing Failed *");
        }
        Utils.runCommand(CLEANING_COMMAND);
        Utils.delete(context.getCacheDir() + "/flash.zip");
        if (!mMagiskModule && mWritableRoot) {
            mFlashingResult.append("\n\n** Unmount Root filesystem: ");
            mFlashingResult.append(Utils.mount("ro", "/"));
            mFlashingResult.append(" Done *");
        }
        if (mMagiskModule) {
            mMagiskModule = false;
        }
    }

}