package com.xhq.photogallery;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xhq on 2016/11/2.
 */

public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "47d7327a15ddcbb5f85a040740d411c7";
    private static final String FETCH_RECENT_PHOTOS = "flickr.photos.getRecent";
    private static final String SEARCH_PHOTOS = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("extras", "url_s")
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .build();


    public byte[] getUrlBytes(String urlSpec) throws IOException {

        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ":with " + urlSpec);
            }
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(Integer page) {
        String url = buildUrl(FETCH_RECENT_PHOTOS, null, page);
        return downloadGalleryItems(url, page);
    }

    public List<GalleryItem> searchPhotos(String query, @Nullable Integer page) {
        String url = buildUrl(SEARCH_PHOTOS, query, page);
        return downloadGalleryItems(url, page);
    }

    private List<GalleryItem> downloadGalleryItems(String url, @Nullable Integer page) {
        List<GalleryItem> listItems = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            listItems = parseItems(jsonString);
        } catch (IOException | JSONException e) {
            Log.e(TAG, e.getMessage() + "获取json失败");
        }
        return listItems;
    }

    private String buildUrl(String method, @Nullable String query, @Nullable Integer page) {
        Uri.Builder builder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);
        if (page != null) {
            builder.appendQueryParameter("page", page.toString());
        }
        if (method.equals(SEARCH_PHOTOS) && query != null) {
            builder.appendQueryParameter("text", query);
        }
        return builder.build().toString();
    }

    private List<GalleryItem> parseItems(String jsonBody) throws JSONException {
        List<GalleryItem> list = new ArrayList<>();
        JSONObject object = new JSONObject(jsonBody);
        JSONObject photo = object.getJSONObject("photos");
        JSONArray items = photo.getJSONArray("photo");
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (!item.has("url_s"))
                continue;
            GalleryItem galleryItem = new GalleryItem();
            galleryItem.setId(item.getString("id"));
            galleryItem.setTitle(item.getString("title"));
            galleryItem.setUrl_s(item.getString("url_s"));
            list.add(galleryItem);
        }
        return list;
    }

    /***
     * 还存在问题。当url_s字段不存在时，会给item对象存入null，应该在此作判断
     *
     * @param jsonBody
     * @return
     * @throws JSONException
     */
    private List<GalleryItem> parseItemsWithGson(String jsonBody) throws JSONException {
        JSONObject object = new JSONObject(jsonBody);
        JSONObject photo = object.getJSONObject("photos");
        JSONArray items = photo.getJSONArray("photo");
        Gson gson = new Gson();
        return gson.fromJson(items.toString(), new TypeToken<List<GalleryItem>>() {
        }.getType());
    }
}
