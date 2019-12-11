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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Flasher;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.utils.ViewUtils;
import com.smartpack.smartflasher.utils.root.RootUtils;
import com.smartpack.smartflasher.views.recyclerview.CardView;
import com.smartpack.smartflasher.views.recyclerview.DescriptionView;
import com.smartpack.smartflasher.views.dialog.Dialog;
import com.smartpack.smartflasher.views.recyclerview.RecyclerViewItem;

import java.io.File;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public class FlasherFragment extends RecyclerViewFragment {

    private boolean mPermissionDenied;

    private Dialog mSelectionMenu;

    private String mPath;

    private String prepareReboot = "am broadcast android.intent.action.ACTION_SHUTDOWN " +
            "&& sync " +
            "&& echo 3 > /proc/sys/vm/drop_caches " +
            "&& sync " +
            "&& sleep 3 " +
            "&& reboot";

    @Override
    protected boolean showTopFab() {
        return true;
    }

    @Override
    protected Drawable getTopFabDrawable() {
        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_flash));
        DrawableCompat.setTint(drawable, Color.WHITE);
        return drawable;
    }

    @Override
    protected void init() {
        super.init();

        addViewPagerFragment(DescriptionFragment.newInstance(getString(R.string.app_name),
                getString(R.string.flasher_summary)));
        if (!Flasher.hasBootPartitionInfo()) {
            Flasher.exportBootPartitionInfo();
        }
        if (!Flasher.hasRecoveryPartitionInfo()) {
            Flasher.exportRecoveryPartitionInfo();
        }
        if (Flasher.BootPartitionInfo() && !Flasher.emptyBootPartitionInfo()) {
            addViewPagerFragment(DescriptionFragment.newInstance(Flasher.isABDevice() ? getString(R.string.ab_partition) : getString(R.string.boot_partition), Flasher.findBootPartition()));
        }
        if (!Flasher.isABDevice() && Flasher.RecoveryPartitionInfo() && !Flasher.emptyRecoveryPartitionInfo()) {
            addViewPagerFragment(DescriptionFragment.newInstance(
                    getString(R.string.recovery_partition), Flasher.findRecoveryPartition()));
        }
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        SmartPackInit(items);
        requestPermission(0, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void postInit() {
        super.postInit();
    }

    private void SmartPackInit(List<RecyclerViewItem> items) {

        CardView flasherCard = new CardView(getActivity());
        flasherCard.setTitle(getString(R.string.flasher_options));

        DescriptionView kernelinfo = new DescriptionView();
        kernelinfo.setTitle(getString(R.string.kernel) + (" ") + getString(R.string.version));
        if (RootUtils.rootAccess()) {
            kernelinfo.setSummary(RootUtils.runCommand("uname -r"));
        } else {
            kernelinfo.setSummary(getString(R.string.unavailable));
        }

        flasherCard.addItem(kernelinfo);

        DescriptionView lastflash = new DescriptionView();
        lastflash.setTitle(getString(R.string.last_flash));
        lastflash.setSummary(getString(R.string.last_flash_summary));
        lastflash.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (RootUtils.rootAccess()) {
                    if (Flasher.isPathLog() && Flasher.isFlashLog()) {
                        lastflash.setSummary(Utils.readFile(Utils.getInternalDataStorage() + "/last_flash.txt"));
                        Dialog flashLog = new Dialog(getActivity());
                        flashLog.setIcon(R.mipmap.ic_launcher);
                        flashLog.setTitle(getString(R.string.last_flash));
                        flashLog.setMessage(Utils.readFile(Utils.getInternalDataStorage() + "/flasher_log.txt"));
                        flashLog.setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                        });
                        flashLog.show();
                    } else {
                        lastflash.setSummary(getString(R.string.nothing_show));
                    }
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
                }
            }
        });

        flasherCard.addItem(lastflash);

        // Show wipe (Cache/Data) functions only if we recognize recovery...
        if (Flasher.hasRecovery()) {
            DescriptionView wipe_cache = new DescriptionView();
            wipe_cache.setTitle(getString(R.string.wipe_cache));
            wipe_cache.setSummary(getString(R.string.wipe_cache_summary));
            wipe_cache.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                @Override
                public void onClick(RecyclerViewItem item) {
                    Dialog wipecache = new Dialog(getActivity());
                    wipecache.setIcon(R.mipmap.ic_launcher);
                    wipecache.setTitle(getString(R.string.sure_question));
                    wipecache.setMessage(getString(R.string.wipe_cache_message));
                    wipecache.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    wipecache.setPositiveButton(getString(R.string.wipe_cache), (dialog1, id1) -> {
                        new Execute().execute("echo --wipe_cache > /cache/recovery/command");
                        new Execute().execute(prepareReboot + " recovery");
                    });
                    wipecache.show();
                }
            });
            flasherCard.addItem(wipe_cache);

            DescriptionView wipe_data = new DescriptionView();
            wipe_data.setTitle(getString(R.string.wipe_data));
            wipe_data.setSummary(getString(R.string.wipe_data_summary));
            wipe_data.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                @Override
                public void onClick(RecyclerViewItem item) {
                    Dialog wipedata = new Dialog(getActivity());
                    wipedata.setIcon(R.mipmap.ic_launcher);
                    wipedata.setTitle(getString(R.string.sure_question));
                    wipedata.setMessage(getString(R.string.wipe_data_message));
                    wipedata.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    wipedata.setPositiveButton(getString(R.string.wipe_data), (dialog1, id1) -> {
                        new Execute().execute("echo --wipe_data > /cache/recovery/command");
                        new Execute().execute(prepareReboot + " recovery");
                    });
                    wipedata.show();
                }
            });
            flasherCard.addItem(wipe_data);
        }

        DescriptionView turnoff = new DescriptionView();
        turnoff.setTitle(getString(R.string.turn_off));
        turnoff.setSummary(getString(R.string.turn_off_summary));
        turnoff.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (RootUtils.rootAccess()) {
                    Dialog turnoff = new Dialog(getActivity());
                    turnoff.setIcon(R.mipmap.ic_launcher);
                    turnoff.setTitle(getString(R.string.sure_question));
                    turnoff.setMessage(getString(R.string.turn_off_message));
                    turnoff.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    turnoff.setPositiveButton(getString(R.string.turn_off), (dialog1, id1) -> {
                        new Execute().execute(prepareReboot + " -p");
                    });
                    turnoff.show();
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
                }
            }
        });
        flasherCard.addItem(turnoff);

        DescriptionView reboot = new DescriptionView();
        reboot.setTitle(getString(R.string.reboot));
        reboot.setSummary(getString(R.string.reboot_summary));
        reboot.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (RootUtils.rootAccess()) {
                    Dialog reboot = new Dialog(getActivity());
                    reboot.setIcon(R.mipmap.ic_launcher);
                    reboot.setTitle(getString(R.string.sure_question));
                    reboot.setMessage(getString(R.string.normal_reboot_message));
                    reboot.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    reboot.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                        new Execute().execute(prepareReboot);
                    });
                    reboot.show();
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
                }
            }
        });
        flasherCard.addItem(reboot);

        DescriptionView recoveryreboot = new DescriptionView();
        recoveryreboot.setTitle(getString(R.string.reboot_recovery));
        recoveryreboot.setSummary(getString(R.string.reboot_recovery_summary));
        recoveryreboot.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (RootUtils.rootAccess()) {
                    Dialog recoveryreboot = new Dialog(getActivity());
                    recoveryreboot.setIcon(R.mipmap.ic_launcher);
                    recoveryreboot.setTitle(getString(R.string.sure_question));
                    recoveryreboot.setMessage(getString(R.string.reboot_recovery_message));
                    recoveryreboot.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    recoveryreboot.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                        new Execute().execute(prepareReboot + " recovery");
                    });
                    recoveryreboot.show();
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
                }
            }
        });
        flasherCard.addItem(recoveryreboot);

        DescriptionView bootloaderreboot = new DescriptionView();
        bootloaderreboot.setTitle(getString(R.string.reboot_bootloader));
        bootloaderreboot.setSummary(getString(R.string.reboot_bootloader_summary));
        bootloaderreboot.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (RootUtils.rootAccess()) {
                    Dialog bootloaderreboot = new Dialog(getActivity());
                    bootloaderreboot.setIcon(R.mipmap.ic_launcher);
                    bootloaderreboot.setTitle(getString(R.string.sure_question));
                    bootloaderreboot.setMessage(getString(R.string.reboot_bootloader_message));
                    bootloaderreboot.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    bootloaderreboot.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                        new Execute().execute(prepareReboot + " bootloader");
                    });
                    bootloaderreboot.show();
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
                }
            }
        });
        flasherCard.addItem(bootloaderreboot);

        if (flasherCard.size() > 0) {
            items.add(flasherCard);
        }
    }

    private class Execute extends AsyncTask<String, Void, Void> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.executing) + ("..."));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            RootUtils.runCommand(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onTopFabClick() {
        super.onTopFabClick();
        if (!RootUtils.rootAccess()) {
            Utils.toast(R.string.no_root_access, getActivity());
            return;
        }
        if (mPermissionDenied) {
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
            return;
        }
        if (!Flasher.hasBootPartitionInfo()) {
            Flasher.exportBootPartitionInfo();
        }
        if (!Flasher.hasRecoveryPartitionInfo()) {
            Flasher.exportRecoveryPartitionInfo();
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
                            Dialog boot = new Dialog(getActivity());
                            boot.setIcon(R.mipmap.ic_launcher);
                            boot.setTitle(getString(R.string.backup) + (" ") + (Flasher.isABDevice() ? getString(R.string.ab_partition) :
                                    getString(R.string.boot_partition)));
                            boot.setMessage(getString(R.string.backup_summary, (Flasher.isABDevice() ? getString(R.string.ab_partition) :
                                    getString(R.string.boot_partition))) + (" ") + Utils.getInternalDataStorage() + "/backup/");
                            boot.setNeutralButton(getString(R.string.cancel), (backupdialogInterface, ii) -> {
                            });
                            boot.setPositiveButton(getString(R.string.backup), (backupdialog, idi) -> {
                                backup_boot_partition();
                            });
                            boot.show();
                        }
                        break;
                    case 1:
                        if (Flasher.isABDevice()) {
                            Utils.toast(R.string.ab_message, getActivity());
                        } else if (Flasher.emptyRecoveryPartitionInfo() || !Flasher.RecoveryPartitionInfo()) {
                            Utils.toast(R.string.recovery_partition_unknown, getActivity());
                        } else {
                            Dialog recovery = new Dialog(getActivity());
                            recovery.setIcon(R.mipmap.ic_launcher);
                            recovery.setTitle(getString(R.string.backup) + (" ") + getString(R.string.recovery_partition));
                            recovery.setMessage(getString(R.string.backup_summary, getString(R.string.recovery_partition)) + (" ") + Utils.getInternalDataStorage() + "/backup/");
                            recovery.setNeutralButton(getString(R.string.cancel), (backupdialogInterface, ii) -> {
                            });
                            recovery.setPositiveButton(getString(R.string.backup), (backupdialog, idi) -> {
                                backup_recovery_partition();
                            });
                            recovery.show();
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
                    case 2:
                        Intent manualflash = new Intent(Intent.ACTION_GET_CONTENT);
                        manualflash.setType("application/zip");
                        startActivityForResult(manualflash, 2);
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
            }
        }.execute();
    }

    private void flash_zip_file(final File file) {
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
                Flasher.manualFlash(file);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }.execute();
    }

    @Override
    public void onPermissionDenied(int request) {
        super.onPermissionDenied(request);
        if (request == 0) {
            mPermissionDenied = true;
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
        }
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
            if (requestCode == 0 || requestCode == 1) {
                if (!file.getName().endsWith(".img")) {
                    Utils.toast(getString(R.string.wrong_extension, ".img"), getActivity());
                    return;
                }
                Dialog flashimg = new Dialog(getActivity());
                flashimg.setIcon(R.mipmap.ic_launcher);
                flashimg.setTitle(getString(R.string.flasher));
                flashimg.setMessage(getString(R.string.sure_message, file.getName()) + getString(R.string.flash_img_warning));
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
            } else if (requestCode == 2) {
                Flasher.cleanLogs();
                RootUtils.runCommand("echo '" + mPath + "' > " + Utils.getInternalDataStorage() + "/last_flash.txt");
                if (!file.getName().endsWith(".zip")) {
                    Utils.toast(getString(R.string.file_selection_error), getActivity());
                    return;
                }
                if (Flasher.fileSize(new File(mPath)) <= 100000000) {
                    Dialog flashzip = new Dialog(getActivity());
                    flashzip.setIcon(R.mipmap.ic_launcher);
                    flashzip.setTitle(getString(R.string.flasher));
                    flashzip.setMessage(getString(R.string.sure_message, file.getName()) +
                            getString(R.string.flasher_warning));
                    flashzip.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    flashzip.setPositiveButton(getString(R.string.flash), (dialogInterface, i) -> {
                        flash_zip_file(new File(mPath));
                    });
                    flashzip.show();
                } else {
                    Dialog flashSizeError = new Dialog(getActivity());
                    flashSizeError.setIcon(R.mipmap.ic_launcher);
                    flashSizeError.setTitle(getString(R.string.flasher));
                    flashSizeError.setMessage(getString(R.string.file_size_limit, file.getName()));
                    flashSizeError.setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    flashSizeError.show();
                }
            }
        }
    }
    
}