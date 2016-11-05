package com.xhq.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xhq on 2016/11/2.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private Boolean mHasQuit = false;
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;


    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler handler) {
        super(TAG);
        mResponseHandler = handler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    handleRequest(target);
                }
            }
        };

    }

    private void handleRequest(final T target) {
        final String url = mRequestMap.get(target);
        if (url == null)
            return;
        byte[] bitmapBytes = new byte[0];
        try {
            bitmapBytes = new FlickrFetchr().getUrlBytes(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mHasQuit || !(url.equals(mRequestMap.get(target))))
                    return;
                mRequestMap.remove(target);
                mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
            }
        });
//        mResponseHandler.obtainMessage(2).sendToTarget();

    }

    public void clearQuene() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queneThumbnail(T target, String url) {
        if (url != null) {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }


    }

}
