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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.Prefs;
import com.smartpack.smartflasher.utils.Utils;
import com.smartpack.smartflasher.utils.ViewUtils;
import com.smartpack.smartflasher.viewpagerindicator.CirclePageIndicator;
import com.smartpack.smartflasher.views.dialog.ViewPagerDialog;
import com.smartpack.smartflasher.views.recyclerview.RecyclerViewAdapter;
import com.smartpack.smartflasher.views.recyclerview.RecyclerViewItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public abstract class RecyclerViewFragment extends BaseFragment {

    private Handler mHandler;
    private ScheduledThreadPoolExecutor mPoolExecutor;

    private View mRootView;

    private List<RecyclerViewItem> mItems = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private Scroller mScroller;

    private View mProgress;

    private List<Fragment> mViewPagerFragments;
    private ViewPagerAdapter mViewPagerAdapter;
    private View mViewPagerParent;
    private ViewPager mViewPager;
    private CirclePageIndicator mCirclePageIndicator;

    private FloatingActionButton mTopFab;
    private FloatingActionButton mBottomFab;

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolBar;

    private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoader;

    private ValueAnimator mForegroundAnimator;
    private boolean mForegroundVisible;
    private View mForegroundParent;
    private float mForegroundHeight;
    private CharSequence mForegroundStrText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mHandler = new Handler();

        mRecyclerView = mRootView.findViewById(R.id.recyclerview);

        if (Prefs.getBoolean("allow_ads", true, requireActivity())) {
            AdView mAdView = new AdView(requireActivity(), "634517477132281_634728027111226", AdSize.BANNER_HEIGHT_50);
            LinearLayout adContainer = mRootView.findViewById(R.id.banner_container);
            adContainer.addView(mAdView);
            mAdView.loadAd();
        }

        if (mViewPagerFragments != null) {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : mViewPagerFragments) {
                fragmentTransaction.remove(fragment);
            }
            fragmentTransaction.commitAllowingStateLoss();
            mViewPagerFragments.clear();
        } else {
            mViewPagerFragments = new ArrayList<>();
        }
        mViewPagerParent = mRootView.findViewById(R.id.viewpagerparent);
        mViewPager = mRootView.findViewById(R.id.viewpager);
        mViewPager.setVisibility(View.INVISIBLE);
        mCirclePageIndicator = mRootView.findViewById(R.id.indicator);
        mViewPagerParent.setVisibility(View.INVISIBLE);
        ViewUtils.dismissDialog(getChildFragmentManager());

        mProgress = mRootView.findViewById(R.id.progress);

        if (mAppBarLayout != null && !isForeground()) {
            mAppBarLayout.postDelayed(() -> {
                if (mAppBarLayout != null && isAdded() && getActivity() != null) {
                    ViewCompat.setElevation(mAppBarLayout, showViewPager() ?
                            0 : getResources().getDimension(R.dimen.app_bar_elevation));
                }
            }, 150);
        }

        mTopFab = mRootView.findViewById(R.id.top_fab);
        mBottomFab = mRootView.findViewById(R.id.bottom_fab);

        mRecyclerView.clearOnScrollListeners();
        if (showViewPager()) {
            mScroller = new Scroller();
            mRecyclerView.addOnScrollListener(mScroller);
        }
        mRecyclerView.setAdapter(mRecyclerViewAdapter == null ? mRecyclerViewAdapter
                = new RecyclerViewAdapter(mItems, () -> getHandler().postDelayed(() -> {
                    if (isAdded() && getActivity() != null) {
                        adjustScrollPosition();
                    }
                }, 250)) : mRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager = getLayoutManager());
        mRecyclerView.setHasFixedSize(true);

        mTopFab.setOnClickListener(v -> onTopFabClick());
        {
            Drawable drawable = getTopFabDrawable();
            if (drawable != null) {
                mTopFab.setImageDrawable(drawable);
            }
        }

        mBottomFab.setOnClickListener(v -> onBottomFabClick());
        {
            Drawable drawable = getBottomFabDrawable();
            if (drawable != null) {
                mBottomFab.setImageDrawable(drawable);
            }
        }

        BaseFragment foregroundFragment = getForegroundFragment();
        mForegroundVisible = false;
        if (foregroundFragment != null) {
            mForegroundParent = mRootView.findViewById(R.id.foreground_parent);
            TextView mForegroundText = mRootView.findViewById(R.id.foreground_text);
            mForegroundText.setOnClickListener(v -> dismissForeground());
            getChildFragmentManager().beginTransaction().replace(R.id.foreground_content,
                    foregroundFragment).commit();
            mForegroundHeight = getResources().getDisplayMetrics().heightPixels;
        }

        if (itemsSize() == 0) {
            mLoader = new UILoader(this, savedInstanceState);
            mLoader.execute();
        } else {
            showProgress();
            init();
            hideProgress();
            postInit();
            adjustScrollPosition();

            mViewPager.setVisibility(View.VISIBLE);
        }

        return mRootView;
    }

    private static class UILoader extends AsyncTask<Void, Void, List<RecyclerViewItem>> {

        private WeakReference<RecyclerViewFragment> mRefFragment;
        private Bundle mSavedInstanceState;

        private UILoader(RecyclerViewFragment fragment, Bundle savedInstanceState) {
            mRefFragment = new WeakReference<>(fragment);
            mSavedInstanceState = savedInstanceState;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RecyclerViewFragment fragment = mRefFragment.get();

            fragment.showProgress();
            fragment.init();
        }

        @Override
        protected List<RecyclerViewItem> doInBackground(Void... params) {
            RecyclerViewFragment fragment = mRefFragment.get();

            if (fragment.isAdded() && fragment.getActivity() != null) {
                List<RecyclerViewItem> items = new ArrayList<>();
                fragment.addItems(items);
                return items;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecyclerViewItem> recyclerViewItems) {
            super.onPostExecute(recyclerViewItems);
            if (isCancelled() || recyclerViewItems == null) return;

            final RecyclerViewFragment fragment = mRefFragment.get();

            for (RecyclerViewItem item : recyclerViewItems) {
                fragment.addItem(item);
            }
            fragment.hideProgress();
            fragment.postInit();
            if (mSavedInstanceState == null) {
                fragment.mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        Activity activity = fragment.getActivity();
                        if (fragment.isAdded() && activity != null) {
                            fragment.mRecyclerView.startAnimation(AnimationUtils.loadAnimation(
                                    activity, R.anim.slide_in_bottom));

                            int cx = fragment.mViewPager.getWidth();

                            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                                    fragment.mViewPager, cx / 2, 0, 0, cx);
                            animator.addListener(new SupportAnimator.SimpleAnimatorListener() {
                                @Override
                                public void onAnimationStart() {
                                    super.onAnimationStart();
                                    fragment.mViewPager.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd() {
                                    super.onAnimationEnd();
                                }
                            });
                            animator.setDuration(400);
                            animator.start();
                        }
                    }
                });
            } else {
                fragment.mViewPager.setVisibility(View.VISIBLE);
            }
            fragment.mLoader = null;
        }
    }

    @Override
    public void onViewFinished() {
        super.onViewFinished();
        if (showViewPager()) {
            mViewPager.setAdapter(mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(),
                    mViewPagerFragments));
            mCirclePageIndicator.setViewPager(mViewPager);

            setAppBarLayoutAlpha(0);
            adjustScrollPosition();
        } else {
            mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), isForeground() ? 0 : mToolBar.getHeight(),
                    mRecyclerView.getPaddingRight(), mRecyclerView.getPaddingBottom());
            mRecyclerView.setClipToPadding(true);
            ViewGroup.LayoutParams layoutParams = mViewPagerParent.getLayoutParams();
            layoutParams.height = 0;
            mViewPagerParent.requestLayout();
            setAppBarLayoutAlpha(255);
        }
    }

    protected void init() {
    }

    protected void postInit() {
        if (getActivity() != null && isAdded()) {
            for (RecyclerViewItem item : mItems) {
                item.onRecyclerViewCreate(getActivity());
            }
        }
    }

    private void adjustScrollPosition() {
        if (mScroller != null) {
            mScroller.onScrolled(mRecyclerView, 0, 0);
        }
    }

    protected abstract void addItems(List<RecyclerViewItem> items);

    private void setAppBarLayoutAlpha(int alpha) {
        if (isForeground()) return;
        Activity activity;
        if ((activity = getActivity()) != null && mAppBarLayout != null && mToolBar != null) {
            int colorPrimary = ViewUtils.getColorPrimaryColor(activity);
            mAppBarLayout.setBackgroundDrawable(new ColorDrawable(Color.argb(alpha, Color.red(colorPrimary),
                    Color.green(colorPrimary), Color.blue(colorPrimary))));
            mToolBar.setTitleTextColor(Color.argb(alpha, 255, 255, 255));
        }
    }

    void addItem(RecyclerViewItem recyclerViewItem) {
        mItems.add(recyclerViewItem);
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyItemInserted(mItems.size() - 1);
        }
        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) mLayoutManager).setSpanCount(getSpanCount());
        }
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
    }

    private int getBannerHeight() {
        int min = Math.round(getResources().getDimension(R.dimen.banner_min_height));
        int max = Math.round(getResources().getDimension(R.dimen.banner_max_height));

        int height = Prefs.getInt("banner_size", Math.round(getResources().getDimension(
                R.dimen.banner_min_height)), getActivity());
        if (height > max) {
            height = max;
            Prefs.saveInt("banner_size", max, getActivity());
        } else if (height < min) {
            height = min;
            Prefs.saveInt("banner_size", min, getActivity());
        }
        return height;
    }

    void clearItems() {
        mItems.clear();
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager = getLayoutManager());
            adjustScrollPosition();
        }
    }

    public int getSpanCount() {
        Activity activity;
        if ((activity = getActivity()) != null) {
            int span = Utils.isTablet(activity) ? Utils.getOrientation(activity) ==
                    Configuration.ORIENTATION_LANDSCAPE ? 3 : 2 : Utils.getOrientation(activity) ==
                    Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
            if (itemsSize() != 0 && span > itemsSize()) {
                span = itemsSize();
            }
            return span;
        }
        return 1;
    }

    int itemsSize() {
        return mItems.size();
    }

    void addViewPagerFragment(BaseFragment fragment) {
        mViewPagerFragments.add(fragment);
        if (mViewPagerAdapter != null) {
            mViewPagerAdapter.notifyDataSetChanged();
        }
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments;

        public ViewPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
            super(fragmentManager);
            mFragments = fragments;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments == null ? 0 : mFragments.size();
        }
    }

    private class Scroller extends RecyclerView.OnScrollListener {

        private int mScrollDistance;
        private int mAppBarLayoutDistance;
        private boolean mFade = true;
        private ValueAnimator mAlphaAnimator;

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            View firstItem = mRecyclerView.getChildAt(0);
            if (firstItem == null) {
                if (mRecyclerViewAdapter != null) {
                    firstItem = mRecyclerViewAdapter.getFirstItem();
                }
                if (firstItem == null) {
                    return;
                }
            }

            mScrollDistance = -firstItem.getTop() + mRecyclerView.getPaddingTop();

            int appBarHeight = 0;
            if (mAppBarLayout != null) {
                appBarHeight = mAppBarLayout.getHeight();
            }

            if (mScrollDistance > mViewPagerParent.getHeight() - appBarHeight) {
                mAppBarLayoutDistance += dy;
                fadeAppBarLayout(false);
                if (mTopFab != null && showTopFab()) {
                    mTopFab.hide();
                }
            } else {
                fadeAppBarLayout(true);
                if (mTopFab != null && showTopFab()) {
                    mTopFab.show();
                }
            }

            if (mAppBarLayout != null) {
                if (mAppBarLayoutDistance > mAppBarLayout.getHeight()) {
                    mAppBarLayoutDistance = mAppBarLayout.getHeight();
                } else if (mAppBarLayoutDistance < 0) {
                    mAppBarLayoutDistance = 0;
                }
                mAppBarLayout.setTranslationY(-mAppBarLayoutDistance);
            }

            mViewPagerParent.setTranslationY(-mScrollDistance);
            if (mTopFab != null) {
                mTopFab.setTranslationY(-mScrollDistance);
            }

            if (showBottomFab() && autoHideBottomFab()) {
                if (dy <= 0) {
                    if (mBottomFab.getVisibility() != View.VISIBLE) {
                        mBottomFab.show();
                    }
                } else if (mBottomFab.getVisibility() == View.VISIBLE) {
                    mBottomFab.hide();
                }
            }
        }

        private void fadeAppBarLayout(boolean fade) {
            if (mFade != fade) {
                mFade = fade;

                if (mAlphaAnimator != null) {
                    mAlphaAnimator.cancel();
                }

                mAlphaAnimator = ValueAnimator.ofFloat(fade ? 1f : 0f, fade ? 0f : 1f);
                mAlphaAnimator.addUpdateListener(animation -> setAppBarLayoutAlpha(Math.round(255 * (float) animation.getAnimatedValue())));
                mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mAlphaAnimator = null;
                    }
                });
                mAlphaAnimator.start();
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (mAppBarLayout == null || newState != 0 || mAppBarLayoutDistance == 0
                    || (mAppBarLayoutDistance == mAppBarLayout.getHeight() && mScrollDistance != 0)) {
                return;
            }

            boolean show = mAppBarLayoutDistance < mAppBarLayout.getHeight() * 0.5f
                    || mScrollDistance <= mViewPagerParent.getHeight();
            ValueAnimator animator = ValueAnimator.ofInt(mAppBarLayoutDistance, show ? 0 : mAppBarLayout.getHeight());
            animator.addUpdateListener(animation -> {
                mAppBarLayoutDistance = (int) animation.getAnimatedValue();
                mAppBarLayout.setTranslationY(-mAppBarLayoutDistance);
            });
            animator.start();
        }
    }

    void showProgress() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    mProgress.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    if (mTopFab != null && showTopFab()) {
                        mTopFab.hide();
                    }
                    if (mBottomFab != null && showBottomFab()) {
                        mBottomFab.hide();
                    }
                }
            });
        }
    }

    void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mViewPagerParent.setVisibility(View.VISIBLE);
        if (mTopFab != null && showTopFab()) {
            mTopFab.show();
        }
        if (mBottomFab != null && showBottomFab()) {
            mBottomFab.show();
        }
        adjustScrollPosition();
    }

    private boolean isForeground() {
        return false;
    }

    private BaseFragment getForegroundFragment() {
        return null;
    }

    private void dismissForeground() {
        float translation = mForegroundParent.getTranslationY();
        mForegroundAnimator = ValueAnimator.ofFloat(translation, mForegroundHeight);
        mForegroundAnimator.addUpdateListener(animation -> mForegroundParent.setTranslationY((float) animation.getAnimatedValue()));
        mForegroundAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mForegroundParent.setVisibility(View.GONE);
                mForegroundVisible = false;
                mForegroundAnimator = null;
            }
        });
        mForegroundAnimator.start();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (showViewPager()) {
            menu.add(0, 0, Menu.NONE, R.string.options)
                    .setIcon(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_launcher_preview))
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        if (showTopFab()) {
            menu.add(0, 1, Menu.NONE, R.string.more)
                    .setIcon(getTopFabDrawable())
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else if (showBottomFab()) {
            menu.add(0, 1, Menu.NONE, R.string.more)
                    .setIcon(getBottomFabDrawable())
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                ViewUtils.showDialog(getChildFragmentManager(),
                        ViewPagerDialog.newInstance(getBannerHeight(), mViewPagerFragments));
                return true;
            case 1:
                if (showTopFab()) {
                    onTopFabClick();
                } else if (showBottomFab()) {
                    onBottomFabClick();
                }
                return true;
        }
        return false;
    }

    private boolean showViewPager() {
        return true;
    }

    protected boolean showTopFab() {
        return false;
    }

    protected Drawable getTopFabDrawable() {
        return null;
    }

    protected void onTopFabClick() {
    }

    private boolean showBottomFab() {
        return false;
    }

    private Drawable getBottomFabDrawable() {
        return null;
    }

    private void onBottomFabClick() {
    }

    private boolean autoHideBottomFab() {
        return true;
    }

    View getRootView() {
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPoolExecutor == null) {
            mPoolExecutor = new ScheduledThreadPoolExecutor(1);
            mPoolExecutor.scheduleWithFixedDelay(mScheduler, 0, 500,
                    TimeUnit.MILLISECONDS);
        }
        for (RecyclerViewItem item : mItems) {
            item.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPoolExecutor != null) {
            mPoolExecutor.shutdown();
            mPoolExecutor = null;
        }
        for (RecyclerViewItem item : mItems) {
            item.onPause();
        }
    }

    private Runnable mScheduler = () -> {
        refreshThread();

        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (getActivity() != null) {
                refresh();
            }
        });
    };

    private void refreshThread() {
    }

    private void refresh() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItems.clear();
        mRecyclerViewAdapter = null;
        setAppBarLayoutAlpha(255);
        if (mAppBarLayout != null && !isForeground()) {
            mAppBarLayout.setTranslationY(0);
            ViewCompat.setElevation(mAppBarLayout, 0);
        }
        if (mLoader != null) {
            mLoader.cancel(true);
            mLoader = null;
        }
        for (RecyclerViewItem item : mItems) {
            item.onDestroy();
        }
    }

    Handler getHandler() {
        return mHandler;
    }

}
