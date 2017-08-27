package com.example.maraj.imagepickerdemo.image_picker;

public abstract class ImagePickerDefaultCallback implements ImagePicker.Callbacks {

    @Override
    public void onImagePickerError(Exception e, ImagePicker.ImageSource source, int type) {
    }

    @Override
    public void onCanceled(ImagePicker.ImageSource source, int type) {
    }
}