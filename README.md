# MultiMediaPickerCropper
Multiple media picker and cropper is a library to pick and crop multiple images from gallery or camera.
Added support of picking single and multiple videos.

# Version
1.1.3

# Installation

To use this library in your android project, just simply add the following dependency into your build.gradle

```java
dependencies {
    compile 'com.crop.multiple:multipleImagePickerLib:1.1.3'
}
```

# Usage
We support various features using a builder pattern

```java

MediaFactory.MediaBuilder mediaBuilder = 
    new MediaFactory.MediaBuilder(MainActivity.this)
    .getSingleMediaFiles() // Method to pick single media file
    .takeVideo() //Method to take a video. Default is image
    .setVideoDuration(10) // Works with video capture from camera. Duration in seconds
    .setVideoSize(3) // Works with video capture from camera. Size in MB 
    .fromGallery(); // Location Specific method. Gallery or camera
mediaFactory = MediaFactory.create().start(mediaBuilder);

```

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
String[] all_path = mediaFactory.onActivityResult(requestCode, resultCode, data);
```

# License

Apache 2.0
