/*
 * Copyright (C) 2019-2020 sunilpaulmathew <sunil.kde@gmail.com>
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
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.smartpack.smartflasher.MainActivity;
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
    protected boolean showTopFab() {
        return true;
    }

    @Override
    protected Drawable getTopFabDrawable() {
        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_backup));
        DrawableCompat.setTint(drawable, Color.WHITE);
        return drawable;
    }

    @Override
    public int getSpanCount() {
        int span = Utils.isTablet(getActivity()) ? Utils.getOrientation(getActivity()) ==
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

        addViewPagerFragment(DescriptionFragment.newInstance(getString(R.string.backup),
                getString(R.string.backup_title)));

        if (Utils.checkWriteStoragePermission(getActivity())) {
            if (!Flasher.hasBootPartitionInfo()) {
                Flasher.exportBootPartitionInfo();
            }
            if (!Flasher.hasRecoveryPartitionInfo()) {
                Flasher.exportRecoveryPartitionInfo();
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        if (Flasher.BootPartitionInfo() && !Flasher.emptyBootPartitionInfo()) {
            addViewPagerFragment(DescriptionFragment.newInstance(Flasher.isABDevice() ? getString(R.string.ab_partition) : getString(R.string.boot_partition), Flasher.findBootPartition()));
        }
        if (!Flasher.isABDevice() && Flasher.RecoveryPartitionInfo() && !Flasher.emptyRecoveryPartitionInfo()) {
            addViewPagerFragment(DescriptionFragment.newInstance(
                    getString(R.string.recovery_partition), Flasher.findRecoveryPartition()));
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
        if (Utils.checkWriteStoragePermission(getActivity())) {
            reload();
        }
    }

    private void reload() {
        if (mLoader == null) {
            getHandler().postDelayed(new Runnable() {
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
                                    backup.setDrawable(getResources().getDrawable(R.drawable.ic_info));
                                    backup.setTitle(getString(R.string.nothing_found));
                                    backup.setSummary(getString(R.string.nothing_found_summary, Utils.getInternalDataStorage() + "/backup"));
                                    backup.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                                        @Override
                                        public void onClick(RecyclerViewItem item) {
                                            BackupOptions();
                                        }
                                    });

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
            for (final File image : file.listFiles()) {
                if (image.isFile()) {
                    DescriptionView descriptionView = new DescriptionView();
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_img));
                    descriptionView.setTitle(image.getName().replace(".img", ""));
                    descriptionView.setSummary((image.length() / 1024L / 1024L) + " MB");
                    descriptionView.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                        @Override
                        public void onClick(RecyclerViewItem item) {
                            mItemOptionsDialog = new Dialog(getActivity())
                                    .setItems(getResources().getStringArray(R.array.backup_item_options),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    switch (i) {
                                                        case 0:
                                                            restoreOptions(image);
                                                            break;
                                                        case 1:
                                                            delete(image);
                                                            break;
                                                    }
                                                }
                                            })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            mItemOptionsDialog = null;
                                        }
                                    });
                            mItemOptionsDialog.show();
                        }
                    });

                    items.add(descriptionView);
                }
            }
        }
    }

    private void delete(final File file) {
        mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.sure_question),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        file.delete();
                        reload();
                    }
                }, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mDeleteDialog = null;
                    }
                }, getActivity());
        mDeleteDialog.show();
    }

    private void restoreOptions(final File file) {
        mSelectionMenu = new Dialog(getActivity()).setItems(getResources().getStringArray(
                R.array.backup_items), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case 0:
                        if (Flasher.emptyBootPartitionInfo() || !Flasher.BootPartitionInfo()) {
                            Utils.toast(R.string.boot_partition_unknown, getActivity());
                        } else {
                            flash_boot_partition(file);
                        }
                        break;
                    case 1:
                        if (Flasher.isABDevice()) {
                            Utils.toast(R.string.ab_message, getActivity());
                        } else if (Flasher.emptyRecoveryPartitionInfo() || !Flasher.RecoveryPartitionInfo()) {
                            Utils.toast(R.string.recovery_partition_unknown, getActivity());
                        } else {
                            flash_recovery_partition(file);
                        }
                        break;
                }
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mSelectionMenu = null;
            }
        });
        mSelectionMenu.show();
    }

    @Override
    protected void postInit() {
        super.postInit();
    }

    @Override
    protected void onTopFabClick() {
        super.onTopFabClick();

        if (Utils.checkWriteStoragePermission(getActivity())) {
            if (!Flasher.hasBootPartitionInfo()) {
                Flasher.exportBootPartitionInfo();
            }
            if (!Flasher.hasRecoveryPartitionInfo()) {
                Flasher.exportRecoveryPartitionInfo();
            }
            reload();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
            return;
        }

        mSelectionMenu = new Dialog(getActivity()).setItems(getResources().getStringArray(
                R.array.flasher), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        BackupOptions();
                        break;
                    case 1:
                        FlashOptions();
                        break;
                }
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mSelectionMenu = null;
            }
        });
        mSelectionMenu.show();
    }

    private void BackupOptions() {
        mSelectionMenu = new Dialog(getActivity()).setItems(getResources().getStringArray(
                R.array.backup_items), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case 0:
                        if (Flasher.emptyBootPartitionInfo() || !Flasher.BootPartitionInfo()) {
                            Utils.toast(R.string.boot_partition_unknown, getActivity());
                        } else {
                            backup_boot_partition();
                        }
                        break;
                    case 1:
                        if (Flasher.isABDevice()) {
                            Utils.toast(R.string.ab_message, getActivity());
                        } else if (Flasher.emptyRecoveryPartitionInfo() || !Flasher.RecoveryPartitionInfo()) {
                            Utils.toast(R.string.recovery_partition_unknown, getActivity());
                        } else {
                            backup_recovery_partition();
                        }
                        break;
                }
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mSelectionMenu = null;
            }
        });
        mSelectionMenu.show();
    }

    private void FlashOptions() {
        mSelectionMenu = new Dialog(getActivity()).setItems(getResources().getStringArray(
                R.array.flasher_items), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case 0:
                        if (Flasher.emptyBootPartitionInfo() || !Flasher.BootPartitionInfo()) {
                            Utils.toast(R.string.boot_partition_unknown, getActivity());
                        } else {
                            Intent boot_img = new Intent(Intent.ACTION_GET_CONTENT);
                            boot_img.setType("*/*");
                            startActivityForResult(boot_img, 0);
                        }
                        break;
                    case 1:
                        if (Flasher.isABDevice()) {
                            Utils.toast(R.string.ab_message, getActivity());
                        } else if (Flasher.emptyRecoveryPartitionInfo() || !Flasher.RecoveryPartitionInfo()) {
                            Utils.toast(R.string.recovery_partition_unknown, getActivity());
                        } else {
                            Intent rec_img = new Intent(Intent.ACTION_GET_CONTENT);
                            rec_img.setType("*/*");
                            startActivityForResult(rec_img, 1);
                        }
                        break;
                }
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mSelectionMenu = null;
            }
        });
        mSelectionMenu.show();
    }

    private void backup_boot_partition() {
        ViewUtils.dialogEditText(RootUtils.runCommand("uname -r"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }, new ViewUtils.OnDialogEditTextListener() {
                    @Override
                    public void onClick(String text) {
                        if (text.isEmpty()) {
                            Utils.toast(R.string.name_empty, getActivity());
                            return;
                        }
                        if (!text.endsWith(".img")) {
                            text += ".img";
                        }
                        if (text.contains(" ")) {
                            text = text.replace(" ", "_");
                        }
                        if (Utils.existFile(Utils.getInternalDataStorage() + "/backup/" + text)) {
                            Utils.toast(getString(R.string.already_exists, text), getActivity());
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
                }, getActivity()).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
            }
        }).show();
    }

    private void backup_recovery_partition() {
        ViewUtils.dialogEditText("Recovery",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }, new ViewUtils.OnDialogEditTextListener() {
                    @Override
                    public void onClick(String text) {
                        if (text.isEmpty()) {
                            Utils.toast(R.string.name_empty, getActivity());
                            return;
                        }
                        if (!text.endsWith(".img")) {
                            text += ".img";
                        }
                        if (text.contains(" ")) {
                            text = text.replace(" ", "_");
                        }
                        if (Utils.existFile(Utils.getInternalDataStorage() + "/backup/" + text)) {
                            Utils.toast(getString(R.string.already_exists, text), getActivity());
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
                }, getActivity()).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
            }
        }).show();
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            File file = new File(uri.getPath());
            mPath = Utils.getPath(file);
            if (Utils.isDocumentsUI(uri)) {
                Dialog dialogueDocumentsUI = new Dialog(getActivity());
                dialogueDocumentsUI.setMessage(getString(R.string.documentsui_message));
                dialogueDocumentsUI.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                });
                dialogueDocumentsUI.show();
                return;
            }
            if (!Utils.getExtension(mPath).equals("img")) {
                Utils.toast(getString(R.string.wrong_extension, ".img"), getActivity());
                return;
            }
            Dialog flashimg = new Dialog(getActivity());
            flashimg.setIcon(R.mipmap.ic_launcher);
            flashimg.setTitle(getString(R.string.flasher));
            flashimg.setMessage(getString(R.string.sure_message, file.getName().replace("primary:", "")) + getString(R.string.flash_img_warning));
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
    public boolean onBackPressed() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return true;
    }
    
}