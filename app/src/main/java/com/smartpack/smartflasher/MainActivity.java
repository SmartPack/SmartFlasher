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

package com.smartpack.smartflasher;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.tabs.TabLayout;
import com.smartpack.smartflasher.fragments.AboutFragment;
import com.smartpack.smartflasher.fragments.BackupFragment;
import com.smartpack.smartflasher.fragments.FlasherFragment;
import com.smartpack.smartflasher.utils.PagerAdapter;
import com.smartpack.smartflasher.utils.Prefs;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.utils.root.RootUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Prefs.getBoolean("dark_theme", true, this)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.no_root_Text);
        TabLayout tabLayout = findViewById(R.id.tabLayoutID);
        ViewPager viewPager = findViewById(R.id.viewPagerID);

        if (!RootUtils.rootAccess()) {
            textView.setText(getString(R.string.no_root));
            return;
        }

        if (!Utils.checkWriteStoragePermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new FlasherFragment(), getString(R.string.flasher));
        adapter.AddFragment(new BackupFragment(), getString(R.string.backup));
        adapter.AddFragment(new AboutFragment(), getString(R.string.about));

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        // Initialize Google Ads
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-7791710838910455~6603969352");
    }
}
