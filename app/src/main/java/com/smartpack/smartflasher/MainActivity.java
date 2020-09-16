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

package com.smartpack.smartflasher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.smartpack.smartflasher.fragments.AboutFragment;
import com.smartpack.smartflasher.fragments.BackupFragment;
import com.smartpack.smartflasher.fragments.FlasherFragment;
import com.smartpack.smartflasher.utils.KernelUpdater;
import com.smartpack.smartflasher.utils.NoRootActivity;
import com.smartpack.smartflasher.utils.PagerAdapter;
import com.smartpack.smartflasher.utils.Prefs;
import com.smartpack.smartflasher.utils.UpdateCheck;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.utils.root.RootUtils;
import com.smartpack.smartflasher.views.dialog.Dialog;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public class MainActivity extends AppCompatActivity {

    private AppCompatImageButton mSettings;
    private boolean mExit;
    private Handler mHandler = new Handler();
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize App Theme & FaceBook Ads
        Utils.initializeAppTheme(this);
        Utils.initializeGoogleAds(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        Utils.mForegroundCard = findViewById(R.id.changelog_card);
        Utils.mBackButton = findViewById(R.id.back);
        Utils.mTitle = findViewById(R.id.card_title);
        Utils.mAppIcon = findViewById(R.id.app_image);
        Utils.mAppName = findViewById(R.id.app_title);
        Utils.mText = findViewById(R.id.scroll_text);
        AppCompatImageView appImage = findViewById(R.id.app_icon);
        appImage.setOnClickListener(v -> showLicence());
        mSettings = findViewById(R.id.settings_menu);
        mSettings.setOnClickListener(v -> settingsMenu());
        Utils.mTabLayout = findViewById(R.id.tabLayoutID);
        mViewPager = findViewById(R.id.viewPagerID);
        AdView mAdView = findViewById(R.id.adView);
        ViewGroup.MarginLayoutParams mLayoutParams = (ViewGroup.MarginLayoutParams) mViewPager.getLayoutParams();

        if (!RootUtils.rootAccess()) {
            Intent noRoot = new Intent(this, NoRootActivity.class);
            startActivity(noRoot);
            finish();
            return;
        }

        if (Prefs.getBoolean("allow_ads", true, this)) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    mAdView.setVisibility(View.GONE);
                    mLayoutParams.bottomMargin = 0;
                }
            });
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
            mLayoutParams.bottomMargin = 0;
        }

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new FlasherFragment(), getString(R.string.flasher));
        adapter.AddFragment(new BackupFragment(), getString(R.string.backup));
        adapter.AddFragment(new AboutFragment(), getString(R.string.about));

        mViewPager.setAdapter(adapter);
        Utils.mTabLayout.setupWithViewPager(mViewPager);
    }
    public void showLicence() {
        Utils.mTitle.setText(getString(R.string.licence));
        Utils.mText.setText(getString(R.string.licence_message));
        Utils.mForegroundActive = true;
        Utils.mBackButton.setVisibility(View.VISIBLE);
        Utils.mTitle.setVisibility(View.VISIBLE);
        Utils.mAppIcon.setVisibility(View.VISIBLE);
        Utils.mAppName.setVisibility(View.VISIBLE);
        Utils.mTabLayout.setVisibility(View.GONE);
        Utils.mForegroundCard.setVisibility(View.VISIBLE);
    }

    public void settingsMenu() {
        mSettings.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSettings);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.dark_theme)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("dark_theme", true, this));
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.allow_ads)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("allow_ads", true, this));
            SubMenu language = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.language, Utils.getLanguage(this)));
            language.add(Menu.NONE, 3, Menu.NONE, getString(R.string.language_default)).setCheckable(true)
                    .setChecked(Utils.languageDefault(this));
            language.add(Menu.NONE, 4, Menu.NONE, getString(R.string.language_en)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_en", false, this));
            language.add(Menu.NONE, 5, Menu.NONE, getString(R.string.language_ch)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_ch", false, this));
            language.add(Menu.NONE, 6, Menu.NONE, getString(R.string.language_ru)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_ru", false, this));
            language.add(Menu.NONE, 7, Menu.NONE, getString(R.string.language_pt)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_pt", false, this));
            language.add(Menu.NONE, 8, Menu.NONE, getString(R.string.language_fr)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_fr", false, this));
            language.add(Menu.NONE, 9, Menu.NONE, getString(R.string.language_it)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_it", false, this));
            language.add(Menu.NONE, 10, Menu.NONE, getString(R.string.language_ko)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_ko", false, this));
            language.add(Menu.NONE, 11, Menu.NONE, getString(R.string.language_am)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_am", false, this));
            language.add(Menu.NONE, 12, Menu.NONE, getString(R.string.language_el)).setCheckable(true)
                    .setChecked(Prefs.getBoolean("use_el", false, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        break;
                    case 1:
                        if (Prefs.getBoolean("dark_theme", true, this)) {
                            Prefs.saveBoolean("dark_theme", false, this);
                        } else {
                            Prefs.saveBoolean("dark_theme", true, this);
                        }
                        restartApp();
                        break;
                    case 2:
                        if (Prefs.getBoolean("allow_ads", true, this)) {
                            Prefs.saveBoolean("allow_ads", false, this);
                            new Dialog(this)
                                    .setMessage(R.string.disable_ads_message)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.ok, (dialog, id) -> {
                                        restartApp();
                                    })
                                    .show();
                        } else {
                            Prefs.saveBoolean("allow_ads", true, this);
                            new Dialog(this)
                                    .setMessage(R.string.allow_ads_message)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.ok, (dialog, id) -> {
                                        restartApp();
                                    })
                                    .show();
                        }
                        break;
                    case 3:
                        if (!Utils.languageDefault(this)) {
                            Prefs.saveBoolean("use_en", false, this);
                            Prefs.saveBoolean("use_ko", false, this);
                            Prefs.saveBoolean("use_am", false, this);
                            Prefs.saveBoolean("use_fr", false, this);
                            Prefs.saveBoolean("use_ru", false, this);
                            Prefs.saveBoolean("use_it", false, this);
                            Prefs.saveBoolean("use_pt", false, this);
                            Prefs.saveBoolean("use_ch", false, this);
                            Prefs.saveBoolean("use_el", false, this);
                            restartApp();
                        }
                        break;
                    case 4:
                        if (!Prefs.getBoolean("use_en", false, this)) {
                            Prefs.saveBoolean("use_en", true, this);
                            Prefs.saveBoolean("use_ko", false, this);
                            Prefs.saveBoolean("use_am", false, this);
                            Prefs.saveBoolean("use_fr", false, this);
                            Prefs.saveBoolean("use_ru", false, this);
                            Prefs.saveBoolean("use_it", false, this);
                            Prefs.saveBoolean("use_pt", false, this);
                            Prefs.saveBoolean("use_ch", false, this);
                            Prefs.saveBoolean("use_el", false, this);
                            restartApp();
                        }
                        break;
                    case 5:
                        if (!Prefs.getBoolean("use_ch", false, this)) {
                            Prefs.saveBoolean("use_en", false, this);
                            Prefs.saveBoolean("use_ko", false, this);
                            Prefs.saveBoolean("use_am", false, this);
                            Prefs.saveBoolean("use_fr", false, this);
                            Prefs.saveBoolean("use_ru", false, this);
                            Prefs.saveBoolean("use_it", false, this);
                            Prefs.saveBoolean("use_pt", false, this);
                            Prefs.saveBoolean("use_ch", true, this);
                            Prefs.saveBoolean("use_el", false, this);
                            restartApp();
                        }
                        break;
                    case 6:
                        if (!Prefs.getBoolean("use_ru", false, this)) {
                            Prefs.saveBoolean("use_en", false, this);
                            Prefs.saveBoolean("use_ko", false, this);
                            Prefs.saveBoolean("use_am", false, this);
                            Prefs.saveBoolean("use_fr", false, this);
                            Prefs.saveBoolean("use_ru", true, this);
                            Prefs.saveBoolean("use_it", false, this);
                            Prefs.saveBoolean("use_pt", false, this);
                            Prefs.saveBoolean("use_ch", false, this);
                            Prefs.saveBoolean("use_el", false, this);
                            restartApp();
                        }
                        break;
                    case 7:
                        if (!Prefs.getBoolean("use_pt", false, this)) {
                            Prefs.saveBoolean("use_en", false, this);
                            Prefs.saveBoolean("use_ko", false, this);
                            Prefs.saveBoolean("use_am", false, this);
                            Prefs.saveBoolean("use_fr", false, this);
                            Prefs.saveBoolean("use_ru", false, this);
                            Prefs.saveBoolean("use_it", false, this);
                            Prefs.saveBoolean("use_pt", true, this);
                            Prefs.saveBoolean("use_ch", false, this);
                            Prefs.saveBoolean("use_el", false, this);
                            restartApp();
                        }
                        break;
                    case 8:
                        if (!Prefs.getBoolean("use_fr", false, this)) {
                            Prefs.saveBoolean("use_en", false, this);
                            Prefs.saveBoolean("use_ko", false, this);
                            Prefs.saveBoolean("use_am", false, this);
                            Prefs.saveBoolean("use_fr", true, this);
                            Prefs.saveBoolean("use_ru", false, this);
                            Prefs.saveBoolean("use_it", false, this);
                            Prefs.saveBoolean("use_pt", false, this);
                            Prefs.saveBoolean("use_ch", false, this);
                            Prefs.saveBoolean("use_el", false, this);
                            restartApp();
                        }
                        break;
                    case 9:
                        if (!Prefs.getBoolean("use_it", false, this)) {
                            Prefs.saveBoolean("use_en", false, this);
                            Prefs.saveBoolean("use_ko", false, this);
                            Prefs.saveBoolean("use_am", false, this);
                            Prefs.saveBoolean("use_fr", false, this);
                            Prefs.saveBoolean("use_ru", false, this);
                            Prefs.saveBoolean("use_it", true, this);
                            Prefs.saveBoolean("use_pt", false, this);
                            Prefs.saveBoolean("use_ch", false, this);
                            Prefs.saveBoolean("use_el", false, this);
                            restartApp();
                        }
                        break;
                    case 10:
                        if (!Prefs.getBoolean("use_ko", false, this)) {
                            Prefs.saveBoolean("use_en", false, this);
                            Prefs.saveBoolean("use_ko", true, this);
                            Prefs.saveBoolean("use_am", false, this);
                            Prefs.saveBoolean("use_fr", false, this);
                            Prefs.saveBoolean("use_ru", false, this);
                            Prefs.saveBoolean("use_it", false, this);
                            Prefs.saveBoolean("use_pt", false, this);
                            Prefs.saveBoolean("use_ch", false, this);
                            Prefs.saveBoolean("use_el", false, this);
                            restartApp();
                        }
                        break;
                    case 11:
                        if (!Prefs.getBoolean("use_am", false, this)) {
                            Prefs.saveBoolean("use_en", false, this);
                            Prefs.saveBoolean("use_ko", false, this);
                            Prefs.saveBoolean("use_am", true, this);
                            Prefs.saveBoolean("use_fr", false, this);
                            Prefs.saveBoolean("use_ru", false, this);
                            Prefs.saveBoolean("use_it", false, this);
                            Prefs.saveBoolean("use_pt", false, this);
                            Prefs.saveBoolean("use_ch", false, this);
                            Prefs.saveBoolean("use_el", false, this);
                            restartApp();
                        }
                        break;
                    case 12:
                        if (!Prefs.getBoolean("use_el", false, this)) {
                            Prefs.saveBoolean("use_en", false, this);
                            Prefs.saveBoolean("use_ko", false, this);
                            Prefs.saveBoolean("use_am", false, this);
                            Prefs.saveBoolean("use_fr", false, this);
                            Prefs.saveBoolean("use_ru", false, this);
                            Prefs.saveBoolean("use_it", false, this);
                            Prefs.saveBoolean("use_pt", false, this);
                            Prefs.saveBoolean("use_ch", false, this);
                            Prefs.saveBoolean("use_el", true, this);
                            restartApp();
                        }
                        break;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    public void closeForeGround(View view) {
        Utils.mForegroundCard = findViewById(R.id.changelog_card);
        Utils.mBackButton = findViewById(R.id.back);
        Utils.mAppIcon = findViewById(R.id.app_image);
        Utils.mAppName = findViewById(R.id.app_title);
        Utils.mTitle = findViewById(R.id.card_title);
        Utils.mAppIcon.setVisibility(View.GONE);
        Utils.mAppName.setVisibility(View.GONE);
        Utils.mBackButton.setVisibility(View.GONE);
        Utils.mTitle .setVisibility(View.GONE);
        Utils.mForegroundCard.setVisibility(View.GONE);
        Utils.mForegroundActive = false;
        Utils.mTabLayout.setVisibility(View.VISIBLE);
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onStart(){
        super.onStart();

        if (!Utils.checkWriteStoragePermission(this)) {
            return;
        }
        if (Utils.networkUnavailable(this)) {
            return;
        }
        if (!Utils.isDownloadBinaries()) {
            return;
        }

        // Initialize kernel update check - Once in a day
        if (Prefs.getBoolean("update_check", true, this)
                && !KernelUpdater.getUpdateChannel().equals("Unavailable") && KernelUpdater.lastModified() +
                89280000L < System.currentTimeMillis()) {
            KernelUpdater.updateInfo(Utils.readFile(Utils.getInternalDataStorage() + "/update_channel"));
        }

        // Initialize manual Update Check, if play store not found
        if (!UpdateCheck.isPlayStoreInstalled(this)) {
            UpdateCheck.autoUpdateCheck(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (RootUtils.rootAccess()) {
            if (Utils.mForegroundActive) {
                closeForeGround(mViewPager);
            } else if (mExit) {
                mExit = false;
                super.onBackPressed();
            } else {
                Utils.snackbar(mViewPager, getString(R.string.press_back));
                mExit = true;
                mHandler.postDelayed(() -> mExit = false, 2000);
            }
        } else {
            super.onBackPressed();
        }
    }

}