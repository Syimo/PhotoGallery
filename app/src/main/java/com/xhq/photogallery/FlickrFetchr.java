package com.xhq.photogallery;

import android.net.Uri;
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

    public List<GalleryItem> fetchItems(int page) {
        List<GalleryItem> listItems = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("page", page + "")
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .build()
                    .toString();
            String jsonString = getUrlString(url);
            listItems = parseItemsWithGson(jsonString);
        } catch (IOException | JSONException e) {
            Log.e(TAG, e.getMessage() + "获取json失败");
        }
        return listItems;
    }

    private List<GalleryItem> parseItemsWithGson(String jsonBody) throws JSONException {
        JSONObject object = new JSONObject(jsonBody);
        JSONObject photo = object.getJSONObject("photos");
        JSONArray items = photo.getJSONArray("photo");
        Gson gson = new Gson();
        return gson.fromJson(items.toString(), new TypeToken<List<GalleryItem>>() {
        }.getType());
    }
}