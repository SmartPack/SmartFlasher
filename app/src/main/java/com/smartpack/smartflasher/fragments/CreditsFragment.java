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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.RecycleViewItem;
import com.smartpack.smartflasher.utils.Utils;

import java.util.ArrayList;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 21, 2020
 */

public class CreditsFragment extends Fragment {

    private ArrayList <RecycleViewItem> mData = new ArrayList<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_credits, container, false);

        mData.clear();
        mData.add(new RecycleViewItem("Kernel Adiutor", "Grarak", getResources().getDrawable(R.drawable.ic_grarak), "https://github.com/Grarak"));
        mData.add(new RecycleViewItem("libsu", "topjohnwu", getResources().getDrawable(R.drawable.ic_topjohnwu),"https://github.com/topjohnwu"));
        mData.add(new RecycleViewItem("Auto Flashing","osm0sis", getResources().getDrawable(R.drawable.ic_osm0sis),"https://github.com/osm0sis"));
        mData.add(new RecycleViewItem("Code contributions","Lennoard", getResources().getDrawable(R.drawable.ic_lennoard),"https://github.com/Lennoard"));
        mData.add(new RecycleViewItem("App Icon","Toxinpiper", getResources().getDrawable(R.mipmap.ic_launcher_round),"https://t.me/toxinpiper"));
        mData.add(new RecycleViewItem("Russian Translations","andrey167", getResources().getDrawable(R.drawable.ic_andrey167),"https://github.com/andrey167"));
        mData.add(new RecycleViewItem("Chinese (rCN & rTW) Translations","jason5545", getResources().getDrawable(R.drawable.ic_jason5545),"https://github.com/jason5545"));
        mData.add(new RecycleViewItem("Portuguese (rBr) Translations","DanGLES3", getResources().getDrawable(R.drawable.ic_dangles3),"https://github.com/DanGLES3"));
        mData.add(new RecycleViewItem("French Translations","tom4tot", getResources().getDrawable(R.drawable.ic_tom4tot),"https://github.com/tom4tot"));
        mData.add(new RecycleViewItem("Italian Translations","IKAR0S", getResources().getDrawable(R.drawable.ic_ikar0s),"https://github.com/IKAR0S"));
        mData.add(new RecycleViewItem("Korean Translations","FiestaLake", getResources().getDrawable(R.drawable.ic_fiestalake),"https://github.com/FiestaLake"));
        mData.add(new RecycleViewItem("Amharic Translations","Mikesew1320", getResources().getDrawable(R.drawable.ic_mikesew),"https://github.com/Mikesew1320"));
        mData.add(new RecycleViewItem("Greek Translations","tsiflimagas", getResources().getDrawable(R.drawable.ic_tsiflimagas),"https://github.com/tsiflimagas"));

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), Utils.getSpanCount(requireActivity())));
        RecycleViewAdapter mRecycleViewAdapter = new RecycleViewAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            Utils.launchUrl(mData.get(position).getURL(), requireActivity());
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
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_credits, parent, false);
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
            private AppCompatImageView mIcon;
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