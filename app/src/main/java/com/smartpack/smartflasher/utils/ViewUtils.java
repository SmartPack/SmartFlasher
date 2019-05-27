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

package com.smartpack.smartflasher.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.AppCompatEditText;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.views.dialog.Dialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashSet;
import java.util.Set;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class ViewUtils {

    public static int getTextSecondaryColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, value, true);
        return value.data;
    }

    public static Drawable getSelectableBackground(Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});
        Drawable drawable = typedArray.getDrawable(0);
        typedArray.recycle();
        return drawable;
    }

    public static void showDialog(FragmentManager manager, DialogFragment fragment) {
        FragmentTransaction ft = manager.beginTransaction();
        Fragment prev = manager.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        fragment.show(ft, "dialog");
    }

    public static void dismissDialog(FragmentManager manager) {
        FragmentTransaction ft = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag("dialog");
        if (fragment != null) {
            ft.remove(fragment).commit();
        }
    }

    public static int getColorPrimaryColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
        return value.data;
    }

    public static int getColorPrimaryDarkColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, value, true);
        return value.data;
    }

    public static int getThemeAccentColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    public interface OnDialogEditTextListener {
        void onClick(String text);
    }

    public static Dialog dialogBuilder(CharSequence message, DialogInterface.OnClickListener negativeListener,
                                       DialogInterface.OnClickListener positiveListener,
                                       DialogInterface.OnDismissListener dismissListener, Context context) {
        Dialog dialog = new Dialog(context).setMessage(message);
        if (negativeListener != null) {
            dialog.setNegativeButton(context.getString(R.string.cancel), negativeListener);
        }
        if (positiveListener != null) {
            dialog.setPositiveButton(context.getString(R.string.ok), positiveListener);
        }
        if (dismissListener != null) {
            dialog.setOnDismissListener(dismissListener);
        }
        return dialog;
    }

    private static final Set<CustomTarget> mProtectedFromGarbageCollectorTargets = new HashSet<>();

    private static class CustomTarget implements Target {
        private ImageView mImageView;
        private int mMaxWidth;
        private int mMaxHeight;

        private CustomTarget(ImageView imageView, int maxWidth, int maxHeight) {
            mImageView = imageView;
            mMaxWidth = maxWidth;
            mMaxHeight = maxHeight;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mImageView.setImageBitmap(scaleDownBitmap(bitmap, mMaxWidth, mMaxHeight));
            mProtectedFromGarbageCollectorTargets.remove(this);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mProtectedFromGarbageCollectorTargets.remove(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

    public static Bitmap scaleDownBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int newWidth = width;
        int newHeight = height;

        if (maxWidth != 0 && newWidth > maxWidth) {
            newHeight = Math.round((float) maxWidth / newWidth * newHeight);
            newWidth = maxWidth;
        }

        if (maxHeight != 0 && newHeight > maxHeight) {
            newWidth = Math.round((float) maxHeight / newHeight * newWidth);
            newHeight = maxHeight;
        }

        return width != newWidth || height != newHeight ? resizeBitmap(bitmap, newWidth, newHeight) : bitmap;
    }

    private static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

}
