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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

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
import java.util.LinkedHashMap;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public class FlasherFragment extends RecyclerViewFragment {
    private boolean mPermissionDenied;

    private Dialog mSelectionMenu;
    private Dialog mFlashingDialog;
    private Dialog mFlashDialog;

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
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        SmartPackInit(items);
    }

    @Override
    protected void postInit() {
        super.postInit();

        addViewPagerFragment(DescriptionFragment.newInstance(getString(R.string.app_name),
                getString(R.string.flasher_summary)));
    }

    private void SmartPackInit(List<RecyclerViewItem> items) {

        // Request write access to internal storage
        if (RootUtils.rootAccess() && Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        String RebootCommand = "am broadcast android.intent.action.ACTION_SHUTDOWN && sync && echo 3 > /proc/sys/vm/drop_caches && sync && sleep 3 && reboot";

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
                    wipecache.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    wipecache.setPositiveButton(getString(R.string.wipe_cache), (dialog1, id1) -> {
                        new Execute().execute("echo --wipe_cache > /cache/recovery/command");
                        new Execute().execute(RebootCommand + " recovery");
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
                    wipedata.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    wipedata.setPositiveButton(getString(R.string.wipe_data), (dialog1, id1) -> {
                        new Execute().execute("echo --wipe_data > /cache/recovery/command");
                        new Execute().execute(RebootCommand + " recovery");
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
                    turnoff.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    turnoff.setPositiveButton(getString(R.string.turn_off), (dialog1, id1) -> {
                        new Execute().execute(RebootCommand + " -p");
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
                    reboot.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    reboot.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                        new Execute().execute(RebootCommand);
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
                    recoveryreboot.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    recoveryreboot.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                        new Execute().execute(RebootCommand + " recovery");
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
                    bootloaderreboot.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    bootloaderreboot.setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                        new Execute().execute(RebootCommand + " bootloader");
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
        if (mPermissionDenied) {
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
            return;
        }

        mSelectionMenu = new Dialog(getActivity()).setItems(getResources().getStringArray(
                R.array.flasher), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (RootUtils.rootAccess()) {
                    Utils.toast(R.string.file_size_limit, getActivity());
                    Intent manualflash  = new Intent(Intent.ACTION_GET_CONTENT);
                    manualflash.setType("application/zip");
                    startActivityForResult(manualflash, 0);
                } else {
                    Utils.toast(R.string.no_root_access, getActivity());
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

    private void showFlashingDialog(final File file) {
        final LinkedHashMap<String, Flasher.FLASHMENU> menu = getflashMenu();
        mFlashingDialog = new Dialog(getActivity()).setItems(menu.keySet().toArray(
                new String[menu.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Flasher.FLASHMENU flashmenu = menu.values().toArray(new Flasher.FLASHMENU[menu.size()])[i];
                if (file != null) {
                    manualFlash(flashmenu, file, true);
                }
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mFlashingDialog = null;
            }
        });
        mFlashingDialog.show();
    }

    private void manualFlash(final Flasher.FLASHMENU flashmenu, final File file, final boolean flashing) {
        mFlashDialog = ViewUtils.dialogBuilder(getString(R.string.sure_message, file.getName()) + ("\n\n") +
                getString(R.string.file_size_limit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new AsyncTask<Void, Void, Void>() {

                    private ProgressDialog mProgressDialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mProgressDialog = new ProgressDialog(getActivity());
                        mProgressDialog.setMessage(getString(R.string.flashing));
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
        }, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mFlashDialog = null;
            }
        }, getActivity());
        mFlashDialog.show();
    }

    @Override
    public void onPermissionDenied(int request) {
        super.onPermissionDenied(request);
        if (request == 0) {
            mPermissionDenied = true;
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
        }
    }

    private LinkedHashMap<String, Flasher.FLASHMENU> getflashMenu() {
        LinkedHashMap<String, Flasher.FLASHMENU> flashingMenu = new LinkedHashMap<>();
        flashingMenu.put(getString(R.string.flasher_message), Flasher.FLASHMENU.FLASH);
        return flashingMenu;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            File file = new File(uri.getPath());
            if (file.getAbsolutePath().contains("/document/raw:")) {
                showFlashingDialog(new File(file.getAbsolutePath().replace("/document/raw:", "")));
            } else if (file.getAbsolutePath().contains("/document/primary:")) {
                showFlashingDialog(new File(Environment.getExternalStorageDirectory() + ("/") + file.getAbsolutePath().replace("/document/primary:", "")));
            } else if (file.getAbsolutePath().contains("/document/")) {
                showFlashingDialog(new File(file.getAbsolutePath().replace("/document/", "/storage/").replace(":", "/")));
            } else {
                showFlashingDialog(new File(file.getAbsolutePath()));
                // Store absolute path format in app data folder
                RootUtils.runCommand("echo " + file.getAbsolutePath() + " > " + Utils.getInternalDataStorage() + "/flasher_log.txt");
            }
        }
    }
}