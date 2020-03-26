//
//  Created by Sergey Muravev on 26.03.2020.
//  Copyright © 2020 Sergey Muravev. All rights reserved.
//

package me.smuravev.glideratio.controllers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import me.smuravev.glideratio.R;
import me.smuravev.glideratio.Utils;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String TAG = BaseActivity.class.getSimpleName();

    private boolean mTouchEventsEnabled = true;

    @Nullable
    private ProgressDialog mProgressDialog;

    protected abstract int contentViewResId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(contentViewResId());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mTouchEventsEnabled) {
            return super.dispatchTouchEvent(ev);
        }

        return true;
    }

    protected void enableTouchEvents() {
        mTouchEventsEnabled = true;
    }

    protected void disableTouchEvents() {
        mTouchEventsEnabled = false;
    }

    protected void showHelp() {
        if (isFinishing()) {
            return;
        }
        Utils.runOnUiThread(() -> {
            try {
                View helpView = getLayoutInflater().inflate(R.layout.view_help, null);

                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
                bottomSheetDialog.setContentView(helpView);
                bottomSheetDialog.setCanceledOnTouchOutside(false);
                bottomSheetDialog.setCancelable(true);

                int height = getWindow().getDecorView().getHeight();
                BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) helpView.getParent());
                mBehavior.setPeekHeight(
                        height
                        // Utils.dpToPx(this, 300)
                );

                bottomSheetDialog.show();
            } catch (Exception e) {
                Log.e(TAG, "Unable to open help view due error", e);
            }
        });
    }

    protected void showProgressIndicator(@Nullable String message) {
        Utils.runOnUiThread(() -> {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            mProgressDialog = new ProgressDialog(this);
            if (message != null) {
                mProgressDialog.setMessage(message);
            }
            mProgressDialog.show();
        });
    }

    protected void hideProgressIndicator() {
        Utils.runOnUiThread(() -> {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        });
    }

    protected void showMessage(@NonNull String message) {
        if (message.trim().isEmpty()) {
            return;
        }

        Utils.runOnUiThread(() -> {
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        });
    }

    protected void showSimpleDialog(
            @NonNull String title,
            @NonNull String message
    ) {
        showSimpleDialog(title, message, getString(R.string.ok));
    }

    protected void showSimpleDialog(
            @NonNull String title,
            @NonNull String message,
            @NonNull String buttonTitle
    ) {
        showSimpleDialog(title, message, buttonTitle, null);
    }

    protected void showSimpleDialog(
            @NonNull String title,
            @NonNull String message,
            @Nullable final SimpleDialogDelegate delegate
    ) {
        showSimpleDialog(title, message, getString(R.string.ok), delegate);
    }

    protected void showSimpleDialog(
            @NonNull String title,
            @NonNull String message,
            @NonNull String buttonTitle,
            @Nullable final SimpleDialogDelegate delegate
    ) {
        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonTitle, (dialog, which) -> {
                })
                .setOnDismissListener(dialog -> {
                    if (delegate != null) {
                        delegate.onClosed();
                    }
                });

        Utils.runOnUiThread(builder::show);
    }

    protected void showConfirmDialog(
            @NonNull String title,
            @NonNull String message,
            @Nullable final ConfirmDialogDelegate delegate
    ) {
        showConfirmDialog(title, message, getString(R.string.ok), getString(R.string.cancel), delegate);
    }

    protected void showConfirmDialog(
            @NonNull String title,
            @NonNull String message,
            @NonNull String positiveButtonTitle,
            @NonNull String negativeButtonTitle,
            @Nullable final ConfirmDialogDelegate delegate
    ) {
        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonTitle, (dialog, which) -> {
                    if (delegate != null) {
                        delegate.onOk();
                    }
                })
                .setNegativeButton(negativeButtonTitle, (dialog, which) -> {
                    if (delegate != null) {
                        delegate.onCancel();
                    }
                })
                .setOnDismissListener(dialog -> {
                    if (delegate != null) {
                        delegate.onClosed();
                    }
                });

        Utils.runOnUiThread(builder::show);
    }

    protected interface SimpleDialogDelegate {
        void onClosed();
    }

    protected interface ConfirmDialogDelegate extends SimpleDialogDelegate {
        void onOk();
        void onCancel();
    }
}
