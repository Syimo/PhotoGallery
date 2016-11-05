package com.xhq.photogallery;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmetActivity {


    @Override
    public Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
