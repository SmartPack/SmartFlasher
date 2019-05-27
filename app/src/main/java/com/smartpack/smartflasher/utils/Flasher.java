/*
 * Copyright (C) 2019-2020 sunilpaulmathew <sunil.kde@gmail.com>
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

import com.smartpack.smartflasher.utils.root.RootUtils;

import java.io.File;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 29, 2018
 */

public class Flasher {

    private static final String ZIPFILE_EXTRACTED = Utils.getInternalDataStorage() + "/flash/META-INF/com/google/android/update-binary";

    private static final String FLASH_FOLDER = Utils.getInternalDataStorage() + "/flash";

    private static final String RECOVERY = "/cache/recovery/";

    public enum FLASHMENU {
        FLASH
    }

    public static boolean isZIPFileExtracted() {
        return Utils.existFile(ZIPFILE_EXTRACTED);
    }

    public static boolean hasFlashFolder() {
        return Utils.existFile(FLASH_FOLDER);
    }

    public static boolean hasRecovery() {
        return Utils.existFile(RECOVERY);
    }

    public static void makeInternalStorageFolder() {
        File file = new File(Utils.getInternalDataStorage());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }

    public static void makeFlashFolder() {
        RootUtils.runCommand("mkdir " + FLASH_FOLDER);
    }

    public static void cleanFlashFolder() {
        RootUtils.runCommand("rm -r " + FLASH_FOLDER + "/*");
    }

    public static void fileSize (File file) {
        float size = file.length();
        return;
    }

    public static void manualFlash(File file) {
        String path = file.toString();
        String flashFolder = Utils.getInternalDataStorage() + "/flash";
        String RECOVERY_API = "3";
        String CleanUpCommand = "rm -r '" + flashFolder + "'/*";
        /*
         * Flashing recovery zip without rebooting to custom recovery
         * Credits to osm0sis @ xda-developers.com
         */
        makeInternalStorageFolder();
        if (Utils.existFile(flashFolder)) {
            RootUtils.runCommand(CleanUpCommand);
        } else {
            RootUtils.runCommand("mkdir '" + flashFolder + "'");
        }
        if (file.length() <= 100000000) {
            RootUtils.runCommand("unzip '" + path + "' -d '" + flashFolder + "'");
            if (isZIPFileExtracted()) {
                RootUtils.runCommand("cd '" + flashFolder + "' && mount -o remount,rw / && mkdir /tmp");
                RootUtils.runCommand("mke2fs -F tmp.ext4 250000 && mount -o loop tmp.ext4 /tmp/");
                RootUtils.runCommand("sh META-INF/com/google/android/update-binary '" + RECOVERY_API + "' 1 '" + path + "'");
                RootUtils.runCommand(CleanUpCommand);
            }
        }
    }
}