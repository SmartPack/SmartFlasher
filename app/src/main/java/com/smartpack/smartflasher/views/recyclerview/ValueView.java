/*
 * Copyright (C) 2019-2020 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Smart Flasher, which is a simple app aimed to make flashing
 * recovery zip files much easier. Significant amount of code for this app has been from
 * Kernel Adiutor by Willi Ye <williye97@gmail.com>
 *
 * Smart Flasher is free softwares: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *
 * SmartPack Kernel Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SmartPack Kernel Manager.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.smartpack.smartflasher.views.recyclerview;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.smartpack.smartflasher.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 */

public abstract class ValueView extends RecyclerViewItem {

    private TextView mTitleView;
    private TextView mSummaryView;
    private View mValueParent;
    private TextView mValueView;
    private View mProgress;

    private CharSequence mTitle;
    private CharSequence mSummary;
    private String mValue;
    private int mValuesRes;

    @Override
    public int getLayoutRes() {
        return R.layout.rv_value_view;
    }

    @Override
    public void onCreateView(View view) {
        mTitleView = (TextView) view.findViewById(R.id.title);
        mSummaryView = (TextView) view.findViewById(R.id.summary);
        mValueParent = view.findViewById(R.id.value_parent);
        mValueView = (TextView) view.findViewById(R.id.value);
        mProgress = view.findViewById(R.id.progress);

        super.onCreateView(view);
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        refresh();
    }

    public void setSummary(CharSequence summary) {
        mSummary = summary;
        refresh();
    }

    public void setValue(String value) {
        mValue = value;
        refresh();
    }

    public void setValue(@StringRes int value) {
        mValuesRes = value;
        refresh();
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public String getValue() {
        return mValue;
    }

    @Override
    protected void refresh() {
        super.refresh();

        if (mTitleView != null) {
            if (mTitle != null) {
                mTitleView.setText(mTitle);
                mTitleView.setVisibility(View.VISIBLE);
            } else {
                mTitleView.setVisibility(View.GONE);
            }
        }

        if (mSummaryView != null && mSummary != null) {
            mSummaryView.setText(mSummary);
        }

        if (mValueView != null && (mValue != null || mValuesRes != 0)) {
            if (mValue == null) {
                mValue = mValueView.getContext().getString(mValuesRes);
            }
            mValueView.setText(mValue);
            mValueView.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
            mValueParent.setVisibility(mValue.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

}
