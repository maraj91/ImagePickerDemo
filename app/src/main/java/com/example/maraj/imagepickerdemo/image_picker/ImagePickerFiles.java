package com.example.maraj.imagepickerdemo.image_picker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Jacek KwiecieĹ on 14.12.15.
 */
class ImagePickerFiles {

    public static String DEFAULT_FOLDER_NAME = "ImagePickerDemo";
    public static String TEMP_FOLDER_NAME = "Temp";


    public static String getFolderName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(ImagePickerBundleKeys.FOLDER_NAME, DEFAULT_FOLDER_NAME);
    }

    public static File tempImageDirectory(Context context) {
        boolean publicTemp = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ImagePickerBundleKeys.PUBLIC_TEMP, false);
        File dir = publicTemp ? publicTempDir(context) : privateTempDir(context);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File publicRootDir(Context context) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }


    public static File publicAppExternalDir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public static File publicTempDir(Context context) {
        File cameraPicturesDir = new File(ImagePickerFiles.getFolderLocation(context), ImagePickerFiles.getFolderName(context));
        File publicTempDir = new File(cameraPicturesDir, TEMP_FOLDER_NAME);
        if (!publicTempDir.exists()) publicTempDir.mkdirs();
        return publicTempDir;
    }

    private static File privateTempDir(Context context) {
        File privateTempDir = new File(context.getApplicationContext().getCacheDir(), getFolderName(context));
        if (!privateTempDir.exists()) privateTempDir.mkdirs();
        return privateTempDir;
    }


    public static void writeToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFileName(Context mContext, Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        String fileName = new File(filePath).getName();
        cursor.close();
        return fileName;
    }

    public static File pickedExistingPicture(Context context, Uri photoUri) throws IOException {
        InputStream pictureInputStream = context.getContentResolver().openInputStream(photoUri);
        File directory = tempImageDirectory(context);
        File photoFile = new File(directory, getFileName(context, photoUri));
        photoFile.createNewFile();
        writeToFile(pictureInputStream, photoFile);
        return photoFile;
    }

    /**
     * Default folder location will be inside app public directory.
     * That way write permissions after SDK 18 aren't required and contents are deleted if app is uninstalled.
     *
     * @param context context
     */
    public static String getFolderLocation(Context context) {
        File publicAppExternalDir = publicAppExternalDir(context);
        String defaultFolderLocation = null;
        if (publicAppExternalDir != null) {
            defaultFolderLocation = publicAppExternalDir.getPath();
        }
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(ImagePickerBundleKeys.FOLDER_LOCATION, defaultFolderLocation);
    }

    public static File getCameraPicturesLocation(Context context) throws IOException {
        File dir = new File(ImagePickerFiles.getFolderLocation(context), ImagePickerFiles.getFolderName(context));
        if (!dir.exists()) dir.mkdirs();
        File imageFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg", dir);
        return imageFile;
    }
}