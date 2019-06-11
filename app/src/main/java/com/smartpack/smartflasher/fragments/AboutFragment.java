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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.smartpack.smartflasher.BuildConfig;
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.views.dialog.Dialog;
import com.smartpack.smartflasher.views.recyclerview.CardView;
import com.smartpack.smartflasher.views.recyclerview.DescriptionView;
import com.smartpack.smartflasher.views.recyclerview.RecyclerViewItem;

import java.util.LinkedHashMap;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public class AboutFragment extends RecyclerViewFragment {

    private static final LinkedHashMap<String, String> sCredits = new LinkedHashMap<>();

    static {
        sCredits.put("Kernel Adiutor,Grarak", "https://github.com/Grarak");
        sCredits.put("Auto Flashing,osm0sis", "https://github.com/osm0sis");
        sCredits.put("Chinese Traditional Translations,jason5545", "https://github.com/jason5545");
        sCredits.put("Russian Translations,andrey167", "https://github.com/andrey167");
        sCredits.put("French Translations,tom4tot", "https://github.com/tom4tot");
        sCredits.put("Portuguese (Brazilian) Translations,DanGLES3", "https://github.com/DanGLES3");
        sCredits.put("Italian Translations,IKAR0S", "https://github.com/IKAR0S");
    }

    @Override
    protected void init() {
        super.init();

        addViewPagerFragment(new InfoFragment());
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        aboutInit(items);
        creditsInit(items);
    }

    private void aboutInit(List<RecyclerViewItem> items) {

        CardView about = new CardView(getActivity());
        about.setTitle(getString(R.string.app_name));

        DescriptionView versioninfo = new DescriptionView();
        versioninfo.setTitle(getString(R.string.version));
        versioninfo.setSummary("v" + BuildConfig.VERSION_NAME);

        about.addItem(versioninfo);

        DescriptionView licence = new DescriptionView();
        licence.setTitle(getString(R.string.licence));
        licence.setSummary(getString(R.string.licence_summary));
        licence.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Utils.launchUrl("https://raw.githubusercontent.com/SmartPack/SmartFlasher/master/LICENSE", requireActivity());
            }
        });

        about.addItem(licence);

        DescriptionView support = new DescriptionView();
        support.setTitle(getString(R.string.support));
        support.setSummary(getString(R.string.support_summary));
        support.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Utils.launchUrl("https://forum.xda-developers.com/android/apps-games/app-smart-flasher-t3934438", getActivity());
            }
        });

        about.addItem(support);

        DescriptionView sourcecode = new DescriptionView();
        sourcecode.setTitle(getString(R.string.source_code));
        sourcecode.setSummary(getString(R.string.source_code_summary));
        sourcecode.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Utils.launchUrl("https://github.com/SmartPack/SmartFlasher", requireActivity());
            }
        });

        about.addItem(sourcecode);

        DescriptionView changelogs = new DescriptionView();
        changelogs.setTitle(getString(R.string.change_logs));
        changelogs.setSummary(getString(R.string.change_logs_summary));
        changelogs.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Utils.launchUrl("https://raw.githubusercontent.com/SmartPack/SmartFlasher/master/change-logs.md", getActivity());
            }
        });

        about.addItem(changelogs);

        DescriptionView playstore = new DescriptionView();
        playstore.setTitle(getString(R.string.playstore));
        playstore.setSummary(getString(R.string.playstore_summary));
        playstore.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Utils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.smartflasher", requireActivity());
            }
        });

        about.addItem(playstore);

        DescriptionView donatetome = new DescriptionView();
        donatetome.setTitle(getString(R.string.donate_me));
        donatetome.setSummary(getString(R.string.donate_me_summary));
        donatetome.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Dialog donate_to_me = new Dialog(getActivity());
                donate_to_me.setIcon(R.mipmap.ic_launcher);
                donate_to_me.setTitle(getString(R.string.donate_me));
                if (Utils.isDonated(requireActivity())) {
                    donate_to_me.setMessage(getString(R.string.donate_me_message));
                    donate_to_me.setNegativeButton(getString(R.string.donate_nope), (dialogInterface, i) -> {
                    });
                } else {
                    donate_to_me.setMessage(getString(R.string.donate_me_message) + getString(R.string.donate_me_playstore));
                    donate_to_me.setNegativeButton(getString(R.string.purchase_app), (dialogInterface, i) -> {
                        Utils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.donate", getActivity());
                    });
                }
                donate_to_me.setPositiveButton(getString(R.string.paypal_donation), (dialog1, id1) -> {
                    Utils.launchUrl("https://www.paypal.me/sunilpaulmathew", getActivity());
                });
                donate_to_me.show();
            }
        });

        about.addItem(donatetome);

        items.add(about);
    }

    private void creditsInit(List<RecyclerViewItem> items) {

        CardView credits = new CardView(getActivity());
        credits.setTitle(getString(R.string.credits));

        for (final String lib : sCredits.keySet()) {
            DescriptionView descriptionView = new DescriptionView();
            descriptionView.setTitle(lib.split(",")[1]);
            descriptionView.setSummary(lib.split(",")[0]);
            descriptionView.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                @Override
                public void onClick(RecyclerViewItem item) {
                    Utils.launchUrl(sCredits.get(lib), getActivity());
                }
            });

            credits.addItem(descriptionView);
        }
        if (credits.size() > 0) {
            items.add(credits);
        }
    }

    public static class InfoFragment extends BaseFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_info, container, false);
            rootView.findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.launchUrl("https://github.com/SmartPack/", getActivity());
                }
            });
            return rootView;
        }
    }

}
