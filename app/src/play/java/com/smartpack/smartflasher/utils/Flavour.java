/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
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
import android.content.Context;
import android.content.Intent;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.activities.BillingActivity;

import java.util.ArrayList;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 21, 2021
 */
public class Flavour {

    public static void showDonateOption(Activity activity) {
        Intent donations = new Intent(activity, BillingActivity.class);
        activity.startActivity(donations);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void launchAppStore(ArrayList<RecycleViewItem> mData, Context context) {
        mData.add(new RecycleViewItem(context.getString(R.string.app_store), context.getString(R.string.app_store_summary),
                context.getResources().getDrawable(R.drawable.ic_app_store), "https://play.google.com/store/apps/details?id=com.smartpack.smartflasher"));
    }

}