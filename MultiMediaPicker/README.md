# MultiMediaPickerCropper
Multiple media picker and cropper is a library to pick files, images and videos from storage directory.
You can also crop multiple images from gallery or camera.


# Version
1.2.5

# Installation

To use this library in your android project, just simply add the following dependency into your build.gradle

```java
dependencies {
    compile 'com.crop.multiple:multipleImagePickerLib:1.2.5'
}
```

# Third Party Dependencies

This library uses the following third parties

```java
dependencies {
    compile 'com.android.support:appcompat-v7:23.2.1'
    compile 'com.android.support:design:23.2.1'
    compile 'com.android.support:recyclerview-v7:23.2.1'
    compile files('libs/universal-image-loader-1.9.4.jar')
    compile 'com.facebook.fresco:fresco:0.7.0'
}
```

```java
dependencies {
    compile 'com.crop.multiple:multipleImagePickerLib:1.2.5'
}
```
# ToolBar Color

To match your app toolbar color, set this attribute in your application.

```java
Define.ACTIONBAR_COLOR = getResources().getColor(R.color.colorPrimary);
```

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
    getSingleMediaFiles() // Picks single media from said location
    getMultipleMediaFiles()// Picks multiple media from said location. Default is Single Media
    
    MediaFactory start(MediaBuilder mediaBuilder) // Takes the builder object and starts the media capturing process

```

And to get the images, you have to add this piece of code to Activity's `onActivityResult` in your app.
```java
ArrayList<String> pathArrayList = mediaFactory.onActivityResult(requestCode, resultCode, data);
```

# License

Apache 2.0
