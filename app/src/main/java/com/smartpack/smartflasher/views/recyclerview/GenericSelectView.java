package com.smartpack.smartflasher.views.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;

import com.smartpack.smartflasher.R;
import com.smartpack.smartflasher.utils.ViewUtils;

/**
 * Adapted from https://github.com/Grarak/KernelAdiutor by Willi Ye.
 */

public class GenericSelectView extends ValueView {

    public interface OnGenericValueListener {
        void onGenericValueSelected(GenericSelectView genericSelectView, String value);
    }

    public interface OnMenuListener {
        void onMenuReady(GenericSelectView genericSelectView, PopupMenu popupMenu);
    }

    private AppCompatImageButton mMenuIconView;
    private Drawable mMenuIcon;
    private String mValueRaw;
    private OnGenericValueListener mOnGenericValueListener;
    private int mInputType = -1;
    private boolean mShowDialog;
    private PopupMenu mPopupMenu;
    private OnMenuListener mOnMenuListener;

    @Override
    public void onRecyclerViewCreate(Activity activity) {
        super.onRecyclerViewCreate(activity);

        if (mShowDialog) {
            showDialog(activity);
        }
    }

    @Override
    public void onCreateView(View view) {
        view.setOnClickListener(v -> showDialog(v.getContext()));

        mMenuIconView = view.findViewById(R.id.menu_button);
        mMenuIconView.setOnClickListener(v -> {
            if (mPopupMenu != null) {
                mPopupMenu.show();
            }
        });
        super.onCreateView(view);
    }

    private void setValueRaw(String value) {
        mValueRaw = value;
    }

    public void setMenuIcon(Drawable menuIcon) {
        mMenuIcon = menuIcon;
        refresh();
    }

    public void setOnGenericValueListener(OnGenericValueListener onGenericValueListener) {
        mOnGenericValueListener = onGenericValueListener;
    }

    public void setOnMenuListener(OnMenuListener onMenuListener) {
        mOnMenuListener = onMenuListener;
        refresh();
    }

    public void setInputType(int inputType) {
        mInputType = inputType;
    }

    private void showDialog(Context context) {
        if (mValueRaw == null) {
            mValueRaw = getValue();
        }
        if (mValueRaw == null) return;

        mShowDialog = true;
        ViewUtils.dialogEditText(mValueRaw, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }, new ViewUtils.OnDialogEditTextListener() {
            @Override
            public void onClick(String text) {
                setValueRaw(text);
                if (mOnGenericValueListener != null) {
                    mOnGenericValueListener.onGenericValueSelected(GenericSelectView.this, text);
                }
            }
        }, mInputType, context).setTitle(getTitle()).setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mShowDialog = false;
                    }
                }).show();
    }

    @Override
    protected void refresh() {
        super.refresh();
        if (mMenuIconView != null && mMenuIcon != null && mOnMenuListener != null) {
            mMenuIconView.setImageDrawable(mMenuIcon);
            mMenuIconView.setVisibility(View.VISIBLE);
            mPopupMenu = new PopupMenu(mMenuIconView.getContext(), mMenuIconView);
            mOnMenuListener.onMenuReady(this, mPopupMenu);
        }
    }

}
