package com.smartpack.smartflasher.views.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.smartpack.smartflasher.utils.ViewUtils;

/**
 * Adapted from https://github.com/Grarak/KernelAdiutor by Willi Ye.
 */

public class GenericSelectView extends ValueView {

    public interface OnGenericValueListener {
        void onGenericValueSelected(GenericSelectView genericSelectView, String value);
    }

    private String mValueRaw;
    private OnGenericValueListener mOnGenericValueListener;
    private int mInputType = -1;
    private boolean mShowDialog;

    @Override
    public void onRecyclerViewCreate(Activity activity) {
        super.onRecyclerViewCreate(activity);

        if (mShowDialog) {
            showDialog(activity);
        }
    }

    @Override
    public void onCreateView(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(v.getContext());
            }
        });
        super.onCreateView(view);
    }

    public void setValueRaw(String value) {
        mValueRaw = value;
    }

    public void setOnGenericValueListener(OnGenericValueListener onGenericValueListener) {
        mOnGenericValueListener = onGenericValueListener;
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

}
