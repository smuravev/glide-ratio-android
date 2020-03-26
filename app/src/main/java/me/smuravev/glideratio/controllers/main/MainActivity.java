package me.smuravev.glideratio.controllers.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import me.smuravev.glideratio.R;
import me.smuravev.glideratio.Utils;
import me.smuravev.glideratio.controllers.BaseActivity;

public class MainActivity extends BaseActivity implements SensorEventListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_PERMISSIONS = 1010;

    private boolean mCanRequestRequiredPermissions = true;

    private static double GLIDE_RATIO_LIMIT = 20.0;

    private static long REFRESH_INTERVAL_MSEC  = 200; // 200 msec

    private AtomicLong mLastRefreshTimestamp = new AtomicLong(0);

    @Nullable
    private SensorManager mSensorManager;

    @Nullable
    private Sensor mAccelerometer;

    @Nullable
    private Sensor mMagnetometer;

    @Nullable
    private float[] mGravity;

    @Nullable
    private float[] mGeomagnetic;

    @Nullable
    private CameraSurfaceView mCameraSurfaceView;

    @Nullable
    private Camera mCamera;

    private FrameLayout mCameraPreview;

    private TextView mGlideRatioTitle;

    private ImageView mDirectionImage;

    private TextView mDirectionTitle;

    @Override
    protected int contentViewResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Let's initialize view
        mGlideRatioTitle = findViewById(R.id.glideRatioTitle);
        mDirectionImage = findViewById(R.id.directionImage);
        mDirectionTitle = findViewById(R.id.directionTitle);

        // Let's initialize sensors
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometer != null) {
                mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }

            mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (mMagnetometer != null) {
                mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        mCameraPreview = findViewById(R.id.cameraPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Let's check if required sensors installed on device. Stop the app if not.
        if (mSensorManager == null || mAccelerometer == null || mMagnetometer == null) {
            showSimpleDialog(
                    getString(R.string.sensors_required_title),
                    getString(R.string.sensors_required_message),
                    this::finish
            );
            return;
        }

        // if we don't have the camera permission then let's request it
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestRequiredPermissions();
            return;
        }

        // Let's initialize camera
        try {
            mCameraPreview.removeAllViews();

            mCamera = Camera.open();
            if (mCamera == null) {
                throw new Exception("camera is null.");
            }

            mCameraSurfaceView = new CameraSurfaceView(MainActivity.this, mCamera);
            mCameraPreview.addView(mCameraSurfaceView);
        } catch (Exception e) {
            Log.e(TAG, "Unable to open camera due error", e);

            mCamera = null;
            mCameraSurfaceView = null;

            showSimpleDialog(
                    getString(R.string.error),
                    getString(R.string.camera_initialization_failed_message),
                    this::finish
            );
        }

        // Let's check if camera initialized well
        if (mCamera == null || mCameraSurfaceView == null) {
            showSimpleDialog(
                    getString(R.string.error),
                    getString(R.string.camera_initialization_failed_message),
                    this::finish
            );
            return;
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister sensors listeners.
        if (mSensorManager != null) {
            if (mAccelerometer != null) {
                mSensorManager.unregisterListener(this, mAccelerometer);
            }
            if (mMagnetometer != null) {
                mSensorManager.unregisterListener(this, mMagnetometer);
            }
        }

        super.onDestroy();
    }

    /**
     * Starts the process of requesting required permissions.
     * The CAMERA permission in our case.
     *
     * <p>{@link #mCanRequestRequiredPermissions} can stop this function from doing anything.
     */
    protected void requestRequiredPermissions() {
        if (!mCanRequestRequiredPermissions) {
            // If this is in progress, don't do it again.
            return;
        }
        mCanRequestRequiredPermissions = false;

        List<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }

        if (!permissions.isEmpty()) {
            // Request the permissions
            requestPermissions(permissions.toArray(new String[0]), RC_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (requestCode != RC_PERMISSIONS) {
            return;
        }

        boolean permitted = true;
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (Manifest.permission.CAMERA.equals(permission)) {
                int result = results[i];
                permitted = permitted && (result == PackageManager.PERMISSION_GRANTED);
            }
        }

        if (permitted) {
            return;
        }

        showConfirmDialog(
                getString(R.string.permissions_required_title),
                getString(R.string.permissions_required_message),
                new ConfirmDialogDelegate() {
                    @Override
                    public void onOk() {
                        // If Ok was hit, bring up the Settings app.
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        startActivity(intent);
                    }

                    @Override
                    public void onCancel() {
                        finish();
                    }

                    @Override
                    public void onClosed() {
                    }
                }
        );
    }

    public void onClickHelpButton(View view) {
        showHelp();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // If type is accelerometer only assign values to global property mGravity
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }

        if (mGravity == null || mGeomagnetic == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if ((now - mLastRefreshTimestamp.longValue()) <= REFRESH_INTERVAL_MSEC) {
            return;
        }
        mLastRefreshTimestamp.set(now);

        float[] rotationMatrix = new float[9];
        float[] inclinationMatrix = new float[9];

        boolean success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, mGravity, mGeomagnetic);

        if (success) {
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            // If the roll is positive, you're in reverse landscape (landscape right),
            // and if the roll is negative you're in landscape (landscape left).
            // Similarly, you can use the pitch to differentiate between portrait and reverse portrait.
            // If the pitch is positive, you're in reverse portrait, and if the pitch is negative you're in portrait.
            // orientation -> azimut, pitch and roll
            float pitch = orientation[1];
            float roll = orientation[2];

            float[] gravity = mGravity.clone();

            double normOfGravity = Math.sqrt(gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2]);

            // Normalize the accelerometer vector
            gravity[0] = (float) (gravity[0] / normOfGravity);
            gravity[1] = (float) (gravity[1] / normOfGravity);
            gravity[2] = (float) (gravity[2] / normOfGravity);

            double tiltDegrees = Math.toDegrees(Math.acos(gravity[2]));

            refresh(tiltDegrees);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void refresh(double tiltDegrees) {
        Double angleDegrees = null;
        if (tiltDegrees > 0 && tiltDegrees < 90) { // from takeoff
            angleDegrees = 90 - tiltDegrees;
            showFromTakeoff();
        } else if (tiltDegrees > 90 && tiltDegrees < 180) { // from landing
            angleDegrees = tiltDegrees - 90;
            showFromLanding();
        }
        if (angleDegrees == null) {
            showFlat();
            return;
        }

        double glideRatio = 1.0 / Math.tan(Math.toRadians(angleDegrees)); // cotan

        if (glideRatio > GLIDE_RATIO_LIMIT) {
            showFlat();
            return;
        }

        Utils.runOnUiThread(() -> {
            mGlideRatioTitle.setText(String.format(
                    Locale.getDefault(),
                    "%.1f", Math.min(glideRatio, GLIDE_RATIO_LIMIT)
            ));
        });
    }

    private void showFromTakeoff() {
        Utils.runOnUiThread(() -> {
            mDirectionImage.setImageDrawable(getDrawable(R.drawable.ic_arrow_bottom_left));
            mDirectionTitle.setText(R.string.from_takeoff);

            mGlideRatioTitle.setVisibility(View.VISIBLE);
        });
    }

    private void showFromLanding() {
        Utils.runOnUiThread(() -> {
            mDirectionImage.setImageDrawable(getDrawable(R.drawable.ic_arrow_top_right));
            mDirectionTitle.setText(R.string.from_landing);

            mGlideRatioTitle.setVisibility(View.VISIBLE);
        });
    }

    private void showFlat() {
        Utils.runOnUiThread(() -> {
            mDirectionImage.setImageDrawable(getDrawable(R.drawable.ic_line_three));
            mDirectionTitle.setText(R.string.flat);

            mGlideRatioTitle.setVisibility(View.INVISIBLE);
        });
    }
}
