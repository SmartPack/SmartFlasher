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

public class Backup {

    public static String mBootPartitionInfo = null;
    public static String mRecoveryPartitionInfo = null;

    public static boolean hasBootPartitionInfo() {
        return mBootPartitionInfo != null && mBootPartitionInfo.contains("boot");
    }

    public static boolean hasRecoveryPartitionInfo() {
        return mRecoveryPartitionInfo != null && mRecoveryPartitionInfo.contains("recovery");
    }

    public static void prepareFolder(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }

    public static List<String> backupItems(Context context) {
        if (!Utils.exist(new File(Utils.getStorageDir(context), "backup").getAbsolutePath())) {
            prepareFolder(new File(Utils.getStorageDir(context), "backup").getAbsolutePath());
        }
        List<String> list = new ArrayList<>();
        String files = Utils.runAndGetOutput("ls '" + new File(Utils.getStorageDir(context), "backup").getAbsolutePath() + "/'");
        if (!files.isEmpty()) {
            // Make sure the files exists
            for (String file : files.split("\\r?\\n")) {
                if (file != null && !file.isEmpty() && Utils.exist(new File(Utils.getStorageDir(context), "backup/" + file).getAbsolutePath())) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        if (Utils.exist(new File(Utils.getStorageDir(context), "backup").getAbsolutePath())) {
            for (final String backupItem : backupItems(context)) {
                File mBackup = new File(Utils.getStorageDir(context), "backup/" + backupItem);
                if (mBackup.getName().endsWith(".img")) {
                    mData.add(mBackup.getAbsolutePath());
                }
            }
        }
        return mData;
    }

    public static void backupBootPartition(String name, Context context) {
        prepareFolder(new File(Utils.getStorageDir(context), "backup").getAbsolutePath());
        String bootPartition = new File(Utils.getStorageDir(context), "backup/"  + name).getAbsolutePath();
        String command = "dd if=" + findBootPartition() + " of=" + bootPartition;
        Utils.runCommand(command);
    }

    public static void backupRecoveryPartition(String name, Context context) {
        prepareFolder(new File(Utils.getStorageDir(context), "backup").getAbsolutePath());
        String recoveryPartition = new File(Utils.getStorageDir(context), "backup/"  + name).getAbsolutePath();
        String command = "dd if=" + findRecoveryPartition() + " of=" + recoveryPartition;
        Utils.runCommand(command);
    }

    public static void flashBootPartition(File file) {
        String command = "dd if='" + file.toString() + "' of=" + findBootPartition();
        Utils.runCommand(command);
    }

    public static void flashRecoveryPartition(File file) {
        String command = "dd if='" + file.toString() + "' of=" + findRecoveryPartition();
        Utils.runCommand(command);
    }

    public static String findBootPartition() {
        /*
         * Inspired from the "find_block()" function on Magisk by topjohnwu @ xda-developers.com
         * Ref: https://github.com/topjohnwu/Magisk/blob/074b1f8c61e0cd03aea152346ad233d2278354f4/scripts/util_functions.sh#L146
         */
        if (!hasBootPartitionInfo()) {
            mBootPartitionInfo = Utils.runAndGetOutput("find /dev/block/ -type l -iname boot$(getprop ro.boot.slot_suffix)") + " Created by Smart Flasher";
        } else {
            try {
                return mBootPartitionInfo.split("\\r?\\n")[0];
            } catch (StringIndexOutOfBoundsException | NullPointerException ignored) {
            }
        }
        return null;
    }

    public static String findRecoveryPartition() {
        if (!isABDevice() && !hasRecoveryPartitionInfo()) {
            mRecoveryPartitionInfo = Utils.runAndGetOutput("find /dev/block/ -type l -iname recovery") + " Created by Smart Flasher";
        } else {
            try {
                return mRecoveryPartitionInfo.split("\\r?\\n")[0];
            } catch (StringIndexOutOfBoundsException | NullPointerException ignored) {}
        }
        return null;
    }

    public static boolean isABDevice() {
        return mBootPartitionInfo.contains("boot_a") || mBootPartitionInfo.contains("boot_b");
    }

    public static String getPath(Context context) {
        File file = new File(Utils.getStorageDir(context), "backup");
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
        return file.toString();
    }

}