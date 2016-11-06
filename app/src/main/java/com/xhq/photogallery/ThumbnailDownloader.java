package com.xhq.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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
                if (mHasQuit || !(mRequestMap.get(target).equals(url))) {
                    return;
                }

                //该句代码会导致mRequestMap.get(target)空指针异常。假设viewholder223在主线程在执行移除前。在迅速的快速滑动中，
                //子线程viewholder223被回收利用，存在于消息队列，而后面又没有被重新复用，存在这种时刻导致空指针异常
//                mRequestMap.remove(target);
                mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
            }
        });

    }

    public void queneThumbnail(T target, String url) {
        mRequestMap.put(target, url);
        mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();

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
