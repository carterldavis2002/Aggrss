<p align="center">
  <img align="center" src="https://github.com/carterldavis2002/Aggrss/blob/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png">
</p>

# Aggrss
An RSS 2.0 aggregator for Android aimed at keeping you up-to-date with your favorite feeds through high performance and a simplistic UI. Built-in features include the
ability to search items in added feeds by title and a range of dates for maximum selectivity, day, night, and automatic themes to customize UI look, validation so you are
notified immediately if feed information was entered incorrectly or the feed is broken, and much more!

<p align="center">
  <img align="center" src="https://github.com/carterldavis2002/Aggrss/blob/master/screenshots.gif">
</p>

## Download
Download one of the [release APKs](https://github.com/carterldavis2002/Aggrss/releases) to an Android device and open it, following the prompts to install the app.

## Development Setup
### Requirements
- [Android Studio](https://developer.android.com/studio)
- Git

1. Clone the repo
```
git clone https://github.com/carterldavis2002/Aggrss.git
```
2. Open the project directory in Android Studio
3. To build + run a debug/release version of the app, navigate to **Build > Select Build Variant...** and select the desired variant. Pressing the **Run 'app'** button
will then cause the app variant to be installed and started on the selected virtual/physical device.

  **NOTE:** To build + publish a release version of the app, the app needs to be properly signed. To do this, create a keystore by navigating to **Build > Generate Signed Bundle
   / APK...** and press **Create new...** under **Key store path** after choosing whether to generate an Android App Bundle or APK. After creating a keystore, open
   the ``keystore.properties`` file in the root of the project directory and enter the information for each property respective of the created keystore. You will now
   be able to build a release variant. **DO NOT COMMIT THE** ``keystore.properties`` **FILE TO GIT AFTER ENTERING YOUR INFORMATION AS IT IS SENSITIVE AND
   UNIQUELY IDENTIFIES YOUR APP**.
   
4. To generate an APK that can be distributed, navigate to **View > Tool Windows > Terminal** to open a terminal and enter ``./gradlew build``. The debug/release 
APKs can then be found in ``app/build/outputs/apk``.
