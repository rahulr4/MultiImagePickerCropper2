# MultiMediaPickerCropper
Multiple media picker and cropper is a library to pick files, images and videos from storage directory.
You can also crop multiple images from gallery or camera.


# Version
1.3.6

# Installation

To use this library in your android project, just simply add the following dependency into your build.gradle

```java
dependencies {
    compile 'com.crop.multiple:multipleImagePickerLib:1.3.6'
}
```

# Third Party Dependencies

This library uses the following third parties

```java
dependencies {
    compile 'com.android.support:appcompat-v7:24.1.1'
    compile 'com.android.support:design:24.1.1'
    compile 'com.android.support:recyclerview-v7:24.1.1'
    compile files('libs/universal-image-loader-1.9.4.jar')
    compile 'com.facebook.fresco:fresco:0.13.0'
    compile 'com.github.bumptech.glide:glide:3.7.0'
}
```

```java
dependencies {
    compile 'com.crop.multiple:multipleImagePickerLib:1.3.6'
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

# License

Apache 2.0
