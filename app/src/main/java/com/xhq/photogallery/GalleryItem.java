package com.xhq.photogallery;

/**
 * Created by xhq on 2016/11/2.
 */

public class GalleryItem {
    private String id;
    private String title;
    private String url_s;

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
}
