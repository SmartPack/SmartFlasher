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

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.smartpack.smartflasher.R;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 23, 2020
 */

public class FlashingActivity extends AppCompatActivity {

    private static AppCompatImageButton mSaveButton;
    private static AppCompatTextView mCancelButton;
    private static AppCompatTextView mFlashingHeading;
    private static AppCompatTextView mFlashingResult;
    private static AppCompatTextView mRebootButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashing);

        mCancelButton = findViewById(R.id.cancel_button);
        mFlashingHeading = findViewById(R.id.flashing_title);
        mFlashingResult = findViewById(R.id.output_text);
        mSaveButton = findViewById(R.id.save_button);
        mRebootButton = findViewById(R.id.reboot_button);
        mSaveButton.setOnClickListener(v -> {
            Utils.create("## Flasher log created by Smart Flasher\n\n" + Flasher.mFlashingResult.toString(),
                    Utils.getInternalDataStorage() + "/flasher_log");
            Utils.toast(getString(R.string.save_log_message, Utils.getInternalDataStorage() + "/flasher_log"), getApplicationContext());
        });
        mCancelButton.setOnClickListener(v -> {
            onBackPressed();
        });
        mRebootButton.setOnClickListener(v -> {
            onBackPressed();
            Utils.rebootCommand(this);
        });
        refreshStatus();
    }

    public void refreshStatus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(() -> {
                            if (Flasher.mFlashingResult != null) {
                                mFlashingResult.setText(Flasher.mFlashingResult.toString());
                                if (!Flasher.mFlashing) {
                                    mFlashingHeading.setText(R.string.flashing_finished);
                                    mCancelButton.setVisibility(View.VISIBLE);
                                    mSaveButton.setVisibility(View.VISIBLE);
                                    mRebootButton.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                } catch (InterruptedException ignored) {}
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (Flasher.mFlashing) return;
        super.onBackPressed();
    }

}