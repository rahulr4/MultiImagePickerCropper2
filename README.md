# MultiImagePickerCropper
Multiple image picker cropper is a library to pick and crop multiple images from gallery or camera.

# Version
1.0.0

# Installation

To use this library in your android project, just simply add the following dependency into your build.gradle

```sh
dependencies {
    compile 'com.crop.multiple:multipleImagePickerLib:1.0.0'
}
```

# Usage
To pick single image from gallery, camera :-

```java
//From Gallery
Intent intent = new Intent(MainActivity.this, MultipleImagePreviewActivity.class);
intent.setAction(Action.ACTION_PICK);
startActivityForResult(intent, GALLERY_APP);
```

```java
//From Camera
Intent intent = new Intent(MainActivity.this, CameraPickActivity.class);
intent.setAction(Action.ACTION_PICK);
startActivityForResult(intent, GALLERY_APP);
```

To pick multiple images from gallery, camera :-

```java
//From Gallery
Intent intent = new Intent(MainActivity.this, MultipleImagePreviewActivity.class);
intent.setAction(Action.ACTION_MULTIPLE_PICK);
startActivityForResult(intent, GALLERY_APP);
```

```java
//From Camera
Intent intent = new Intent(MainActivity.this, CameraPickActivity.class);
intent.setAction(Action.ACTION_MULTIPLE_PICK);
startActivityForResult(intent, GALLERY_APP);
```

And to get the images, you have to add this piece of code to Activity's `onActivityResult` in your app.
```java
if (requestCode == GALLERY_APP || requestCode == CAMERA_APP) {
String[] all_path = data.getStringArrayExtra("all_path");
for (String string : all_path) {
  // Do something with image path
}
```

# License

Apache 2.0
