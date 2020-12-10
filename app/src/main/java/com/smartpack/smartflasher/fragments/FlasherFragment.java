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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.activities.FlashingActivity;
import com.smartpack.smartflasher.activities.UpdateChannelActivity;
import com.smartpack.smartflasher.utils.Flasher;
import com.smartpack.smartflasher.utils.KernelUpdater;
import com.smartpack.smartflasher.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 19, 2020
 */

public class FlasherFragment extends Fragment {

    private AppCompatImageButton mUpdateChannelMenu;
    private ArrayList <RecycleViewItem> mData = new ArrayList<>();
    private MaterialCardView mFrameInfo;
    private LinearLayout mProgressLayout;
    private MaterialCardView mRecyclerViewCard;
    private MaterialTextView mProgressText;
    private MaterialTextView mUpdateChannelSummary;
    private RecyclerView mRecyclerView;
    private String mPath;
    private View mRootView;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_flasher, container, false);

        mProgressLayout = mRootView.findViewById(R.id.progress_layout);
        mProgressText = mRootView.findViewById(R.id.progress_text);
        mUpdateChannelMenu = mRootView.findViewById(R.id.icon);
        AppCompatImageButton mInfoIcon = mRootView.findViewById(R.id.info_icon);
        AppCompatImageButton mMenuIcon = mRootView.findViewById(R.id.menu_icon);
        MaterialCardView mFlash = mRootView.findViewById(R.id.flash);
        mFrameInfo = mRootView.findViewById(R.id.frame_info);
        mRecyclerViewCard = mRootView.findViewById(R.id.recycler_view_card);
        LinearLayout mUpdateChannelURL = mRootView.findViewById(R.id.update_channel_layout);
        MaterialTextView mKernel = mRootView.findViewById(R.id.kernel_version);
        MaterialTextView mKernelSummary = mRootView.findViewById(R.id.kernel_version_summary);
        MaterialTextView mInfo = mRootView.findViewById(R.id.info);
        MaterialTextView mUpdateChannel = mRootView.findViewById(R.id.update_channel);
        mUpdateChannelSummary = mRootView.findViewById(R.id.update_channel_summary);

        if (Utils.isDarkTheme(requireActivity())) {
            mKernel.setTextColor(Utils.getThemeAccentColor(requireActivity()));
            mProgressText.setTextColor(Utils.getThemeAccentColor(requireActivity()));
            mUpdateChannel.setTextColor(Utils.getThemeAccentColor(requireActivity()));
            mInfoIcon.setColorFilter(Utils.getThemeAccentColor(requireActivity()));
        }

        mRecyclerViewCard.setVisibility(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ? View.GONE : View.VISIBLE);
        mFrameInfo.setVisibility(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ? View.VISIBLE : View.GONE);
        mUpdateChannelMenu.setVisibility(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ? View.GONE : View.VISIBLE);

        mUpdateChannelMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mUpdateChannelMenu);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.remove));
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.share));
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.update_check)).setCheckable(true)
                    .setChecked(Utils.getBoolean("update_check", false, getActivity()));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setMessage(getString(R.string.sure_question))
                                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                })
                                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                                    clearUpdateInfo(requireActivity());
                                })
                                .show();
                        break;
                    case 1:
                        Intent shareChannel = new Intent();
                        shareChannel.setAction(Intent.ACTION_SEND);
                        shareChannel.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                        shareChannel.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_channel_message,
                                Utils.read(requireActivity().getFilesDir().getPath() + "/updatechannel")));
                        shareChannel.setType("text/plain");
                        Intent shareIntent = Intent.createChooser(shareChannel, null);
                        startActivity(shareIntent);
                        break;
                    case 2:
                        if (Utils.getBoolean("update_check", false, getActivity())) {
                            Utils.saveBoolean("update_check", false, getActivity());
                        } else {
                            Utils.saveBoolean("update_check", true, getActivity());
                            Utils.snackbar(mRootView, getString(R.string.update_check_message, !KernelUpdater.getKernelName(requireActivity()).
                                    equals("Unavailable") ? KernelUpdater.getKernelName(requireActivity()) : "this"));
                        }
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        mMenuIcon.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mMenuIcon);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.update_channel_create));
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.documentation));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        Intent createUpdateChannel = new Intent(getActivity(), UpdateChannelActivity.class);
                        startActivity(createUpdateChannel);
                        break;
                    case 1:
                        Utils.launchUrl(mRootView, "https://smartpack.github.io/kerneldownloads/", getActivity());
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        mKernelSummary.setText(Utils.runAndGetOutput("uname -a"));
        mUpdateChannelSummary.setText(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ?
                getString(R.string.update_channel_summary) : KernelUpdater.getUpdateChannel(requireActivity()));
        mInfo.setText(getString(R.string.update_channel_info, Utils.getInternalDataStorage()));

        mUpdateChannelURL.setOnClickListener(v -> {
            if (!Utils.checkWriteStoragePermission(requireActivity())) {
                ActivityCompat.requestPermissions((Activity) requireActivity(), new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                Utils.snackbar(mRootView, requireActivity().getString(R.string.permission_denied_write_storage));
                return;
            }
            if (Utils.isNetworkUnavailable(requireActivity())) {
                Utils.snackbar(mRootView, requireActivity().getString(R.string.no_internet));
                return;
            }
            Utils.dialogEditText(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ?
                            getString(R.string.update_channel_summary) : KernelUpdater.getUpdateChannel(requireActivity()),
                    (dialogInterface, i) -> {
                    }, text -> {
                        if (text.isEmpty()) {
                            clearUpdateInfo(requireActivity());
                            Utils.snackbar(mRootView, requireActivity().getString(R.string.update_channel_empty));
                            return;
                        }
                        if (text.equals(KernelUpdater.getUpdateChannel(requireActivity()))) {
                            return;
                        }
                        if (text.contains("/blob/")) {
                            text = text.replace("/blob/", "/raw/");
                        }
                        acquireUpdateInfo(text, requireActivity());
                    }, requireActivity()).setOnDismissListener(dialogInterface -> {
            }).show();
        });

        mFlash.setOnClickListener(v -> {
            if (!Utils.checkWriteStoragePermission(requireActivity())) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                Utils.snackbar(mRootView, getString(R.string.permission_denied_write_storage));
                return;
            }

            Intent manualflash = new Intent(Intent.ACTION_GET_CONTENT);
            manualflash.setType("application/*");
            startActivityForResult(manualflash, 0);
        });

        mData.clear();
        mData.add(new RecycleViewItem(getString(R.string.kernel_latest), KernelUpdater.getLatestVersion(requireActivity())));
        mData.add(new RecycleViewItem(getString(R.string.change_logs), getString(R.string.change_logs_summary)));
        mData.add(new RecycleViewItem(getString(R.string.download), getString(R.string.get_it_summary)));
        mData.add(new RecycleViewItem(getString(R.string.support), getString(R.string.support_summary)));
        mData.add(new RecycleViewItem(getString(R.string.donations), getString(R.string.donations_summary)));

        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), Utils.getSpanCount(requireActivity())));
        RecycleViewAdapter mRecycleViewAdapter = new RecycleViewAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (position == 1) {
                if (KernelUpdater.getChangeLog(requireActivity()).contains("https://") ||
                        KernelUpdater.getChangeLog(requireActivity()).contains("http://")) {
                    Utils.launchUrl(mRootView, KernelUpdater.getChangeLog(requireActivity()), getActivity());
                } else {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle(KernelUpdater.getKernelName(requireActivity()) + " " + KernelUpdater.getLatestVersion(requireActivity()))
                            .setMessage(KernelUpdater.getChangeLog(requireActivity()))
                            .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                            })
                            .show();
                }
            } else if (position == 2) {
                if (!Utils.checkWriteStoragePermission(requireActivity())) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    Utils.snackbar(mRootView, getString(R.string.permission_denied_write_storage));
                    return;
                }
                if (Utils.isNetworkUnavailable(requireActivity())) {
                    Utils.snackbar(mRootView, getString(R.string.no_internet));
                    return;
                }
                downloadKernel();
            } else if (position == 3) {
                if (KernelUpdater.getSupport(requireActivity()).equals("Unavailable") || KernelUpdater.getSupport(requireActivity()).equals("")) {
                    Utils.snackbar(mRootView, getString(R.string.support_group_unknown));
                    return;
                }
                Utils.launchUrl(mRootView, KernelUpdater.getSupport(requireActivity()), getActivity());
            } else if (position == 4) {
                if (KernelUpdater.getDonationLink(requireActivity()).equals("Unavailable") || KernelUpdater.getDonationLink(requireActivity()).equals("")) {
                    Utils.snackbar(mRootView, getString(R.string.donations_unknown));
                    return;
                }
                Utils.launchUrl(mRootView, KernelUpdater.getDonationLink(requireActivity()), getActivity());
            }
        });

        return mRootView;
    }

    public void clearUpdateInfo(Context context) {
        Utils.delete(KernelUpdater.updateChannelInfo(context));
        Utils.delete(KernelUpdater.updateInfo(context));
        mUpdateChannelMenu.setVisibility(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ? View.GONE : View.VISIBLE);
        mUpdateChannelSummary.setText(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ?
                getString(R.string.update_channel_summary) : KernelUpdater.getUpdateChannel(requireActivity()));
        mFrameInfo.setVisibility(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ? View.VISIBLE : View.GONE);
        mRecyclerViewCard.setVisibility(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ? View.GONE : View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    public void acquireUpdateInfo(String value, Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mRecyclerView.setVisibility(View.GONE);
                mProgressText.setText(context.getString(R.string.acquiring));
                mProgressLayout.setVisibility(View.VISIBLE);
                clearUpdateInfo(context);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                KernelUpdater.updateInfo(value, context);
                KernelUpdater.updateChannel(value, context);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mUpdateChannelMenu.setVisibility(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ? View.GONE : View.VISIBLE);
                mUpdateChannelSummary.setText(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ?
                        getString(R.string.update_channel_summary) : KernelUpdater.getUpdateChannel(requireActivity()));
                mFrameInfo.setVisibility(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ? View.VISIBLE : View.GONE);
                mRecyclerViewCard.setVisibility(KernelUpdater.getKernelName(requireActivity()).equals("Unavailable") ? View.GONE : View.VISIBLE);
                if (KernelUpdater.getKernelName(context).equals("Unavailable")) {
                    new MaterialAlertDialogBuilder(context)
                            .setMessage(R.string.update_channel_invalid)
                            .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                            }).show();
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void downloadKernel() {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mRecyclerViewCard.setVisibility(View.GONE);
                mProgressText.setText(getString(R.string.downloading, KernelUpdater.getKernelName(requireActivity()) + "-" + KernelUpdater.getLatestVersion(requireActivity())) + "...");
                mProgressLayout.setVisibility(View.VISIBLE);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Flasher.prepareFolder(Utils.getInternalDataStorage());
                Utils.download(Utils.getInternalDataStorage() + "/Kernel.zip", KernelUpdater.getUrl(requireActivity()));
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerViewCard.setVisibility(View.VISIBLE);
                if (KernelUpdater.getChecksum(requireActivity()).equals("Unavailable") || !KernelUpdater.getChecksum(requireActivity()).equals("Unavailable") &&
                        Utils.getChecksum(Utils.getInternalDataStorage() + "/Kernel.zip").contains(KernelUpdater.getChecksum(requireActivity()))) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(getString(R.string.download_completed,
                                    KernelUpdater.getKernelName(requireActivity()) + "-" + KernelUpdater.getLatestVersion(requireActivity())))
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(getString(R.string.flash), (dialog, id) -> {
                                flashingTask(new File(Utils.getInternalDataStorage() + "/Kernel.zip"));
                            })
                            .show();
                } else {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(getString(R.string.download_failed))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.cancel), (dialog, id) -> {
                            })
                            .show();
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void flashingTask(File file) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Flasher.mFlashing = true;
                Flasher.mZipName = file.getName();
                Flasher.mFlashingResult = new StringBuilder();
                Flasher.mFlashingOutput = new ArrayList<>();
                Flasher.mFlashingResult.append("** Preparing to flash ").append(file.getName()).append("...\n\n");
                Flasher.mFlashingResult.append("** Path: '").append(file.toString()).append("'\n\n");
                Utils.delete(requireActivity().getCacheDir() + "/flash.zip");
                Intent flashingIntent = new Intent(getActivity(), FlashingActivity.class);
                startActivity(flashingIntent);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Flasher.mFlashingResult.append("** Copying '").append(file.getName()).append("' into temporary folder: ");
                Flasher.mFlashingResult.append(Utils.runAndGetError("cp '" + file.toString() + "' " + requireActivity().getCacheDir() + "/flash.zip"));
                Flasher.mFlashingResult.append(Utils.exist(requireActivity().getCacheDir() + "/flash.zip") ? "Done *\n\n" : "\n\n");
                Flasher.manualFlash(requireActivity());
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Flasher.mFlashing = false;
            }
        }.execute();
    }

    @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
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
            if (!mPath.endsWith(".zip")) {
                Utils.snackbar(mRootView, getString(R.string.wrong_extension, ".zip"));
                return;
            }
            if (requestCode == 0) {
                if (Flasher.fileSize(new File(mPath)) >= 100000000) {
                    Utils.snackbar(mRootView, getString(R.string.file_size_limit, Flasher.fileSize(new File(mPath)) / 1000000));
                }
                MaterialAlertDialogBuilder flashzip = new MaterialAlertDialogBuilder(requireActivity());
                flashzip.setMessage(getString(R.string.flash_question, new File(mPath).getName()));
                flashzip.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });
                flashzip.setPositiveButton(getString(R.string.flash), (dialogInterface, i) -> {
                    flashingTask(new File(mPath));
                });
                flashzip.show();
            }
        }
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private ArrayList<RecycleViewItem> data;

        private static ClickListener clickListener;

        public RecycleViewAdapter(ArrayList<RecycleViewItem> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_flasher, parent, false);
            return new ViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                holder.mTitle.setText(this.data.get(position).getTitle());
                if (Utils.isDarkTheme(holder.mTitle.getContext())) {
                    holder.mTitle.setTextColor(Utils.getThemeAccentColor(holder.mTitle.getContext()));
                }
                holder.mDescription.setText(this.data.get(position).getDescription());
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }

        @Override
        public int getItemCount() {
            return this.data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private MaterialTextView mTitle;
            private MaterialTextView mDescription;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                this.mTitle = view.findViewById(R.id.title);
                this.mDescription = view.findViewById(R.id.description);
            }

            @Override
            public void onClick(View view) {
                clickListener.onItemClick(getAdapterPosition(), view);
            }
        }

        public void setOnItemClickListener(ClickListener clickListener) {
            RecycleViewAdapter.clickListener = clickListener;
        }

        public interface ClickListener {
            void onItemClick(int position, View v);
        }
    }

    private static class RecycleViewItem implements Serializable {
        private String mTitle;
        private String mDescription;

        public RecycleViewItem(String title, String description) {
            this.mTitle = title;
            this.mDescription = description;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }
    }
}