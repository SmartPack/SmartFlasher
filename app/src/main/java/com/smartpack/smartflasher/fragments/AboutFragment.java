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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.smartflasher.BuildConfig;
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.activities.ChangeLogActivity;
import com.smartpack.smartflasher.activities.CreditsActivity;
import com.smartpack.smartflasher.utils.Flavour;
import com.smartpack.smartflasher.utils.RecycleViewItem;
import com.smartpack.smartflasher.utils.Utils;

import java.util.ArrayList;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 19, 2020
 */

public class AboutFragment extends Fragment {

    private ArrayList <RecycleViewItem> mData = new ArrayList<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_about, container, false);

        mData.clear();
        mData.add(new RecycleViewItem(getString(R.string.version), BuildConfig.VERSION_NAME, getResources().getDrawable(R.drawable.ic_info), null));
        mData.add(new RecycleViewItem(getString(R.string.change_logs), getString(R.string.change_logs_summary), getResources().getDrawable(R.drawable.ic_eye), null));
        mData.add(new RecycleViewItem(getString(R.string.support), getString(R.string.support_summary), getResources().getDrawable(R.drawable.ic_support), "https://t.me/smartpack_kmanager"));
        mData.add(new RecycleViewItem(getString(R.string.source_code), getString(R.string.source_code_summary), getResources().getDrawable(R.drawable.ic_github), "https://github.com/SmartPack/SmartFlasher"));
        Flavour.launchAppStore(mData, requireActivity());
        mData.add(new RecycleViewItem(getString(R.string.donations), getString(R.string.donations_message), getResources().getDrawable(R.drawable.ic_donate), null));
        mData.add(new RecycleViewItem(getString(R.string.share), getString(R.string.share_app), getResources().getDrawable(R.drawable.ic_share), null));
        mData.add(new RecycleViewItem(getString(R.string.credits), getString(R.string.credits_summary), getResources().getDrawable(R.drawable.ic_contributors), null));

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), Utils.getSpanCount(requireActivity())));
        RecycleViewAdapter mRecycleViewAdapter = new RecycleViewAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (mData.get(position).getURL() != null) {
                Utils.launchUrl(mData.get(position).getURL(), requireActivity());
            } else if (position == 0) {
                Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                settings.setData(uri);
                startActivity(settings);
            } else if (position == 1) {
                Intent changelog = new Intent(getActivity(), ChangeLogActivity.class);
                startActivity(changelog);
            } else if (position == 5) {
                Flavour.showDonateOption(requireActivity());
            } else if (position == 6) {
                Intent shareapp = new Intent();
                shareapp.setAction(Intent.ACTION_SEND);
                shareapp.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                shareapp.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message, BuildConfig.VERSION_NAME));
                shareapp.setType("text/plain");
                Intent shareIntent = Intent.createChooser(shareapp, null);
                startActivity(shareIntent);
            } else {
                Intent credits = new Intent(getActivity(), CreditsActivity.class);
                startActivity(credits);
            }
        });

        return mRootView;
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private ArrayList<RecycleViewItem> data;

        private static RecycleViewAdapter.ClickListener clickListener;

        public RecycleViewAdapter(ArrayList<RecycleViewItem> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_about, parent, false);
            return new RecycleViewAdapter.ViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
            try {
                holder.mTitle.setText(this.data.get(position).getTitle());
                if (Utils.isDarkTheme(holder.mTitle.getContext())) {
                    holder.mTitle.setTextColor(Utils.getThemeAccentColor(holder.mTitle.getContext()));
                }
                holder.mDescription.setText(this.data.get(position).getDescription());
                holder.mIcon.setImageDrawable(this.data.get(position).getIcon());
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }

        @Override
        public int getItemCount() {
            return this.data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private AppCompatImageButton mIcon;
            private MaterialTextView mTitle;
            private MaterialTextView mDescription;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                this.mIcon = view.findViewById(R.id.icon);
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

}