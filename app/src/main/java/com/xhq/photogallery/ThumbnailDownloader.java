package com.xhq.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
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
    private LruCache<String, Bitmap> mLruCache;
    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private final int cacheSize = maxMemory / 8;

    int i = 0;
    Object obj = new Object();


    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler handler) {
        super(TAG);

        mResponseHandler = handler;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                int temp = bitmap.getByteCount() / 1024;

                return temp;
            }
        };
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
        byte[] bitmapBytes = null;
        try {
            bitmapBytes = new FlickrFetchr().getUrlBytes(url);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        final Bitmap mBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        mLruCache.put(url, mBitmap);


        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!(mRequestMap.get(target).equals(url)) || mHasQuit) {
                    return;
                }

//                该句代码会导致mRequestMap.get(target)空指针异常。假设viewholder223在主线程在执行移除前。在迅速的快速滑动中，
//                子线程viewholder223被回收利用，存在于消息队列，而后面又没有被重新复用，存在这种时刻导致空指针异常
//                mRequestMap.remove(target);

                mThumbnailDownloadListener.onThumbnailDownloaded(target, mBitmap);
            }
        });

    }

    public void queneThumbnail(final T target, String url) {
        // Log.d(TAG, target + "");

        mRequestMap.put(target, url);
        final Bitmap mBitmap = mLruCache.get(url);
        Log.d(TAG, "sub thread : " + target.toString());
        if (mBitmap != null) {
            // Log.d(TAG, "req : " + (i++));

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "main thread : " + target.toString());
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, mBitmap);
                }
            });

        } else

        {
            //Log.d(TAG,"send !!");
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }


    }


    public void clearQuene() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

}
