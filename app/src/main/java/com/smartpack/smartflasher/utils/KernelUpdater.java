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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.root.RootUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 21, 2020
 */

public class KernelUpdater {

    private static final String UPDATE_CHANNEL = Utils.getInternalDataStorage() + "/update_channel";
    private static final String UPDATE_INFO = Utils.getInternalDataStorage() + "/update_info";

    private static void updateChannel(String value) {
        Utils.create(value, UPDATE_CHANNEL);
    }

    public static void updateInfo(String value) {
        Flasher.makeInternalStorageFolder(Utils.getInternalDataStorage());
        Utils.downloadFile(UPDATE_INFO, value);
    }

    public static void clearUpdateInfo() {
        Utils.delete(UPDATE_CHANNEL);
        Utils.delete(UPDATE_INFO);
    }

    private static String getKernelInfo() {
        try {
            JSONObject obj = new JSONObject(Utils.readFile(UPDATE_INFO));
            return (obj.getString("kernel"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    private static String getSupportInfo() {
        try {
            JSONObject obj = new JSONObject(Utils.readFile(UPDATE_INFO));
            return (obj.getString("support"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getUpdateChannel() {
        if (Utils.existFile(UPDATE_CHANNEL)) {
            return Utils.readFile(UPDATE_CHANNEL);
        } else {
            return "Unavailable";
        }
    }

    public static void acquireUpdateInfo(String value, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.acquiring));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                clearUpdateInfo();
                updateInfo(value);
                updateChannel(value);
                RootUtils.runCommand("sleep 1");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (getKernelName().equals("Unavailable")) {
                    Utils.toast(R.string.update_channel_invalid, context);
                }
            }
        }.execute();
    }

    public static String getKernelName() {
        try {
            JSONObject obj = new JSONObject(getKernelInfo());
            return (obj.getString("name"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getLatestVersion() {
        try {
            JSONObject obj = new JSONObject(getKernelInfo());
            return (obj.getString("version"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getUrl() {
        try {
            JSONObject obj = new JSONObject(getKernelInfo());
            return (obj.getString("link"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getChecksum() {
        try {
            JSONObject obj = new JSONObject(getKernelInfo());
            return (obj.getString("sha1"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getChangeLog() {
        try {
            JSONObject obj = new JSONObject(getKernelInfo());
            return (obj.getString("changelog_url"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getSupport() {
        try {
            JSONObject obj = new JSONObject(getSupportInfo());
            return (obj.getString("link"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static String getDonationLink() {
        try {
            JSONObject obj = new JSONObject(getSupportInfo());
            return (obj.getString("donation"));
        } catch (JSONException e) {
            return "Unavailable";
        }
    }

    public static long lastModified() {
        return new File(UPDATE_INFO).lastModified();
    }

}