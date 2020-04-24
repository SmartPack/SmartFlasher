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
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.Menu;

import androidx.core.app.ActivityCompat;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Flasher;
import com.smartpack.smartflasher.utils.Prefs;
import com.smartpack.smartflasher.utils.KernelUpdater;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.utils.root.RootUtils;
import com.smartpack.smartflasher.views.recyclerview.DescriptionView;
import com.smartpack.smartflasher.views.dialog.Dialog;
import com.smartpack.smartflasher.views.recyclerview.GenericSelectView;
import com.smartpack.smartflasher.views.recyclerview.RecyclerViewItem;
import com.smartpack.smartflasher.views.recyclerview.SwitchView;
import com.smartpack.smartflasher.views.recyclerview.TitleView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public class FlasherFragment extends RecyclerViewFragment {

    private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoader;
    private Dialog mItemOptionsDialog;
    private String mPath;

    @Override
    protected boolean showTopFab() {
        return true;
    }

    @Override
    protected Drawable getTopFabDrawable() {
        return getResources().getDrawable(R.drawable.ic_flash);
    }

    @Override
    protected void init() {
        super.init();

        addViewPagerFragment(DescriptionFragment.newInstance(getString(R.string.flasher),
                getString(R.string.flasher_summary)));
    }

    @Override
    public int getSpanCount() {
        return super.getSpanCount() + 1;
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        reload();
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
                            kernelinfoInit(items);
                            SmartPackInit(items);
                            return items;
                        }

                        @Override
                        protected void onPostExecute(List<RecyclerViewItem> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            for (RecyclerViewItem item : recyclerViewItems) {
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

    @Override
    protected void postInit() {
        super.postInit();
    }

    private void kernelinfoInit(List<RecyclerViewItem> items) {
        TitleView kernel = new TitleView();
        kernel.setText(!KernelUpdater.getKernelName().equals("Unavailable") ? KernelUpdater.getKernelName() :
                getString(R.string.kernel_information));

        items.add(kernel);

        DescriptionView kernelinfo = new DescriptionView();
        kernelinfo.setTitle(getString(R.string.kernel));
        kernelinfo.setSummary(RootUtils.runCommand("uname -r"));

        items.add(kernelinfo);

        GenericSelectView updateChannel = new GenericSelectView();
        updateChannel.setMenuIcon(getResources().getDrawable(R.drawable.ic_dots));
        updateChannel.setTitle(getString(R.string.update_channel));
        updateChannel.setValue((!KernelUpdater.getKernelName().equals("Unavailable"))
                ? KernelUpdater.getUpdateChannel() : getString(R.string.update_channel_summary));
        updateChannel.setOnGenericValueListener((genericSelectView, value) -> {
            if (!Utils.checkWriteStoragePermission(requireActivity())) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                Utils.toast(R.string.permission_denied_write_storage, getActivity());
                return;
            }
            if (Utils.networkUnavailable(requireActivity())) {
                Utils.toast(R.string.no_internet, getActivity());
                return;
            }
            if (value.isEmpty()) {
                KernelUpdater.clearUpdateInfo();
                Utils.toast(R.string.update_channel_empty, getActivity());
                reload();
                return;
            }
            if (value.equals(KernelUpdater.getUpdateChannel())) {
                return;
            }
            KernelUpdater.acquireUpdateInfo(value, getActivity());
            getHandler().postDelayed(() -> {
                updateChannel.setValue((!KernelUpdater.getKernelName().equals("Unavailable"))
                        ? KernelUpdater.getUpdateChannel() : getString(R.string.update_channel_summary));
            }, 100);
            reload();

        });
        if (!KernelUpdater.getKernelName().equals("Unavailable")) {
            updateChannel.setOnMenuListener((itemslist1, popupMenu) -> {
                Menu menu = popupMenu.getMenu();
                menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.remove));
                menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.share));
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 0:
                            new Dialog(requireActivity())
                                    .setMessage(getString(R.string.sure_question))
                                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                    })
                                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                                        KernelUpdater.clearUpdateInfo();
                                        reload();
                                    })
                                    .show();
                            break;
                        case 1:
                            Intent shareChannel = new Intent();
                            shareChannel.setAction(Intent.ACTION_SEND);
                            shareChannel.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                            shareChannel.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_channel_message,
                                    Utils.readFile(Utils.getInternalDataStorage() + "/update_channel")));
                            shareChannel.setType("text/plain");
                            Intent shareIntent = Intent.createChooser(shareChannel, null);
                            startActivity(shareIntent);
                            break;
                    }
                    return false;
                });
            });
        }

        items.add(updateChannel);

        if (KernelUpdater.getLatestVersion().equals("Unavailable")) {
            DescriptionView info = new DescriptionView();
            info.setDrawable(getResources().getDrawable(R.drawable.ic_info));
            info.setTitle(getString(R.string.update_channel_info, Utils.getInternalDataStorage()));
            info.setFullSpan(true);
            info.setOnItemClickListener(item ->
                    Utils.launchUrl("https://smartpack.github.io/kerneldownloads/", getActivity())
            );

            items.add(info);
        }

        if (!KernelUpdater.getLatestVersion().equals("Unavailable")) {
            DescriptionView latest = new DescriptionView();
            latest.setTitle(getString(R.string.kernel_latest));
            latest.setSummary(KernelUpdater.getLatestVersion());

            items.add(latest);
        }

        if (!KernelUpdater.getChangeLog().equals("Unavailable")) {
            DescriptionView changelogs = new DescriptionView();
            changelogs.setTitle(getString(R.string.change_logs));
            changelogs.setSummary(getString(R.string.change_logs_summary));
            changelogs.setOnItemClickListener(item -> {
                if (KernelUpdater.getChangeLog().contains("https://") ||
                        KernelUpdater.getChangeLog().contains("http://")) {
                    Utils.launchUrl(KernelUpdater.getChangeLog(), getActivity());
                } else {
                    new Dialog(requireActivity())
                            .setTitle(KernelUpdater.getKernelName() + " " + KernelUpdater.getLatestVersion())
                            .setMessage(KernelUpdater.getChangeLog())
                            .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                            })
                            .show();
                }
            });

            items.add(changelogs);
        }

        if (!KernelUpdater.getUrl().equals("Unavailable")) {
            DescriptionView download = new DescriptionView();
            download.setTitle(getString(R.string.download));
            download.setSummary(getString(R.string.get_it_summary));
            download.setOnItemClickListener(item -> {
                if (!Utils.checkWriteStoragePermission(requireActivity())) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    Utils.toast(R.string.permission_denied_write_storage, getActivity());
                    return;
                }
                if (Utils.networkUnavailable(requireActivity())) {
                    Utils.toast(getString(R.string.no_internet), getActivity());
                    return;
                }
                KernelUpdater.downloadKernel(getActivity());
            });

            items.add(download);
        }

        if (!KernelUpdater.getSupport().equals("Unavailable")) {
            DescriptionView support = new DescriptionView();
            support.setTitle(getString(R.string.support));
            support.setSummary(getString(R.string.support_summary));
            support.setOnItemClickListener(item ->
                    Utils.launchUrl(KernelUpdater.getSupport(), getActivity())
            );

            items.add(support);
        }

        if (!KernelUpdater.getLatestVersion().equals("Unavailable")) {
            DescriptionView donations = new DescriptionView();
            donations.setTitle(getString(R.string.donations));
            donations.setSummary(getString(R.string.donations_summary));
            donations.setOnItemClickListener(item -> {
                if (KernelUpdater.getDonationLink().equals("Unavailable")) {
                    Utils.toast(getString(R.string.donations_unknown), getActivity());
                    return;
                }
                Utils.launchUrl(KernelUpdater.getDonationLink(), getActivity());
            });

            items.add(donations);
        }

        if (!KernelUpdater.getKernelName().equals("Unavailable") && Utils.isDownloadBinaries()) {
            SwitchView update_check = new SwitchView();
            update_check.setSummary(getString(R.string.update_check));
            update_check.setChecked(Prefs.getBoolean("update_check", false, getActivity()));
            update_check.addOnSwitchListener((switchview, isChecked) -> {
                Prefs.saveBoolean("update_check", isChecked, getActivity());
                if (Prefs.getBoolean("update_check", true, getActivity())) {
                    Utils.toast(getString(R.string.update_check_message, !KernelUpdater.getKernelName().
                            equals("Unavailable") ? KernelUpdater.getKernelName() : "this"), getActivity());
                }
            });

            items.add(update_check);
        }
    }

    private void SmartPackInit(List<RecyclerViewItem> items) {
        TitleView bb = new TitleView();
        bb.setText(getString(R.string.busybox_installer));

        items.add(bb);

        DescriptionView busybox = new DescriptionView();
        busybox.setDrawable(getResources().getDrawable(R.drawable.ic_playstore));
        busybox.setSummary(getString(R.string.busybox_installer_summary));
        busybox.setFullSpan(true);
        busybox.setOnItemClickListener(item -> {
            Utils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.busyboxinstaller", getActivity());
        });
        items.add(busybox);
        
        TitleView other_options = new TitleView();
        other_options.setText(getString(R.string.other_options));
        items.add(other_options);

        DescriptionView rebootOptions = new DescriptionView();
        rebootOptions.setTitle(getString(R.string.reboot_options));
        rebootOptions.setSummary(getString(R.string.reboot_options_Summary));
        rebootOptions.setFullSpan(true);
        rebootOptions.setOnItemClickListener(item -> {
            mItemOptionsDialog = new Dialog(requireActivity())
                    .setItems(getResources().getStringArray(R.array.reboot_options),
                            (dialogInterface, i) -> {
                                switch (i) {
                                    case 0:
                                        new Dialog(requireActivity())
                                                .setMessage(getString(R.string.sure_question))
                                                .setNegativeButton(getString(R.string.cancel), (dialogInterfacei, ii) -> {
                                                })
                                                .setPositiveButton(getString(R.string.yes), (dialogInterfacei, ii) -> {
                                                    new Execute().execute(Utils.prepareReboot() + " -p");
                                                })
                                                .show();
                                        break;
                                    case 1:
                                        new Dialog(requireActivity())
                                                .setMessage(getString(R.string.sure_question))
                                                .setNegativeButton(getString(R.string.cancel), (dialogInterfacei, ii) -> {
                                                })
                                                .setPositiveButton(getString(R.string.yes), (dialogInterfacei, ii) -> {
                                                    new Execute().execute(Utils.prepareReboot());
                                                })
                                                .show();
                                        break;
                                    case 2:
                                        new Dialog(requireActivity())
                                                .setMessage(getString(R.string.sure_question))
                                                .setNegativeButton(getString(R.string.cancel), (dialogInterfacei, ii) -> {
                                                })
                                                .setPositiveButton(getString(R.string.yes), (dialogInterfacei, ii) -> {
                                                    new Execute().execute(Utils.prepareReboot() + " recovery");
                                                })
                                                .show();
                                        break;
                                    case 3:
                                        new Dialog(requireActivity())
                                                .setMessage(getString(R.string.sure_question))
                                                .setNegativeButton(getString(R.string.cancel), (dialogInterfacei, ii) -> {
                                                })
                                                .setPositiveButton(getString(R.string.yes), (dialogInterfacei, ii) -> {
                                                    new Execute().execute(Utils.prepareReboot() + " bootloader");
                                                })
                                                .show();
                                        break;
                                }
                            })
                    .setOnDismissListener(dialogInterface -> mItemOptionsDialog = null);
            mItemOptionsDialog.show();
        });
        items.add(rebootOptions);
    }

    @Override
    protected void onTopFabClick() {
        super.onTopFabClick();

        if (!Utils.checkWriteStoragePermission(requireActivity())) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
            return;
        }

        Intent manualflash = new Intent(Intent.ACTION_GET_CONTENT);
        manualflash.setType("application/zip");
        startActivityForResult(manualflash, 0);
    }

    @SuppressLint("StaticFieldLeak")
    private class Execute extends AsyncTask<String, Void, Void> {
        private ProgressDialog mExecuteDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mExecuteDialog = new ProgressDialog(getActivity());
            mExecuteDialog.setMessage(getString(R.string.executing) + ("..."));
            mExecuteDialog.setCancelable(false);
            mExecuteDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            RootUtils.runCommand(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mExecuteDialog.dismiss();
        }
    }

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
            }
            if (requestCode == 0) {
                if (Flasher.fileSize(new File(mPath)) >= 100000000) {
                    Utils.toast(getString(R.string.file_size_limit, (Flasher.fileSize(new File(mPath)) / 1000000)), getActivity());
                }
                Dialog flashzip = new Dialog(requireActivity());
                flashzip.setIcon(R.mipmap.ic_launcher);
                flashzip.setTitle(getString(R.string.flasher));
                flashzip.setMessage(getString(R.string.sure_message, new File(mPath).getName()) +
                        getString(R.string.flasher_warning));
                flashzip.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });
                flashzip.setPositiveButton(getString(R.string.flash), (dialogInterface, i) -> {
                    Flasher.flashingTask(new File(mPath), getActivity());
                });
                flashzip.show();
            }
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