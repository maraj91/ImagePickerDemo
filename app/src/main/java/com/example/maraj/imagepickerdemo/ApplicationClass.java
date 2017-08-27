package com.example.maraj.imagepickerdemo;

import android.app.Application;

import com.example.maraj.imagepickerdemo.image_picker.ImagePicker;

/**
 * Created by maraj on 27-08-2017.
 */

public class ApplicationClass extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        ImagePicker.configuration(this)
                .setImagesFolderName(getString(R.string.app_name))
                .saveInAppExternalFilesDir();
    }
}
