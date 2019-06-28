# RadarBeacon

This app can be used for locating BLE devices in the direct vicinity. It requires the user to turn around holding the phone, thereby using variance in received RSSI values to approximate a distance and direction to the device.

Note that for adequate results, the BLE device needs to broadcast on any advertising channel about once a second.
Also, the phone should have a real compass.

## Build

This is an Android Studio Project. It can be built directly in Android Studio or with Gradle using `./gradlew build` from the project root.

For details see https://developer.android.com/studio/run

Written for Android Version 4.4.2
