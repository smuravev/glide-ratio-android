# GlideRatio (Android) project

GlideRatio is Android application to calculate minimal glide ratio (of a wing) required to fly your line in
Speedflying, Paragliding, Basejumping and other Paralpnisme activities.

It's simple (useful) application with minimalistic UI / UX to use it in your Hike&Fly missions.

Application uses rear camera, accelerometer and magnetometer (compass) sensors to detect the tilt angle of your flying line and then
to calculate the minimal glide ratio.

You only need to point your camera to landing zone or to potential takeoff area.

Fly safe.


# License

The application (source code) is distributed under is distributed under [Apache-2.0](LICENSE) license.

# How to build project

## Build Debug version

    $ ./gradlew clean lintDebug assembleDebug

Built artifact: _$PROJECT_HOME/app/build/outputs/apk/debug/app-debug.apk_

## Produce Release build to publish it to GooglePlay

    $ ./gradlew clean lintRelease assembleRelease

Built artifact: _$PROJECT_HOME/app/build/outputs/apk/release/app-release.apk_

**NOTICE:** To build signed release build, please email me to get required signing properties.

# How to run project

## Run project in the emulator

1. Run AVD manager then select necessary (prepared) VirtualDevice and run it. **NOTE:** If you have no configured VirtualDevice then you must prepare it first (in the AVD manager).

2. Check that you running VirtualDevice instance is visible through _adb_ tool:

        $ $ANDROID_SDK_HOME/platform-tools/adb devices
        For example (based on my local environment): $ ~/workspace/android/sdk/platform-tools/adb devices

        List of devices attached
        emulator-5554	device

    As you can see _emulator-5554_ VirtualDevice is running/connected (especially such device will be used later for installation of our built APK file).

3. Install built APK file on your running/connected device:

        $ ./gradlew installDebug

4. Go to your emulator then to applications and click application icon.

## Run project on real device ###

1. Connect your device to computer through USB cable.

2. Make sure you set "Settings > Applications > Development > Debugging through USB" [x]

3. Follow instructions like for emulator (see above). Eg: `$ ./gradlew installDebug`

