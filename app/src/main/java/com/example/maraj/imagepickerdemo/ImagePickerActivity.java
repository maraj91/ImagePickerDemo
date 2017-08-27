package com.example.maraj.imagepickerdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.maraj.imagepickerdemo.image_picker.ImagePickerDefaultCallback;
import com.example.maraj.imagepickerdemo.image_picker.ImagePicker;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ImagePickerActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int GALLERY_PICTURE = 101;
    private static final int CAMERA_REQUEST_PERMISSION_RESULT = 102;
    private static final int CAMERA_REQUEST = 100;
    private Button btnCamera,btnGallery;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        btnCamera = (Button)findViewById(R.id.btn_camera);
        btnGallery = (Button)findViewById(R.id.btn_gallery);
        mImageView = (ImageView)findViewById(R.id.imageView);

        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);

    }
    void checkWritePermission() {
        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED){
                    ImagePicker.openCamera(this, CAMERA_REQUEST);
                }else {
                    if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        showToast("This App Requred Camera Permission");
                    }
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},CAMERA_REQUEST_PERMISSION_RESULT);
                }
            }else {
                ImagePicker.openCamera(this, CAMERA_REQUEST);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_REQUEST_PERMISSION_RESULT){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                showToast("This App can't Run Without Camera Permission");
            }
        }
    }
     @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImagePicker.handleActivityResult(requestCode, resultCode, data, this, new ImagePickerDefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, ImagePicker.ImageSource source, int type) {
                //Some error handling
                showToast(e.getMessage());
            }

            @Override
            public void onImagePicked(File imageFile, ImagePicker.ImageSource source, int type) {
                updateImageList(imageFile);
            }

            @Override
            public void onCanceled(ImagePicker.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == ImagePicker.ImageSource.CAMERA) {
                    File photoFile = ImagePicker.lastlyTakenButCanceledPhoto(getApplicationContext());
                    if (photoFile != null) photoFile.delete();
                }
            }
        });

    }

    private void updateImageList(File imageFile) {
        showToast(imageFile.getAbsolutePath());
        Picasso.with(this)
                .load(imageFile)
                .error(R.drawable.ic_no_image)
                .into(mImageView);
    }

    @Override
    public void onClick(View view) {
        if(view == btnGallery){
            ImagePicker.openGallery(ImagePickerActivity.this, GALLERY_PICTURE);
        }else if(view == btnCamera){
            checkWritePermission();
        }
    }
}
