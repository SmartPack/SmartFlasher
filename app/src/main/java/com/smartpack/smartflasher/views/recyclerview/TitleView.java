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

package com.smartpack.smartflasher.views.recyclerview;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.smartpack.smartflasher.R;

/**
 *  Created by sunilpaulmathew <sunil.kde@gmail.com> on January 03, 2020
 *
 * Adapted from https://github.com/Grarak/KernelAdiutor by Willi Ye.
 */

public class TitleView extends RecyclerViewItem {

    private AppCompatTextView mTitle;

    private CharSequence mTitleText;

    @Override
    public int getLayoutRes() {
        return R.layout.rv_title_view;
    }

    @Override
    public void onCreateView(View view) {
        mTitle = view.findViewById(R.id.title);

        setFullSpan(true);
        super.onCreateView(view);
    }

    public void setText(CharSequence text) {
        mTitleText = text;
        refresh();
    }

    @Override
    protected void refresh() {
        super.refresh();
        if (mTitle != null && mTitleText != null) {
            mTitle.setText(mTitleText);
        }
    }

    @Override
    protected boolean cardCompatible() {
        return false;
    }
}
