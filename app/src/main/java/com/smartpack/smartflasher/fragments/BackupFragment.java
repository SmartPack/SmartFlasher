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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
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
import com.smartpack.smartflasher.utils.Backup;
import com.smartpack.smartflasher.utils.Utils;

import java.io.File;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 19, 2020
 */

public class BackupFragment extends Fragment {

    private AsyncTask<Void, Void, List<String>> mLoader;
    private Handler mHandler = new Handler();
    private LinearLayout mProgressLayout;
    private MaterialTextView mProgressText;
    private RecyclerView mRecyclerView;
    private RecycleViewAdapter mRecycleViewAdapter;
    private View mRootView;
    private String mPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_backup, container, false);

        mProgressLayout = mRootView.findViewById(R.id.progress_layout);
        mProgressText = mRootView.findViewById(R.id.progress_text);
        MaterialCardView mBackup = mRootView.findViewById(R.id.backup);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        if (Utils.isDarkTheme(requireActivity())) {
            mProgressText.setTextColor(Utils.getThemeAccentColor(requireActivity()));
        }

        mBackup.setOnClickListener(v -> {
            if (Utils.checkWriteStoragePermission(requireActivity())) {
                PopupMenu popupMenu = new PopupMenu(requireActivity(), mBackup);
                Menu menu = popupMenu.getMenu();
                SubMenu backup = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.backup));
                backup.add(Menu.NONE, 1, Menu.NONE, getString(R.string.boot_partition));
                backup.add(Menu.NONE, 2, Menu.NONE, getString(R.string.recovery_partition));
                SubMenu flash = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.flash));
                flash.add(Menu.NONE, 3, Menu.NONE, getString(R.string.boot_img));
                flash.add(Menu.NONE, 4, Menu.NONE, getString(R.string.recovery_img));
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 0:
                            break;
                        case 1:
                            if (!Backup.hasBootPartitionInfo()) {
                                Utils.snackbar(mRootView, getString(R.string.boot_partition_unknown));
                            } else {
                                backup_boot_partition();
                            }
                            break;
                        case 2:
                            if (Backup.isABDevice()) {
                                Utils.snackbar(mRootView, getString(R.string.ab_message));
                            } else if (!Backup.hasRecoveryPartitionInfo()) {
                                Utils.snackbar(mRootView, getString(R.string.recovery_partition_unknown));
                            } else {
                                backup_recovery_partition();
                            }
                            break;
                        case 3:
                            if (!Backup.hasBootPartitionInfo()) {
                                Utils.snackbar(mRootView, getString(R.string.boot_partition_unknown));
                            } else {
                                Intent boot_img = new Intent(Intent.ACTION_GET_CONTENT);
                                boot_img.setType("*/*");
                                startActivityForResult(boot_img, 0);
                            }
                            break;
                        case 4:
                            if (Backup.isABDevice()) {
                                Utils.snackbar(mRootView, getString(R.string.ab_message));
                            } else if (!Backup.hasRecoveryPartitionInfo()) {
                                Utils.snackbar(mRootView, getString(R.string.recovery_partition_unknown));
                            } else {
                                Intent rec_img = new Intent(Intent.ACTION_GET_CONTENT);
                                rec_img.setType("*/*");
                                startActivityForResult(rec_img, 1);
                            }
                            break;
                    }
                    return false;
                });
                popupMenu.show();
            } else {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                Utils.snackbar(mRootView, getString(R.string.permission_denied_write_storage));
            }
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), Utils.getSpanCount(requireActivity())));
        mRecycleViewAdapter = new RecycleViewAdapter(Backup.getData());
        if (Utils.checkWriteStoragePermission(requireActivity())) {
            mRecyclerView.setAdapter(mRecycleViewAdapter);
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mRecyclerView);
            Menu menu = popupMenu.getMenu();
            SubMenu backup = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.restore));
            backup.add(Menu.NONE, 1, Menu.NONE, getString(R.string.boot_partition));
            backup.add(Menu.NONE, 2, Menu.NONE, getString(R.string.recovery_partition));
            menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.delete));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        break;
                    case 1:
                        if (!Backup.hasBootPartitionInfo()) {
                            Utils.snackbar(mRootView, getString(R.string.boot_partition_unknown));
                        } else {
                            flash_boot_partition(new File(mRecycleViewAdapter.getData(position)));
                        }
                        break;
                    case 2:
                        if (Backup.isABDevice()) {
                            Utils.snackbar(mRootView, getString(R.string.ab_message));
                        } else if (!Backup.hasRecoveryPartitionInfo()) {
                            Utils.snackbar(mRootView, getString(R.string.recovery_partition_unknown));
                        } else {
                            flash_recovery_partition(new File(mRecycleViewAdapter.getData(position)));
                        }
                        break;
                    case 3:
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setMessage(R.string.sure_question)
                                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                })
                                .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                                    Utils.delete(mRecycleViewAdapter.getData(position));
                                    reload();
                                }).show();
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        return mRootView;
    }

    private void reload() {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mProgressLayout.setVisibility(View.VISIBLE);
                            mProgressText.setText(null);
                            mRecyclerView.setVisibility(View.GONE);
                            mRecyclerView.removeAllViews();
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewAdapter(Backup.getData());
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            mProgressLayout.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    private void backup_boot_partition() {
        Utils.dialogEditText(Utils.runAndGetOutput("uname -r"),
                (dialogInterface, i) -> {
                }, new Utils.OnDialogEditTextListener() {
                    @SuppressLint({"StringFormatInvalid", "StaticFieldLeak"})
                    @Override
                    public void onClick(String text) {
                        if (text.isEmpty()) {
                            Utils.snackbar(mRootView, getString(R.string.name_empty));
                            return;
                        }
                        if (!text.endsWith(".img")) {
                            text += ".img";
                        }
                        if (text.contains(" ")) {
                            text = text.replace(" ", "_");
                        }
                        if (Utils.exist(Utils.getInternalDataStorage() + "/backup/" + text)) {
                            Utils.snackbar(mRootView, getString(R.string.already_exists, text));
                            return;
                        }
                        final String path = text;
                        new AsyncTask<Void, Void, Void>() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                mRecyclerView.setVisibility(View.GONE);
                                mProgressText.setText(getString(R.string.backup_message, (Backup.isABDevice() ?
                                        getString(R.string.ab_partition) : getString(R.string.boot_partition))) +
                                        " " + Utils.getInternalDataStorage() + "/backup/");
                                mProgressLayout.setVisibility(View.VISIBLE);
                            }
                            @Override
                            protected Void doInBackground(Void... voids) {
                                Backup.backupBootPartition(path);
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                mProgressLayout.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                reload();
                            }
                        }.execute();
                    }
                }, getActivity()).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    private void backup_recovery_partition() {
        Utils.dialogEditText("Recovery",
                (dialogInterface, i) -> {
                }, new Utils.OnDialogEditTextListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onClick(String text) {
                        if (text.isEmpty()) {
                            Utils.snackbar(mRootView, getString(R.string.name_empty));
                            return;
                        }
                        if (!text.endsWith(".img")) {
                            text += ".img";
                        }
                        if (text.contains(" ")) {
                            text = text.replace(" ", "_");
                        }
                        if (Utils.exist(Utils.getInternalDataStorage() + "/backup/" + text)) {
                            Utils.snackbar(mRootView, getString(R.string.already_exists, text));
                            return;
                        }
                        final String path = text;
                        new AsyncTask<Void, Void, Void>() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                mRecyclerView.setVisibility(View.GONE);
                                mProgressText.setText(getString(R.string.backup_message, getString(R.string.recovery_partition)) +
                                        " " + Utils.getInternalDataStorage() + "/backup/");
                                mProgressLayout.setVisibility(View.VISIBLE);
                            }
                            @Override
                            protected Void doInBackground(Void... voids) {
                                Backup.backupRecoveryPartition(path);
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                mProgressLayout.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.VISIBLE);
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
            @SuppressLint("SetTextI18n")
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mRecyclerView.setVisibility(View.GONE);
                mProgressText.setText(getString(R.string.flashing) + " " + file.getName());
                mProgressLayout.setVisibility(View.VISIBLE);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Backup.flashBootPartition(file);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                rebootDialog();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void flash_recovery_partition(final File file) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mRecyclerView.setVisibility(View.GONE);
                mProgressText.setText(getString(R.string.flashing) + " " + file.getName());
                mProgressLayout.setVisibility(View.VISIBLE);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Backup.flashRecoveryPartition(file);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                rebootDialog();
            }
        }.execute();
    }

    private void rebootDialog() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setMessage(getString(R.string.reboot_dialog))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> {
                })
                .setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> {
                    Utils.reboot("", mProgressLayout, mProgressText, requireActivity());
                }).show();
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
                if (!mPath.endsWith(".img")) {
                    Utils.snackbar(mRootView, getString(R.string.wrong_extension, ".img"));
                    return;
                }
            }
            new MaterialAlertDialogBuilder(requireActivity())
                    .setMessage(getString(R.string.flash_question, new File(mPath).getName()) + getString(R.string.flash_img_warning))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.flash), (dialogInterface, i) -> {
                        if (requestCode == 0) {
                            flash_boot_partition(new File(mPath));
                        } else if (requestCode == 1) {
                            flash_recovery_partition(new File(mPath));
                        }
                    }).show();
        }
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private List<String> data;

        private static ClickListener clickListener;

        public RecycleViewAdapter (List<String> data){
            this.data = data;
        }

        @NonNull
        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_backup, parent, false);
            return new RecycleViewAdapter.ViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
            try {
                holder.mName.setText(new File(this.data.get(position)).getName().replace(".img", ""));
                if (Utils.isDarkTheme(holder.mName.getContext())) {
                    holder.mName.setTextColor(Utils.getThemeAccentColor(holder.mName.getContext()));
                    holder.mIcon.setColorFilter(Utils.getThemeAccentColor(holder.mIcon.getContext()));
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }

        @Override
        public int getItemCount() {
            return this.data.size();
        }

        public String getData(int position) {
            return data.get(position);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private AppCompatImageButton mIcon;
            private MaterialTextView mName;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                this.mIcon = view.findViewById(R.id.icon);
                this.mName = view.findViewById(R.id.name);
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

}