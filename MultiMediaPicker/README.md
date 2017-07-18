# MultiMediaPickerCropper
Multiple media picker and cropper is a library to pick files, images and videos from storage directory.
You can also crop multiple images from gallery or camera.


# Version
1.4.4

# Installation

To use this library in your android project, just simply add the following dependency into your build.gradle

```java
dependencies {
    compile 'com.crop.multiple:multipleImagePickerLib:1.4.4'
}
```

# Third Party Dependencies

This library uses the following third parties

```java
dependencies {
    compile 'com.android.support:appcompat-v7:25.0.0'
    compile 'com.android.support:design:25.0.0'
    compile 'com.android.support:recyclerview-v7:25.0.0'
    compile files('libs/universal-image-loader-1.9.4.jar')
    compile 'com.facebook.fresco:fresco:0.14.1'
    compile 'com.github.bumptech.glide:glide:3.7.0'
}
```

# ToolBar Color

To match your app toolbar color, set this attribute in your application.

```java
Define.ACTIONBAR_COLOR = getResources().getColor(R.color.colorPrimary);
```

# Theme Support
All activities uses a custom toolbar with NoActionBar Theme, please use this theme in your main app otherwise app will crash.
To, override this, please declare all activities in your manifest with NoActionBar Theme.

# Usage
Features supported in this library

MediaBuilder Specifications :-
```java
    takeVideo() // Takes a video. Default is image
    setImageQuality(int size) //Takes the size between 0 to 100
    setPlayIcon(int playResId) //Sets the play icon preview. Have to be a valid resource id
    setVideoSize(int size) // Sets the size of video in MBs for camera. Default is -1
    setVideoDuration(long seconds)// Sets the duration of video in seconds for camera. Default is -1
    fromGallery() // Picks media file from gallery
    fromCamera() //Captures media file from camera. Default is gallery
    doCropping() // Cropping functionality for images only. Default is false
    isSquareCrop() // Crops the image in 1:1 ratio
    withAspectRatio(int x, int y) // Custom aspect ratio. Set isSquareCrop() to false then only this method will work
    MediaFactory start(MediaBuilder mediaBuilder) // Takes the builder object and starts the media capturing process

    MediaFactory.create().clearCache(MainActivity.this); // Clears local cache of compressed images from sd card
```

And to get the files, you have to add this piece of code to Activity's `onActivityResult` in your app.
```java
ArrayList<String> pathArrayList = mediaFactory.onActivityResult(requestCode, resultCode, data);
```

# Nougat Support
Post nougat, process for selecting image from camera has been changed. You need to make the following changes to your manifest :-
```java
 <provider
     android:name="android.support.v4.content.FileProvider"
     android:authorities="com.app.multiimagepickercropper.provider"
     android:exported="false"
     android:grantUriPermissions="true">
     <meta-data
           android:name="android.support.FILE_PROVIDER_PATHS"
           android:resource="@xml/provider_paths" />
 </provider>

 //Add this line of code anywhere in your class
 Define.MEDIA_PROVIDER = getString(R.string.image_provider);
 //image_provider should be same as the authorities you will give above in the provider
```java

# License

Apache 2.0
