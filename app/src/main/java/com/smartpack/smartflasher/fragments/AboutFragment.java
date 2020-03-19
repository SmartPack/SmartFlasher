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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.smartpack.smartflasher.BuildConfig;
import com.smartpack.smartflasher.MainActivity;
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Prefs;
import com.smartpack.smartflasher.utils.UpdateCheck;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.views.dialog.Dialog;
import com.smartpack.smartflasher.views.recyclerview.DescriptionView;
import com.smartpack.smartflasher.views.recyclerview.RecyclerViewItem;
import com.smartpack.smartflasher.views.recyclerview.SwitchView;
import com.smartpack.smartflasher.views.recyclerview.TitleView;

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
        sCredits.put("App Icon,Toxinpiper", "https://t.me/toxinpiper");
        sCredits.put("Russian Translations,andrey167", "https://github.com/andrey167");
        sCredits.put("Chinese (rCN & rTW) Translations,jason5545", "https://github.com/jason5545");
        sCredits.put("Portuguese (rBr) Translations,DanGLES3", "https://github.com/DanGLES3");
        sCredits.put("French Translations,tom4tot", "https://github.com/tom4tot");
        sCredits.put("Italian Translations,IKAR0S", "https://github.com/IKAR0S");
        sCredits.put("Korean Translations,SmgKhOaRn", "https://github.com/SmgKhOaRn");
    }

    @Override
    protected void init() {
        super.init();

        addViewPagerFragment(new InfoFragment());
    }

    @Override
    public int getSpanCount() {
        return super.getSpanCount() + 1;
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        aboutInit(items);
        creditsInit(items);
    }

    private void aboutInit(List<RecyclerViewItem> items) {
        TitleView about = new TitleView();
        about.setText(getString(R.string.app_name));
        items.add(about);

        DescriptionView versioninfo = new DescriptionView();
        versioninfo.setDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        versioninfo.setTitle(getString(R.string.version));
        versioninfo.setSummary("v" + BuildConfig.VERSION_NAME);

        items.add(versioninfo);

        DescriptionView changelogs = new DescriptionView();
        changelogs.setDrawable(getResources().getDrawable(R.drawable.ic_changelog));
        changelogs.setTitle(getString(R.string.change_logs));
        changelogs.setSummary(getString(R.string.change_logs_summary));
        changelogs.setOnItemClickListener(item ->
                Utils.launchUrl("https://raw.githubusercontent.com/SmartPack/SmartFlasher/master/change-logs.md", getActivity())
        );

        items.add(changelogs);

        SwitchView allow_ads = new SwitchView();
        allow_ads.setDrawable(getResources().getDrawable(R.drawable.ic_ads));
        allow_ads.setSummary(getString(R.string.allow_ads));
        allow_ads.setChecked(Prefs.getBoolean("google_ads", true, getActivity()));
        allow_ads.addOnSwitchListener((switchview, isChecked) -> {
            Prefs.saveBoolean("google_ads", isChecked, getActivity());
            if (!isChecked) {
                new Dialog(requireActivity())
                        .setMessage(R.string.disable_ads_message)
                        .setPositiveButton(R.string.ok, (dialog, id) -> {
                        })
                        .show();
            } else {
                new Dialog(requireActivity())
                        .setMessage(R.string.allow_ads_message)
                        .setPositiveButton(R.string.ok, (dialog, id) -> {
                        })
                        .show();
            }
        });

        items.add(allow_ads);

        SwitchView dark_theme = new SwitchView();
        dark_theme.setDrawable(getResources().getDrawable(R.drawable.ic_color));
        dark_theme.setSummary(getString(R.string.dark_theme));
        dark_theme.setChecked(Prefs.getBoolean("dark_theme", true, getActivity()));
        dark_theme.addOnSwitchListener((switchview, isChecked) -> {
            Prefs.saveBoolean("dark_theme", isChecked, getActivity());
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        items.add(dark_theme);

        DescriptionView support = new DescriptionView();
        support.setDrawable(getResources().getDrawable(R.drawable.ic_support));
        support.setTitle(getString(R.string.support));
        support.setSummary(getString(R.string.support_summary));
        support.setOnItemClickListener(item ->
                Utils.launchUrl("https://t.me/smartpack_kmanager", getActivity())
        );

        items.add(support);

        DescriptionView sourcecode = new DescriptionView();
        sourcecode.setDrawable(getResources().getDrawable(R.drawable.ic_source));
        sourcecode.setTitle(getString(R.string.source_code));
        sourcecode.setSummary(getString(R.string.source_code_summary));
        sourcecode.setOnItemClickListener(item ->
                Utils.launchUrl("https://github.com/SmartPack/SmartFlasher", requireActivity())
        );

        items.add(sourcecode);

        if (UpdateCheck.isPlayStoreInstalled(requireActivity())) {
            DescriptionView playstore = new DescriptionView();
            playstore.setDrawable(getResources().getDrawable(R.drawable.ic_playstore));
            playstore.setTitle(getString(R.string.playstore));
            playstore.setSummary(getString(R.string.playstore_summary));
            playstore.setOnItemClickListener(item ->
                    Utils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.smartflasher", requireActivity())
            );

            items.add(playstore);
        } else {
            DescriptionView updateCheck = new DescriptionView();
            updateCheck.setDrawable(getResources().getDrawable(R.drawable.ic_update));
            updateCheck.setTitle(getString(R.string.update_check));
            updateCheck.setSummary(getString(R.string.update_check_summary));
            updateCheck.setOnItemClickListener(item -> {
                if (Utils.networkUnavailable(requireActivity())) {
                    Utils.toast(R.string.no_internet, getActivity());
                    return;
                }
                UpdateCheck.updateCheck(getActivity());
            });

            items.add(updateCheck);
        }

        DescriptionView donatetome = new DescriptionView();
        donatetome.setDrawable(getResources().getDrawable(R.drawable.ic_donate));
        donatetome.setTitle(getString(R.string.donate_me));
        donatetome.setSummary(getString(R.string.donate_me_summary));
        donatetome.setOnItemClickListener(item -> {
            Dialog donate_to_me = new Dialog(requireActivity());
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
                Utils.launchUrl("https://www.paypal.me/menacherry", getActivity());
            });
            donate_to_me.show();
        });

        items.add(donatetome);

        DescriptionView share = new DescriptionView();
        share.setDrawable(getResources().getDrawable(R.drawable.ic_share));
        share.setTitle(getString(R.string.share_app));
        share.setSummary(getString(R.string.share_app_summary));
        share.setOnItemClickListener(item -> {
            Intent shareapp = new Intent();
            shareapp.setAction(Intent.ACTION_SEND);
            shareapp.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareapp.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message, "v" + BuildConfig.VERSION_NAME));
            shareapp.setType("text/plain");
            Intent shareIntent = Intent.createChooser(shareapp, null);
            startActivity(shareIntent);
        });

        items.add(share);
    }

    private void creditsInit(List<RecyclerViewItem> items) {

        TitleView credits = new TitleView();
        credits.setText(getString(R.string.credits));
        items.add(credits);

        for (final String lib : sCredits.keySet()) {
            String title = lib.split(",")[1];
            String summary = lib.split(",")[0];
            DescriptionView descriptionView = new DescriptionView();
            switch (title) {
                case "Grarak":
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_grarak));
                    break;
                case "osm0sis":
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_osm0sis));
                    break;
                case "Toxinpiper":
                    descriptionView.setDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                    break;
                case "jason5545":
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_jason5545));
                    break;
                case "andrey167":
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_andrey167));
                    break;
                case "tom4tot":
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_tom4tot));
                    break;
                case "DanGLES3":
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_dangles3));
                    break;
                case "IKAR0S":
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_ikar0s));
                    break;
                case "SmgKhOaRn":
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_smgkhoarn));
                    break;
            }
            descriptionView.setTitle(title);
            descriptionView.setSummary(summary);
            descriptionView.setOnItemClickListener(item ->
                    Utils.launchUrl(sCredits.get(lib), getActivity())
            );

            items.add(descriptionView);
        }
    }

    public static class InfoFragment extends BaseFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_info, container, false);
            rootView.findViewById(R.id.image).setOnClickListener(view -> {
                Dialog licence = new Dialog(requireActivity());
                licence.setIcon(R.mipmap.ic_launcher);
                licence.setTitle(getString(R.string.licence));
                licence.setMessage(getString(R.string.licence_message));
                licence.setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });

                licence.show();
            });
            return rootView;
        }
    }

}