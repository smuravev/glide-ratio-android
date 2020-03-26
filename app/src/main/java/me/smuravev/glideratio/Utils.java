//
//  Created by Sergey Muravev on 26.03.2020.
//  Copyright © 2020 Sergey Muravev. All rights reserved.
//

package me.smuravev.glideratio;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import java.util.UUID;

public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    public static void checkOnMainThread() throws IllegalStateException {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("This method must be executed from main UI thread");
        }
    }

    public static void runOnUiThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static void fadeIn(View v) {
        if (v != null) {
            Animation animFadeIn = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_in);
            v.startAnimation(animFadeIn);
        }
    }

    public static int dpToPx(final Context context, final int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int pxToDp(final Context context, final int px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }
}
