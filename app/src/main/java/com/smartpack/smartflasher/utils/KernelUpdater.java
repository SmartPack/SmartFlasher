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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 21, 2020
 */

public class KernelUpdater {

    public static void updateChannel(String value, Context context) {
        Utils.create(value, updateChannelInfo(context));
    }

    public static void updateInfo(String value, Context context) {
        Flasher.prepareFolder(Utils.getStorageDir(context).getAbsolutePath());
        Utils.download(updateInfo(context), value);
    }

    private static String getKernelInfo(Context context) {
        try {
            JSONObject obj = new JSONObject(Utils.read(updateInfo(context)));
            return (obj.getString("kernel"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    private static String getSupportInfo(Context context) {
        try {
            JSONObject obj = new JSONObject(Utils.read(updateInfo(context)));
            return (obj.getString("support"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getUpdateChannel(Context context) {
        if (Utils.exist(updateChannelInfo(context))) {
            return Utils.read(updateChannelInfo(context));
        } else {
            return "Unavailable";
        }
    }

    public static String getKernelName(Context context) {
        try {
            JSONObject obj = new JSONObject(getKernelInfo(context));
            return (obj.getString("name"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getLatestVersion(Context context) {
        try {
            JSONObject obj = new JSONObject(getKernelInfo(context));
            return (obj.getString("version"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getUrl(Context context) {
        try {
            JSONObject obj = new JSONObject(getKernelInfo(context));
            return (obj.getString("link"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getChecksum(Context context) {
        try {
            JSONObject obj = new JSONObject(getKernelInfo(context));
            return (obj.getString("sha1"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getChangeLog(Context context) {
        try {
            JSONObject obj = new JSONObject(getKernelInfo(context));
            return (obj.getString("changelog_url"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getSupport(Context context) {
        try {
            JSONObject obj = new JSONObject(getSupportInfo(context));
            return (obj.getString("link"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getDonationLink(Context context) {
        try {
            JSONObject obj = new JSONObject(getSupportInfo(context));
            return (obj.getString("donation"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String updateInfo(Context context) {
        return context.getFilesDir().getPath() + "/update_info";
    }

    public static String updateChannelInfo(Context context) {
        return context.getFilesDir().getPath() + "/updatechannel";
    }

    public static long lastModified(Context context) {
        return new File(updateInfo(context)).lastModified();
    }

}