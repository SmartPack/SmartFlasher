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

package com.smartpack.smartflasher.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.ActionBar;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.BaseActivity;
import com.smartpack.smartflasher.fragments.RecyclerViewFragment;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.utils.ViewUtils;
import com.smartpack.smartflasher.utils.root.RootFile;
import com.smartpack.smartflasher.views.dialog.Dialog;
import com.smartpack.smartflasher.views.recyclerview.DescriptionView;
import com.smartpack.smartflasher.views.recyclerview.RecyclerViewItem;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class FilePickerActivity extends BaseActivity {

    public static final String PATH_INTENT = "path";
    public static final String EXTENSION_INTENT = "extension";
    public static final String RESULT_INTENT = "result";

    private String mPath;
    private String mExtension;
    private FilePickerFragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments);

        initToolBar();

        mPath = getIntent().getStringExtra(PATH_INTENT);
        mExtension = getIntent().getStringExtra(EXTENSION_INTENT);

        RootFile path = new RootFile(mPath);
        if (!path.exists() || !path.isDirectory()) {
            mPath = "/";
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mFragment
                = (FilePickerFragment) getFragment(), "fragment").commit();
    }

    private Fragment getFragment() {
        Fragment filePickerFragment = getSupportFragmentManager().findFragmentByTag("fragment");
        if (filePickerFragment == null) {
            filePickerFragment = FilePickerFragment.newInstance(mPath, mExtension);
        }
        return filePickerFragment;
    }

    @Override
    public void onBackPressed() {
        if (mFragment != null && !mFragment.mPath.equals("/")) {
            if (mFragment.mLoadAsyncTask == null) {
                mFragment.mPath = new RootFile(mFragment.mPath).getParentFile().toString();
                mFragment.reload();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        getSupportFragmentManager().beginTransaction().remove(getFragment()).commit();
        super.finish();
    }

    public static class FilePickerFragment extends RecyclerViewFragment {

        private String mPath;
        private String mExtension;
        private Drawable mDirImage;
        private Drawable mFileImage;
        private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoadAsyncTask;
        private Dialog mPickDialog;

        @Override
        protected boolean showViewPager() {
            return false;
        }

        public static FilePickerFragment newInstance(String path, String extension) {
            Bundle args = new Bundle();
            args.putString(PATH_INTENT, path);
            args.putString(EXTENSION_INTENT, extension);
            FilePickerFragment fragment = new FilePickerFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        protected void init() {
            super.init();
            if (mPath == null) {
                mPath = getArguments().getString(PATH_INTENT);
            }
            if (mExtension == null) {
                mExtension = getArguments().getString(EXTENSION_INTENT);
            }
            int accentColor = ViewUtils.getThemeAccentColor(getContext());
            if (mDirImage == null) {
                mDirImage = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_dir));
                DrawableCompat.setTint(mDirImage, accentColor);
            }
            if (mFileImage == null) {
                mFileImage = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_file));
                DrawableCompat.setTint(mFileImage, ViewUtils.getTextSecondaryColor(getContext()));
            }
            if (mPickDialog != null) {
                mPickDialog.show();
            }

            ActionBar actionBar;
            if ((actionBar = ((FilePickerActivity) getActivity()).getSupportActionBar()) != null) {
                actionBar.setTitle(mPath);
            }
        }

        @Override
        protected void addItems(List<RecyclerViewItem> items) {
            load(items);
        }

        @Override
        protected void postInit() {
            super.postInit();
            ActionBar actionBar;
            if ((actionBar = ((FilePickerActivity) getActivity()).getSupportActionBar()) != null) {
                actionBar.setTitle(mPath);
            }
        }

        private void reload() {
            if (mLoadAsyncTask == null) {
                mLoadAsyncTask = new AsyncTask<Void, Void, List<RecyclerViewItem>>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        clearItems();
                        showProgress();
                    }

                    @Override
                    protected List<RecyclerViewItem> doInBackground(Void... params) {
                        List<RecyclerViewItem> items = new ArrayList<>();
                        load(items);
                        return items;
                    }

                    @Override
                    protected void onPostExecute(List<RecyclerViewItem> items) {
                        super.onPostExecute(items);
                        for (RecyclerViewItem item : items) {
                            addItem(item);
                        }
                        hideProgress();
                        mLoadAsyncTask = null;

                        Activity activity = getActivity();
                        ActionBar actionBar;
                        if (activity != null && (actionBar = ((FilePickerActivity) activity)
                                .getSupportActionBar()) != null) {
                            actionBar.setTitle(mPath);
                        }
                    }
                };
                mLoadAsyncTask.execute();
            }
        }

        private void load(List<RecyclerViewItem> items) {
            RootFile path = new RootFile(mPath).getRealPath();
            mPath = path.toString();

            if (!path.isDirectory()) path = path.getParentFile();
            List<RootFile> dirs = new ArrayList<>();
            List<RootFile> files = new ArrayList<>();
            for (RootFile file : path.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file);
                } else {
                    files.add(file);
                }
            }

            final RootFile returnDir = new RootFile(mPath).getParentFile();
            if (returnDir.isDirectory()) {
                DescriptionView descriptionViewParent = new DescriptionView();
                descriptionViewParent.setSummary("..");
                descriptionViewParent.setDrawable(mDirImage);
                descriptionViewParent.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                    @Override
                    public void onClick(RecyclerViewItem item) {
                        mPath = returnDir.toString();
                        reload();
                    }
                });

                items.add(descriptionViewParent);
            }

            for (final RootFile dir : dirs) {
                DescriptionView descriptionView = new DescriptionView();
                descriptionView.setSummary(dir.getName());
                descriptionView.setDrawable(mDirImage);
                descriptionView.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                    @Override
                    public void onClick(RecyclerViewItem item) {
                        mPath = dir.toString();
                        reload();
                    }
                });

                items.add(descriptionView);
            }
            for (final RootFile file : files) {
                DescriptionView descriptionView = new DescriptionView();
                descriptionView.setSummary(file.getName());
                descriptionView.setDrawable(mFileImage);
                descriptionView.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                    @Override
                    public void onClick(RecyclerViewItem item) {
                        if (mExtension != null && !mExtension.isEmpty() && file.getName() != null
                                && !file.getName().endsWith(mExtension)) {
                            Utils.toast(getString(R.string.wrong_extension, mExtension), getActivity());
                        } else {
                            mPickDialog =
                                    ViewUtils.dialogBuilder(getString(R.string.select_question, file.getName()),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            }, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent();
                                                    intent.putExtra(RESULT_INTENT, file.toString());
                                                    getActivity().setResult(0, intent);
                                                    getActivity().finish();
                                                }
                                            }, new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    mPickDialog = null;
                                                }
                                            }, getActivity());
                            mPickDialog.show();
                        }
                    }
                });

                items.add(descriptionView);
            }
        }
    }

}
