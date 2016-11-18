package com.xhq.photogallery;

import android.net.Uri;

/**
 * Created by xhq on 2016/11/2.
 */

public class GalleryItem {
    private String id;
    private String title;
    private String url_s;
    private String OwnerId;

    public String getId() {
        return id;
    }

    public String getUrl_s() {
        return url_s;
    }

    public String getTitle() {
        return title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl_s(String url_s) {
        this.url_s = url_s;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwnerId() {
        return OwnerId;
    }

    public void setOwnerId(String ownerId) {
        OwnerId = ownerId;
    }

    public Uri getPhotoPageUrl() {
        return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(OwnerId)
                .appendPath(id)
                .build();
    }

    @Override
    public String toString() {
        return title;
    }
}
