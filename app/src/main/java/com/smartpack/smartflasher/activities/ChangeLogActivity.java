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

package com.smartpack.smartflasher.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.smartflasher.BuildConfig;
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 21, 2020
 */

public class ChangeLogActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelog);

        AppCompatImageButton mBack = findViewById(R.id.back);
        MaterialTextView mChangeLog = findViewById(R.id.change_log);
        MaterialTextView mTitle = findViewById(R.id.app_title);
        MaterialTextView mCancel = findViewById(R.id.cancel_button);
        mTitle.setText(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        String change_log = null;
        try {
            change_log = new JSONObject(Objects.requireNonNull(Utils.readAssetFile(
                    this, "version.json"))).getString("fullChanges");
        } catch (JSONException ignored) {
        }
        mChangeLog.setText(change_log);
        mCancel.setOnClickListener(v -> {
            onBackPressed();
        });
        mBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

}