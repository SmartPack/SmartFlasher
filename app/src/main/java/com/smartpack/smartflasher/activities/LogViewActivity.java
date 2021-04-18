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
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Flasher;
import com.smartpack.smartflasher.utils.Utils;

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 21, 2020
 */

public class LogViewActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logview);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSave = findViewById(R.id.save);
        MaterialTextView mLogView = findViewById(R.id.flasher_log);
        MaterialTextView mCancel = findViewById(R.id.cancel_button);
        mLogView.setText(Flasher.mFlashingResult.toString());
        mBack.setOnClickListener(v -> onBackPressed());
        mSave.setOnClickListener(v -> {
            Utils.create(Flasher.mFlashingResult.toString(), new File(Utils.getStorageDir(this), "/flasher_log-" +
                    Flasher.mZipName.replace(".zip", "")).getAbsolutePath());
            Utils.snackbar(mSave, getString(R.string.save_log_message, new File(Utils.getStorageDir(this), "/flasher_log-" +
                    Flasher.mZipName.replace(".zip", "")).getAbsolutePath()));
        });
        mCancel.setOnClickListener(v -> onBackPressed());
    }

}