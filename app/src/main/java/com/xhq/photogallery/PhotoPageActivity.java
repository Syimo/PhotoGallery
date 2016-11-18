package com.xhq.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.webkit.WebView;

/**
 * Created by xhq on 2016/11/18.
 */

public class PhotoPageActivity extends SingleFragmetActivity {
    PhotoPageFragment mPhotoPageFragment;

    public static Intent newIntent(Context context, Uri pageUri) {
        Intent intent = new Intent(context, PhotoPageActivity.class);
        intent.setData(pageUri);
        return intent;
    }

    @Override
    public Fragment createFragment() {
        mPhotoPageFragment = PhotoPageFragment.newInstance(getIntent().getData());
        return mPhotoPageFragment;
    }

    @Override
    public void onBackPressed() {
        if (!mPhotoPageFragment.canBack())
            super.onBackPressed();
    }
}
