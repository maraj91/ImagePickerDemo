package com.example.maraj.imagepickerdemo.image_picker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"unused", "FieldCanBeLocal", "ResultOfMethodCallIgnored"})
public class ImagePicker implements ImagePickerConfig {
    public enum ImageSource {
        GALLERY, CAMERA
    }

    public interface Callbacks {
        void onImagePickerError(Exception e, ImageSource source, int type);

        void onImagePicked(File imageFile, ImageSource source, int type);

        void onCanceled(ImageSource source, int type);
    }

    private static final String KEY_PHOTO_URI = "maraj.image.photo_uri";
    private static final String KEY_LAST_CAMERA_PHOTO = "maraj.image.last_photo";
    private static final String KEY_TYPE = "maraj.image.type";

    private static Uri createCameraPictureFile(Context context) throws IOException {
        File imagePath = ImagePickerFiles.getCameraPicturesLocation(context);
        Uri uri = Uri.fromFile(imagePath);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(KEY_PHOTO_URI, uri.toString());
        editor.putString(KEY_LAST_CAMERA_PHOTO, imagePath.toString());
        editor.apply();
        return uri;
    }

    private static Intent createGalleryIntent(Context context, int type) {
        storeType(context, type);
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    private static Intent createCameraIntent(Context context, int type) {
        storeType(context, type);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri capturedImageUri = createCameraPictureFile(context);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return intent;
    }

    @SuppressLint("ApplySharedPref")
    private static void storeType(Context context, int type) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_TYPE, type).commit();
    }

    private static int restoreType(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_TYPE, 0);
    }

    public static void openGallery(Activity activity, int type) {
        Intent intent = createGalleryIntent(activity, type);
        activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openGallery(Fragment fragment, int type) {
        Intent intent = createGalleryIntent(fragment.getContext(), type);
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openGallery(android.app.Fragment fragment, int type) {
        Intent intent = createGalleryIntent(fragment.getActivity(), type);
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openCamera(Activity activity, int type) {
        Intent intent = createCameraIntent(activity, type);
        activity.startActivityForResult(intent, REQ_TAKE_PICTURE);
    }

    public static void openCamera(Fragment fragment, int type) {
        Intent intent = createCameraIntent(fragment.getActivity(), type);
        fragment.startActivityForResult(intent, REQ_TAKE_PICTURE);
    }

    public static void openCamera(android.app.Fragment fragment, int type) {
        Intent intent = createCameraIntent(fragment.getActivity(), type);
        fragment.startActivityForResult(intent, REQ_TAKE_PICTURE);
    }


    private static File takenCameraPicture(Context context) throws IOException, URISyntaxException {
        @SuppressWarnings("ConstantConditions")
        URI imageUri = new URI(PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PHOTO_URI, null));
        notifyGallery(context, imageUri);
        return new File(imageUri);
    }


    private static void notifyGallery(Context context, URI pictureUri) throws URISyntaxException {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(pictureUri);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    public static void handleActivityResult(int requestCode, int resultCode, Intent data, Activity activity, Callbacks callbacks) {
        if (requestCode == ImagePickerConfig.REQ_PICK_PICTURE_FROM_GALLERY ||
                requestCode == ImagePickerConfig.REQ_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == ImagePickerConfig.REQ_PICK_PICTURE_FROM_GALLERY) {
                    onPictureReturnedFromGallery(data, activity, callbacks);
                } else if (requestCode == ImagePickerConfig.REQ_TAKE_PICTURE) {
                    onPictureReturnedFromCamera(activity, callbacks);
                } else if (data == null || data.getData() == null) {
                    onPictureReturnedFromCamera(activity, callbacks);
                }
            }
        }
    }

    public static boolean willHandleActivityResult(int requestCode, int resultCode, Intent data) {
        return  requestCode == ImagePickerConfig.REQ_PICK_PICTURE_FROM_GALLERY ||
                requestCode == ImagePickerConfig.REQ_TAKE_PICTURE;
    }

    /**
     * @param context context
     * @return File containing lastly taken (using camera) photo. Returns null if there was no photo taken or it doesn't exist anymore.
     */
    public static File lastlyTakenButCanceledPhoto(Context context) {
        String filePath = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LAST_CAMERA_PHOTO, null);
        if (filePath == null) return null;
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    private static void onPictureReturnedFromGallery(Intent data, Activity activity, Callbacks callbacks) {
        try {
            Uri photoPath = data.getData();
            File photoFile = new File(ImagePickerCompressor.with(activity).compress(ImagePickerFiles.pickedExistingPicture(activity, photoPath).getAbsolutePath()));
            callbacks.onImagePicked(photoFile, ImageSource.GALLERY, restoreType(activity));
        } catch (Exception e) {
            e.printStackTrace();
            callbacks.onImagePickerError(e, ImageSource.GALLERY, restoreType(activity));
        }
    }

    private static void onPictureReturnedFromCamera(Activity activity, Callbacks callbacks) {
        try {
            File photoFile = new File(ImagePickerCompressor.with(activity).compress(ImagePicker.takenCameraPicture(activity).getAbsolutePath()));
            callbacks.onImagePicked(photoFile, ImageSource.CAMERA, restoreType(activity));
            PreferenceManager.getDefaultSharedPreferences(activity).edit().remove(KEY_LAST_CAMERA_PHOTO).commit();
        } catch (Exception e) {
            e.printStackTrace();
            callbacks.onImagePickerError(e, ImageSource.CAMERA, restoreType(activity));
        }
    }

    public static void clearPublicTemp(Context context) {
        List<File> tempFiles = new ArrayList<>();
        File[] files = ImagePickerFiles.publicTempDir(context).listFiles();
        for (File file : files) {
            file.delete();
        }
    }


    /**
     * Method to clear configuration. Would likely be used in onDestroy(), onDestroyView()...
     *
     * @param context context
     */
    public static void clearConfiguration(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .remove(ImagePickerBundleKeys.FOLDER_NAME)
                .remove(ImagePickerBundleKeys.FOLDER_LOCATION)
                .remove(ImagePickerBundleKeys.PUBLIC_TEMP)
                .apply();
    }

    public static Configuration configuration(Context context) {
        return new Configuration(context);
    }

    public static class Configuration {
        private Context context;

        private Configuration(Context context) {
            this.context = context;
        }

        @SuppressLint("ApplySharedPref")
        public Configuration setImagesFolderName(String folderName) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putString(ImagePickerBundleKeys.FOLDER_NAME, folderName)
                    .commit();
            return this;
        }

        @SuppressLint("ApplySharedPref")
        public Configuration saveInRootPicturesDirectory() {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString(ImagePickerBundleKeys.FOLDER_LOCATION, ImagePickerFiles.publicRootDir(context).toString())
                    .commit();
            return this;
        }

        @SuppressLint("ApplySharedPref")
        public Configuration saveInAppExternalFilesDir() {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString(ImagePickerBundleKeys.FOLDER_LOCATION, ImagePickerFiles.publicAppExternalDir(context).toString())
                    .commit();
            return this;
        }


        /**
         * Use this method if you want your picked gallery or documents pictures to be duplicated into public, other apps accessible, directory.
         * You'll have to take care of removing that file on your own after you're done with it. Use ImagePicker.clearPublicTemp() method for that.
         * If you don't delete them they could show up in user galleries.
         *
         * @return modified Configuration object
         */
        @SuppressLint("ApplySharedPref")
        public Configuration setCopyExistingPicturesToPublicLocation(boolean copyToPublicLocation) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(ImagePickerBundleKeys.PUBLIC_TEMP, copyToPublicLocation)
                    .commit();
            return this;
        }
    }
}