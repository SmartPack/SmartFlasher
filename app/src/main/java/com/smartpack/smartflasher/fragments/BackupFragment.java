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

package com.smartpack.smartflasher.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.OpenableColumns;

import androidx.core.app.ActivityCompat;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Flasher;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.utils.ViewUtils;
import com.smartpack.smartflasher.utils.root.RootUtils;
import com.smartpack.smartflasher.views.dialog.Dialog;
import com.smartpack.smartflasher.views.recyclerview.DescriptionView;
import com.smartpack.smartflasher.views.recyclerview.RecyclerViewItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on December 12, 2019
 */

public class BackupFragment extends RecyclerViewFragment {

    private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoader;

    private Dialog mItemOptionsDialog;
    private Dialog mSelectionMenu;
    private Dialog mDeleteDialog;

    private String mPath;

    @Override
    protected boolean showBottomFab() {
        return true;
    }

    @Override
    protected Drawable getBottomFabDrawable() {
        return getResources().getDrawable(R.drawable.ic_backup);
    }

    @Override
    public int getSpanCount() {
        int span = Utils.isTablet(requireActivity()) ? Utils.getOrientation(getActivity()) ==
                Configuration.ORIENTATION_LANDSCAPE ? 4 : 3 : Utils.getOrientation(getActivity()) ==
                Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        if (itemsSize() != 0 && span > itemsSize()) {
            span = itemsSize();
        }
        return span;
    }

    @Override
    protected void init() {
        super.init();

        if (Utils.checkWriteStoragePermission(requireActivity())) {
            if (!Flasher.hasBootPartitionInfo()) {
                Flasher.exportBootPartitionInfo();
            }
            if (!Flasher.hasRecoveryPartitionInfo()) {
                Flasher.exportRecoveryPartitionInfo();
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        if (mItemOptionsDialog != null) {
            mItemOptionsDialog.show();
        }
        if (mDeleteDialog != null) {
            mDeleteDialog.show();
        }
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        if (Utils.checkWriteStoragePermission(requireActivity())) {
            reload();
        }
    }

    private void reload() {
        if (mLoader == null) {
            getHandler().postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    clearItems();
                    mLoader = new AsyncTask<Void, Void, List<RecyclerViewItem>>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            showProgress();
                        }

                        @Override
                        protected List<RecyclerViewItem> doInBackground(Void... voids) {
                            List<RecyclerViewItem> items = new ArrayList<>();
                            if (Flasher.BootPartitionInfo() && !Flasher.emptyBootPartitionInfo() || Flasher.RecoveryPartitionInfo() && !Flasher.emptyRecoveryPartitionInfo()) {
                                List<RecyclerViewItem> backupPartitions = new ArrayList<>();
                                itemInit(backupPartitions);
                                if (backupPartitions.size() > 0) {
                                    items.addAll(backupPartitions);
                                } else {
                                    DescriptionView backup = new DescriptionView();
                                    backup.setDrawable(Utils.getColoredIcon(R.drawable.ic_info, requireActivity()));
                                    backup.setTitle(getString(R.string.nothing_found));
                                    backup.setSummary(getString(R.string.nothing_found_summary, Utils.getInternalDataStorage() + "/backup"));
                                    backup.setOnItemClickListener(item -> BackupOptions());

                                    items.add(backup);
                                }
                            }
                            return items;
                        }

                        @Override
                        protected void onPostExecute(List<RecyclerViewItem> items) {
                            super.onPostExecute(items);
                            for (RecyclerViewItem item : items) {
                                addItem(item);
                            }
                            hideProgress();
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    private void itemInit(List<RecyclerViewItem> items) {
        File file = new File(Flasher.getPath());
        if (file.exists()) {
            for (final String backup : Flasher.backupItems()) {
                final File image = new File(Flasher.backupPath() + "/" + backup);
                if (image.isFile()) {
                    DescriptionView descriptionView = new DescriptionView();
                    descriptionView.setDrawable(Utils.getColoredIcon(R.drawable.ic_img, requireActivity()));
                    descriptionView.setTitle(image.getName().replace(".img", ""));
                    descriptionView.setSummary((image.length() / 1024L / 1024L) + " MB");
                    descriptionView.setOnItemClickListener(item -> {
                        mItemOptionsDialog = new Dialog(requireActivity())
                                .setItems(getResources().getStringArray(R.array.backup_item_options),
                                        (dialogInterface, i) -> {
                                            switch (i) {
                                                case 0:
                                                    restoreOptions(image);
                                                    break;
                                                case 1:
                                                    delete(image);
                                                    break;
                                            }
                                        })
                                .setOnDismissListener(dialogInterface -> mItemOptionsDialog = null);
                        mItemOptionsDialog.show();
                    });

                    items.add(descriptionView);
                }
            }
        }
    }

    private void delete(final File file) {
        mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.sure_question),
                (dialogInterface, i) -> {
                }, (dialogInterface, i) -> {
                    file.delete();
                    reload();
                }, dialogInterface -> mDeleteDialog = null, getActivity());
        mDeleteDialog.show();
    }

    private void restoreOptions(final File file) {
        mSelectionMenu = new Dialog(requireActivity()).setItems(getResources().getStringArray(
                R.array.backup_items), (dialog, i) -> {
                    switch (i) {
                        case 0:
                            if (Flasher.emptyBootPartitionInfo() || !Flasher.BootPartitionInfo()) {
                                Utils.snackbar(getRootView(), getString(R.string.boot_partition_unknown));
                            } else {
                                flash_boot_partition(file);
                            }
                            break;
                        case 1:
                            if (Flasher.isABDevice()) {
                                Utils.snackbar(getRootView(), getString(R.string.ab_message));
                            } else if (Flasher.emptyRecoveryPartitionInfo() || !Flasher.RecoveryPartitionInfo()) {
                                Utils.snackbar(getRootView(), getString(R.string.recovery_partition_unknown));
                            } else {
                                flash_recovery_partition(file);
                            }
                            break;
                    }
                }).setOnDismissListener(dialog -> mSelectionMenu = null);
        mSelectionMenu.show();
    }

    @Override
    protected void postInit() {
        super.postInit();
    }

    @Override
    protected void onBottomFabClick() {
        super.onBottomFabClick();

        if (Utils.checkWriteStoragePermission(requireActivity())) {
            if (!Flasher.hasBootPartitionInfo()) {
                Flasher.exportBootPartitionInfo();
            }
            if (!Flasher.hasRecoveryPartitionInfo()) {
                Flasher.exportRecoveryPartitionInfo();
            }
            reload();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.snackbar(getRootView(), getString(R.string.permission_denied_write_storage));
            return;
        }

        mSelectionMenu = new Dialog(requireActivity()).setItems(getResources().getStringArray(
                R.array.flasher), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            BackupOptions();
                            break;
                        case 1:
                            FlashOptions();
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> mSelectionMenu = null);
        mSelectionMenu.show();
    }

    private void BackupOptions() {
        mSelectionMenu = new Dialog(requireActivity()).setItems(getResources().getStringArray(
                R.array.backup_items), (dialog, i) -> {
                    switch (i) {
                        case 0:
                            if (Flasher.emptyBootPartitionInfo() || !Flasher.BootPartitionInfo()) {
                                Utils.snackbar(getRootView(), getString(R.string.boot_partition_unknown));
                            } else {
                                backup_boot_partition();
                            }
                            break;
                        case 1:
                            if (Flasher.isABDevice()) {
                                Utils.snackbar(getRootView(), getString(R.string.ab_message));
                            } else if (Flasher.emptyRecoveryPartitionInfo() || !Flasher.RecoveryPartitionInfo()) {
                                Utils.snackbar(getRootView(), getString(R.string.recovery_partition_unknown));
                            } else {
                                backup_recovery_partition();
                            }
                            break;
                    }
                }).setOnDismissListener(dialog -> mSelectionMenu = null);
        mSelectionMenu.show();
    }

    private void FlashOptions() {
        mSelectionMenu = new Dialog(requireActivity()).setItems(getResources().getStringArray(
                R.array.flasher_items), (dialog, i) -> {
                    switch (i) {
                        case 0:
                            if (Flasher.emptyBootPartitionInfo() || !Flasher.BootPartitionInfo()) {
                                Utils.snackbar(getRootView(), getString(R.string.boot_partition_unknown));
                            } else {
                                Intent boot_img = new Intent(Intent.ACTION_GET_CONTENT);
                                boot_img.setType("*/*");
                                startActivityForResult(boot_img, 0);
                            }
                            break;
                        case 1:
                            if (Flasher.isABDevice()) {
                                Utils.snackbar(getRootView(), getString(R.string.ab_message));
                            } else if (Flasher.emptyRecoveryPartitionInfo() || !Flasher.RecoveryPartitionInfo()) {
                                Utils.snackbar(getRootView(), getString(R.string.recovery_partition_unknown));
                            } else {
                                Intent rec_img = new Intent(Intent.ACTION_GET_CONTENT);
                                rec_img.setType("*/*");
                                startActivityForResult(rec_img, 1);
                            }
                            break;
                    }
                }).setOnDismissListener(dialog -> mSelectionMenu = null);
        mSelectionMenu.show();
    }

    private void backup_boot_partition() {
        ViewUtils.dialogEditText(RootUtils.runAndGetOutput("uname -r"),
                (dialogInterface, i) -> {
                }, new ViewUtils.OnDialogEditTextListener() {
                    @SuppressLint({"StaticFieldLeak", "StringFormatInvalid"})
                    @Override
                    public void onClick(String text) {
                        if (text.isEmpty()) {
                            Utils.snackbar(getRootView(), getString(R.string.name_empty));
                            return;
                        }
                        if (!text.endsWith(".img")) {
                            text += ".img";
                        }
                        if (text.contains(" ")) {
                            text = text.replace(" ", "_");
                        }
                        if (Utils.existFile(Utils.getInternalDataStorage() + "/backup/" + text)) {
                            Utils.snackbar(getRootView(), getString(R.string.already_exists, text));
                            return;
                        }
                        final String path = text;
                        new AsyncTask<Void, Void, Void>() {
                            private ProgressDialog mProgressDialog;
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                mProgressDialog = new ProgressDialog(getActivity());
                                mProgressDialog.setMessage(getString(R.string.backup_message, (Flasher.isABDevice() ?
                                        getString(R.string.ab_partition) : getString(R.string.boot_partition))) +
                                        (" ") + Utils.getInternalDataStorage() + "/backup/");
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                            }
                            @Override
                            protected Void doInBackground(Void... voids) {
                                Flasher.backupBootPartition(path);
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                try {
                                    mProgressDialog.dismiss();
                                } catch (IllegalArgumentException ignored) {
                                }
                                reload();
                            }
                        }.execute();
                    }
                }, getActivity()).setOnDismissListener(dialogInterface -> {
                }).show();
    }

    private void backup_recovery_partition() {
        ViewUtils.dialogEditText("Recovery",
                (dialogInterface, i) -> {
                }, new ViewUtils.OnDialogEditTextListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onClick(String text) {
                        if (text.isEmpty()) {
                            Utils.snackbar(getRootView(), getString(R.string.name_empty));
                            return;
                        }
                        if (!text.endsWith(".img")) {
                            text += ".img";
                        }
                        if (text.contains(" ")) {
                            text = text.replace(" ", "_");
                        }
                        if (Utils.existFile(Utils.getInternalDataStorage() + "/backup/" + text)) {
                            Utils.snackbar(getRootView(), getString(R.string.already_exists, text));
                            return;
                        }
                        final String path = text;
                        new AsyncTask<Void, Void, Void>() {
                            private ProgressDialog mProgressDialog;
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                mProgressDialog = new ProgressDialog(getActivity());
                                mProgressDialog.setMessage(getString(R.string.backup_message, getString(R.string.recovery_partition)) +
                                        (" ") + Utils.getInternalDataStorage() + "/backup/");
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                            }
                            @Override
                            protected Void doInBackground(Void... voids) {
                                Flasher.backupRecoveryPartition(path);
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                try {
                                    mProgressDialog.dismiss();
                                } catch (IllegalArgumentException ignored) {
                                }
                                reload();
                            }
                        }.execute();
                    }
                }, getActivity()).setOnDismissListener(dialogInterface -> {
                }).show();
    }

    @SuppressLint("StaticFieldLeak")
    private void flash_boot_partition(final File file) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage(getString(R.string.flashing) + (" ") + file.getName());
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Flasher.flashBootPartition(file);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                ViewUtils.rebootDialog(getActivity());
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void flash_recovery_partition(final File file) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage(getString(R.string.flashing) + (" ") + file.getName());
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Flasher.flashRecoveryPartition(file);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                ViewUtils.rebootDialog(getActivity());
            }
        }.execute();
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            assert uri != null;
            File file = new File(Objects.requireNonNull(uri.getPath()));
            if (Utils.isDocumentsUI(uri)) {
                @SuppressLint("Recycle") Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mPath = Environment.getExternalStorageDirectory().toString() + "/Download/" +
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } else {
                mPath = Utils.getPath(file);
                if (!Utils.getExtension(mPath).equals("img")) {
                    Utils.snackbar(getRootView(), getString(R.string.wrong_extension, ".img"));
                    return;
                }
            }
            Dialog flashimg = new Dialog(requireActivity());
            flashimg.setIcon(R.mipmap.ic_launcher);
            flashimg.setTitle(getString(R.string.flasher));
            flashimg.setMessage(getString(R.string.sure_message, new File(mPath).getName()) + getString(R.string.flash_img_warning));
            flashimg.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
            });
            flashimg.setPositiveButton(getString(R.string.flash), (dialogInterface, i) -> {
                if (requestCode == 0) {
                    flash_boot_partition(new File(mPath));
                } else if (requestCode == 1) {
                    flash_recovery_partition(new File(mPath));
                }
            });
            flashimg.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoader != null) {
            mLoader.cancel(true);
        }
    }
    
}