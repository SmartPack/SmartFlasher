package com.smartpack.smartflasher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.SubMenu;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.smartflasher.activities.NoRootActivity;
import com.smartpack.smartflasher.fragments.AboutFragment;
import com.smartpack.smartflasher.fragments.BackupFragment;
import com.smartpack.smartflasher.fragments.FlasherFragment;
import com.smartpack.smartflasher.utils.Backup;
import com.smartpack.smartflasher.utils.KernelUpdater;
import com.smartpack.smartflasher.utils.PagerAdapter;
import com.smartpack.smartflasher.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public class MainActivity extends AppCompatActivity {

    private AppCompatImageButton mSettings;
    private boolean mExit;
    private final Handler mHandler = new Handler();
    private LinearLayout mProgressLayout;
    private MaterialTextView mProgressText;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        Utils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        if (!Utils.rootAccess()) {
            Intent noRoot = new Intent(this, NoRootActivity.class);
            startActivity(noRoot);
            finish();
            return;
        }

        if (!Utils.getBoolean("depreciation_message", false, this)) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("Notice")
                    .setMessage("Please Note: The development of this project is abandoned. Thank you very much to all of you for supporting this project to date.")
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> Utils.saveBoolean("depreciation_message", true, this)).show();
        }

        mProgressLayout = findViewById(R.id.progress_layout);
        mProgressText = findViewById(R.id.progress_text);
        TabLayout mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        mSettings = findViewById(R.id.settings_menu);

        if (Utils.isDarkTheme(this)) {
            mProgressText.setTextColor(Utils.getThemeAccentColor(this));
        }

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new FlasherFragment(), getString(R.string.flasher));
        adapter.AddFragment(new BackupFragment(), getString(R.string.backup));
        adapter.AddFragment(new AboutFragment(), getString(R.string.about));

        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mSettings.setOnClickListener(v -> settingsMenu());
    }

    public void settingsMenu() {
        PopupMenu popupMenu = new PopupMenu(this, mSettings);
        Menu menu = popupMenu.getMenu();
        SubMenu language = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.language, Utils.getLanguage(this)));
        language.add(Menu.NONE, 1, Menu.NONE, getString(R.string.language_default)).setCheckable(true)
                .setChecked(Utils.languageDefault(this));
        language.add(Menu.NONE, 2, Menu.NONE, getString(R.string.language_en)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_en", false, this));
        language.add(Menu.NONE, 3, Menu.NONE, getString(R.string.language_ch)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_ch", false, this));
        language.add(Menu.NONE, 4, Menu.NONE, getString(R.string.language_ru)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_ru", false, this));
        language.add(Menu.NONE, 5, Menu.NONE, getString(R.string.language_pt)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_pt", false, this));
        language.add(Menu.NONE, 6, Menu.NONE, getString(R.string.language_fr)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_fr", false, this));
        language.add(Menu.NONE, 7, Menu.NONE, getString(R.string.language_it)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_it", false, this));
        language.add(Menu.NONE, 8, Menu.NONE, getString(R.string.language_ko)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_ko", false, this));
        language.add(Menu.NONE, 9, Menu.NONE, getString(R.string.language_am)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_am", false, this));
        language.add(Menu.NONE, 10, Menu.NONE, getString(R.string.language_el)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_el", false, this));
        SubMenu reboot = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.reboot_options));
        reboot.add(Menu.NONE, 11, Menu.NONE, getString(R.string.reboot_normal));
        reboot.add(Menu.NONE, 12, Menu.NONE, getString(R.string.reboot_recovery));
        reboot.add(Menu.NONE, 13, Menu.NONE, getString(R.string.reboot_bootloader));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    if (!Utils.languageDefault(this)) {
                        Utils.saveBoolean("use_en", false, this);
                        Utils.saveBoolean("use_ko", false, this);
                        Utils.saveBoolean("use_am", false, this);
                        Utils.saveBoolean("use_fr", false, this);
                        Utils.saveBoolean("use_ru", false, this);
                        Utils.saveBoolean("use_it", false, this);
                        Utils.saveBoolean("use_pt", false, this);
                        Utils.saveBoolean("use_ch", false, this);
                        Utils.saveBoolean("use_el", false, this);
                        restartApp();
                    }
                    break;
                case 2:
                    if (!Utils.getBoolean("use_en", false, this)) {
                        Utils.saveBoolean("use_en", true, this);
                        Utils.saveBoolean("use_ko", false, this);
                        Utils.saveBoolean("use_am", false, this);
                        Utils.saveBoolean("use_fr", false, this);
                        Utils.saveBoolean("use_ru", false, this);
                        Utils.saveBoolean("use_it", false, this);
                        Utils.saveBoolean("use_pt", false, this);
                        Utils.saveBoolean("use_ch", false, this);
                        Utils.saveBoolean("use_el", false, this);
                        restartApp();
                    }
                    break;
                case 3:
                    if (!Utils.getBoolean("use_ch", false, this)) {
                        Utils.saveBoolean("use_en", false, this);
                        Utils.saveBoolean("use_ko", false, this);
                        Utils.saveBoolean("use_am", false, this);
                        Utils.saveBoolean("use_fr", false, this);
                        Utils.saveBoolean("use_ru", false, this);
                        Utils.saveBoolean("use_it", false, this);
                        Utils.saveBoolean("use_pt", false, this);
                        Utils.saveBoolean("use_ch", true, this);
                        Utils.saveBoolean("use_el", false, this);
                        restartApp();
                    }
                    break;
                case 4:
                    if (!Utils.getBoolean("use_ru", false, this)) {
                        Utils.saveBoolean("use_en", false, this);
                        Utils.saveBoolean("use_ko", false, this);
                        Utils.saveBoolean("use_am", false, this);
                        Utils.saveBoolean("use_fr", false, this);
                        Utils.saveBoolean("use_ru", true, this);
                        Utils.saveBoolean("use_it", false, this);
                        Utils.saveBoolean("use_pt", false, this);
                        Utils.saveBoolean("use_ch", false, this);
                        Utils.saveBoolean("use_el", false, this);
                        restartApp();
                    }
                    break;
                case 5:
                    if (!Utils.getBoolean("use_pt", false, this)) {
                        Utils.saveBoolean("use_en", false, this);
                        Utils.saveBoolean("use_ko", false, this);
                        Utils.saveBoolean("use_am", false, this);
                        Utils.saveBoolean("use_fr", false, this);
                        Utils.saveBoolean("use_ru", false, this);
                        Utils.saveBoolean("use_it", false, this);
                        Utils.saveBoolean("use_pt", true, this);
                        Utils.saveBoolean("use_ch", false, this);
                        Utils.saveBoolean("use_el", false, this);
                        restartApp();
                    }
                    break;
                case 6:
                    if (!Utils.getBoolean("use_fr", false, this)) {
                        Utils.saveBoolean("use_en", false, this);
                        Utils.saveBoolean("use_ko", false, this);
                        Utils.saveBoolean("use_am", false, this);
                        Utils.saveBoolean("use_fr", true, this);
                        Utils.saveBoolean("use_ru", false, this);
                        Utils.saveBoolean("use_it", false, this);
                        Utils.saveBoolean("use_pt", false, this);
                        Utils.saveBoolean("use_ch", false, this);
                        Utils.saveBoolean("use_el", false, this);
                        restartApp();
                    }
                    break;
                case 7:
                    if (!Utils.getBoolean("use_it", false, this)) {
                        Utils.saveBoolean("use_en", false, this);
                        Utils.saveBoolean("use_ko", false, this);
                        Utils.saveBoolean("use_am", false, this);
                        Utils.saveBoolean("use_fr", false, this);
                        Utils.saveBoolean("use_ru", false, this);
                        Utils.saveBoolean("use_it", true, this);
                        Utils.saveBoolean("use_pt", false, this);
                        Utils.saveBoolean("use_ch", false, this);
                        Utils.saveBoolean("use_el", false, this);
                        restartApp();
                    }
                    break;
                case 8:
                    if (!Utils.getBoolean("use_ko", false, this)) {
                        Utils.saveBoolean("use_en", false, this);
                        Utils.saveBoolean("use_ko", true, this);
                        Utils.saveBoolean("use_am", false, this);
                        Utils.saveBoolean("use_fr", false, this);
                        Utils.saveBoolean("use_ru", false, this);
                        Utils.saveBoolean("use_it", false, this);
                        Utils.saveBoolean("use_pt", false, this);
                        Utils.saveBoolean("use_ch", false, this);
                        Utils.saveBoolean("use_el", false, this);
                        restartApp();
                    }
                    break;
                case 9:
                    if (!Utils.getBoolean("use_am", false, this)) {
                        Utils.saveBoolean("use_en", false, this);
                        Utils.saveBoolean("use_ko", false, this);
                        Utils.saveBoolean("use_am", true, this);
                        Utils.saveBoolean("use_fr", false, this);
                        Utils.saveBoolean("use_ru", false, this);
                        Utils.saveBoolean("use_it", false, this);
                        Utils.saveBoolean("use_pt", false, this);
                        Utils.saveBoolean("use_ch", false, this);
                        Utils.saveBoolean("use_el", false, this);
                        restartApp();
                    }
                    break;
                case 10:
                    if (!Utils.getBoolean("use_el", false, this)) {
                        Utils.saveBoolean("use_en", false, this);
                        Utils.saveBoolean("use_ko", false, this);
                        Utils.saveBoolean("use_am", false, this);
                        Utils.saveBoolean("use_fr", false, this);
                        Utils.saveBoolean("use_ru", false, this);
                        Utils.saveBoolean("use_it", false, this);
                        Utils.saveBoolean("use_pt", false, this);
                        Utils.saveBoolean("use_ch", false, this);
                        Utils.saveBoolean("use_el", true, this);
                        restartApp();
                    }
                    break;
                case 11:
                    Utils.reboot("", mProgressLayout, mProgressText, this);
                    break;
                case 12:
                    Utils.reboot(" recovery", mProgressLayout, mProgressText, this);
                    break;
                case 13:
                    Utils.reboot(" bootloader", mProgressLayout, mProgressText, this);
                    break;
            }
            return false;
        });
        popupMenu.show();
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
        if (Utils.isNetworkUnavailable(this)) {
            return;
        }

        // Initialize kernel update check - Once in a day
        if (Utils.getBoolean("update_check", true, this)
                && !KernelUpdater.getUpdateChannel(this).equals("Unavailable") && KernelUpdater.lastModified(this) +
                89280000L < System.currentTimeMillis()) {
            KernelUpdater.updateInfo(Utils.read(getFilesDir().getPath() + "/updatechannel"), this);
        }

        // Get boot  & recovery partition info
        Backup.findBootPartition();
        Backup.findRecoveryPartition();
    }

    @Override
    public void onBackPressed() {
        if (Utils.rootAccess()) {
            if (mExit) {
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