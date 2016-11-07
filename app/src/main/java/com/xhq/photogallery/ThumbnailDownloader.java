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
import java.util.List;
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
    private static final int MESSAGE_PRE_DOWNLOAD = 1;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private LruCache<String, Bitmap> mLruCache;
    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private final int cacheSize = maxMemory / 8;
    private Handler mPreDownloadHandler;

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
                return bitmap.getByteCount() / 1024;
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

        mPreDownloadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_PRE_DOWNLOAD) {
                    String url = (String) msg.obj;
                    try {
                        if (mLruCache.get(url) != null)
                            return;
                        byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                        Bitmap mBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                        mLruCache.put(url, mBitmap);
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
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

    public void queneThumbnail(final T target, final String url) {
        mRequestMap.put(target, url);
        final Bitmap mBitmap = mLruCache.get(url);
        if (mBitmap != null) {
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, mBitmap);
                }
            });

        } else {
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void preCacheDownload(List<GalleryItem> list, int currentPosition) {
        int start = Math.max(0, currentPosition - 10);
        int end = Math.min(list.size() - 1, currentPosition + 10);
        for (int i = start; i <= end; i++) {
            mPreDownloadHandler.obtainMessage(MESSAGE_PRE_DOWNLOAD, list.get(i).getUrl_s());
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
