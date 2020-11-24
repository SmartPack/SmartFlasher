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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Flasher;
import com.smartpack.smartflasher.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 23, 2020
 */

public class FlashingActivity extends AppCompatActivity {

    private AppCompatImageButton mSave;
    private LinearLayout mProgressLayout;
    private MaterialCardView mCancel;
    private MaterialCardView mLog;
    private MaterialCardView mReboot;
    private MaterialTextView mFlashingOutput;
    private MaterialTextView mTitle;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashing);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mSave = findViewById(R.id.save);
        mTitle = findViewById(R.id.title);
        mFlashingOutput = findViewById(R.id.output);
        mCancel = findViewById(R.id.cancel);
        mLog = findViewById(R.id.log);
        mReboot = findViewById(R.id.reboot);
        mProgressLayout = findViewById(R.id.flashing_progress);
        MaterialTextView mProgressText = findViewById(R.id.progress_text);
        mProgressText.setText(getString(R.string.flashing) + "...");
        refreshStatus();
        mBack.setOnClickListener(v -> onBackPressed());
        mSave.setOnClickListener(v -> {
            Utils.create(Flasher.mFlashingResult.toString(), Utils.getInternalDataStorage() + "/flasher_log-" +
                    Flasher.mZipName.replace(".zip", ""));
            Utils.snackbar(mSave, getString(R.string.save_log_message, Utils.getInternalDataStorage() + "/flasher_log-" +
                    Flasher.mZipName.replace(".zip", "")));
        });
        mCancel.setOnClickListener(v -> finish());
        mLog.setOnClickListener(v -> {
            Intent viewLog = new Intent(this, LogViewActivity.class);
            startActivity(viewLog);
            finish();
        });
        mReboot.setOnClickListener(v -> {
            Utils.reboot("", mProgressLayout, mProgressText, this);
            finish();
        });
    }

    public void refreshStatus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(() -> {
                            if (!Flasher.mFlashing) {
                                mProgressLayout.setVisibility(View.GONE);
                                mSave.setVisibility(View.VISIBLE);
                            }
                            mTitle.setText(Flasher.mFlashing ? getString(R.string.flashing) + "..." :
                                    Flasher.mFlashingOutput != null && !Flasher.mFlashingOutput.isEmpty() ?
                                            getString(R.string.flashing_finished) : getString(R.string.flashing_failed));
                            mFlashingOutput.setText(Flasher.mFlashing ? "" : Flasher.mFlashingOutput != null && !Flasher.mFlashingOutput.isEmpty() ?
                                    Flasher.mFlashingOutput : getString(R.string.flashing_failed));
                            mCancel.setVisibility(Flasher.mFlashingOutput != null && !Flasher.mFlashingOutput.isEmpty() ? View.VISIBLE : View.GONE);
                            if (Utils.exist(getFilesDir().getPath() + "/flasher_log")) {
                                mLog.setVisibility(Flasher.mFlashingOutput != null && !Flasher.mFlashingOutput.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                            mReboot.setVisibility(Flasher.mFlashingOutput != null && !Flasher.mFlashingOutput.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (Flasher.mFlashing) return;
        super.onBackPressed();
    }

}