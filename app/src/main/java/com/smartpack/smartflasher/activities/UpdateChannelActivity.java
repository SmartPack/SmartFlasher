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

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Flasher;
import com.smartpack.smartflasher.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 30, 2020
 */

public class UpdateChannelActivity extends AppCompatActivity {
    private AppCompatEditText mKernelNameHint;
    private AppCompatEditText mKernelVersionHint;
    private AppCompatEditText mDownloadLinkHint;
    private AppCompatEditText mChangelogHint;
    private AppCompatEditText mSHA1Hint;
    private AppCompatEditText mSupportHint;
    private AppCompatEditText mDonationHint;
    private MaterialCardView mCardView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatechannel);

        mCardView = findViewById(R.id.updatechannel_card);
        AppCompatImageButton mBack = findViewById(R.id.back_button);
        mBack.setOnClickListener(v -> onBackPressed());
        AppCompatImageButton mSave = findViewById(R.id.save_button);
        mSave.setOnClickListener(v -> {
            if (Utils.checkWriteStoragePermission(this)) {
                if (mKernelNameHint.getText() != null && !mKernelNameHint.getText().toString().equals("")
                        && mKernelVersionHint.getText() != null && !mKernelVersionHint.getText().toString().equals("")
                        && mDownloadLinkHint.getText() != null && !mDownloadLinkHint.getText().toString().equals("")) {
                    saveUpdateChannel();
                } else {
                    Utils.snackbar(mCardView, getString(R.string.update_channel_create_abort));
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                Utils.snackbar(mCardView, getString(R.string.permission_denied_write_storage));
            }
        });
        MaterialTextView mClearAll = findViewById(R.id.clear_all);
        mClearAll.setOnClickListener(v -> {
            if (isTextEntered()) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.clear_all_summary) + " " + getString(R.string.sure_question))
                        .setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> {
                        })
                        .setPositiveButton(getString(R.string.yes), (dialog1, id1) -> {
                            clearAll();
                        })
                        .show();
            } else {
                Utils.snackbar(mCardView, getString(R.string.clear_all_abort_message));
            }
        });
        mKernelNameHint = findViewById(R.id.kernel_name_hint);
        mKernelVersionHint = findViewById(R.id.kernel_version_hint);
        mDownloadLinkHint = findViewById(R.id.download_link_hint);
        mChangelogHint = findViewById(R.id.changelog_hint);
        mSHA1Hint = findViewById(R.id.sha1_hint);
        mSupportHint = findViewById(R.id.support_hint);
        mDonationHint = findViewById(R.id.donation_link_hint);
    }

    private void clearAll() {
        mKernelNameHint.setText(null);
        mKernelVersionHint.setText(null);
        mDownloadLinkHint.setText(null);
        mChangelogHint.setText(null);
        mSHA1Hint.setText(null);
        mSupportHint.setText(null);
        mDonationHint.setText(null);
    }

    @SuppressLint("StringFormatInvalid")
    private void saveUpdateChannel() {
        Utils.dialogEditText(Objects.requireNonNull(mKernelNameHint.getText()).toString(),
                (dialogInterface, i) -> {
                }, text -> {
                    if (text.isEmpty()) {
                        Utils.snackbar(mCardView, getString(R.string.name_empty));
                        return;
                    }
                    if (!text.endsWith(".json")) {
                        text += ".json";
                    }
                    if (text.contains(" ")) {
                        text = text.replace(" ", "_");
                    }
                    if (Utils.exist(new File(Utils.getStorageDir(this), text).getAbsolutePath())) {
                        Utils.snackbar(mCardView, getString(R.string.already_exists, text));
                        return;
                    }
                    try {
                        JSONObject obj = new JSONObject();
                        JSONObject kernel = new JSONObject();
                        kernel.put("name", mKernelNameHint.getText());
                        kernel.put("version", mKernelVersionHint.getText());
                        kernel.put("link", mDownloadLinkHint.getText());
                        kernel.put("changelog_url", mChangelogHint.getText());
                        kernel.put("sha1", mSHA1Hint.getText());
                        obj.put("kernel", kernel);
                        JSONObject support = new JSONObject();
                        support.put("link", mSupportHint.getText());
                        support.put("donation", mDonationHint.getText());
                        obj.put("support", support);
                        Flasher.prepareFolder(Utils.getStorageDir(this).getAbsolutePath());
                        Utils.create(obj.toString(), new File(Utils.getStorageDir(this), text).getAbsolutePath());
                        Utils.snackbar(mCardView, getString(R.string.update_channel_create_success,
                                new File(Utils.getStorageDir(this), text).getAbsolutePath()));
                    } catch (JSONException ignored) {
                    }
                }, this).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    private boolean isTextEntered() {
        return mKernelNameHint.getText() != null && !mKernelNameHint.getText().toString().equals("")
                || mKernelVersionHint.getText() != null && !mKernelVersionHint.getText().toString().equals("")
                || mDownloadLinkHint.getText() != null && !mDownloadLinkHint.getText().toString().equals("")
                || mChangelogHint.getText() != null && !mChangelogHint.getText().toString().equals("")
                || mSHA1Hint.getText() != null && !mSHA1Hint.getText().toString().equals("")
                || mSupportHint.getText() != null && !mSupportHint.getText().toString().equals("")
                || mDonationHint.getText() != null && !mDonationHint.getText().toString().equals("");
    }

    @Override
    public void onBackPressed() {
        if (isTextEntered()) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.update_channel_create_warning) + " " + getString(R.string.sure_question))
                    .setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialog1, id1) -> {
                        super.onBackPressed();
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

}