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

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.smartpack.smartflasher.fragments.AboutFragment;
import com.smartpack.smartflasher.fragments.BackupFragment;
import com.smartpack.smartflasher.fragments.FlasherFragment;
import com.smartpack.smartflasher.utils.KernelUpdater;
import com.smartpack.smartflasher.utils.PagerAdapter;
import com.smartpack.smartflasher.utils.Prefs;
import com.smartpack.smartflasher.utils.UpdateCheck;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.utils.root.RootUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public class MainActivity extends AppCompatActivity {

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

        AppCompatImageView unsupported = findViewById(R.id.no_root_Image);
        TextView textView = findViewById(R.id.no_root_Text);
        Utils.mTabLayout = findViewById(R.id.tabLayoutID);
        mViewPager = findViewById(R.id.viewPagerID);
        AdView mAdView = findViewById(R.id.adView);
        ViewGroup.MarginLayoutParams mLayoutParams = (ViewGroup.MarginLayoutParams) mViewPager.getLayoutParams();

        if (!RootUtils.rootAccess()) {
            textView.setText(getString(R.string.no_root));
            unsupported.setImageDrawable(Utils.getColoredIcon(R.drawable.ic_help, this));
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

    public void androidRooting(View view) {
        Utils.launchUrl("https://www.google.com/search?site=&source=hp&q=android+rooting+magisk", this);
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