package com.xhq.photogallery;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xhq on 2016/11/1.
 */

public class PhotoGalleryFragment extends Fragment {
    private RecyclerView mPhotoRecyclerView;
    private static final String TAG = "PhotoGalleryFrgamen";
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private ProgressDialog mProgressDialog;
    private int currentPage = 1;
    private int currentPosition = 0;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute(currentPage++);
        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (currentPage > 10) {
                        Toast.makeText(getActivity(), "已经到最后了", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                    currentPosition = manager.findLastVisibleItemPosition();
                    new FetchItemsTask().execute(currentPage++);
                }
            }
        });
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                if (metrics.widthPixels > metrics.heightPixels) {
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) mPhotoRecyclerView.getLayoutManager();
                    gridLayoutManager.setSpanCount(4);
                }
            }
        });
        setupAdapter(currentPosition);
        return v;
    }

    private void setupAdapter(int currentPosition) {
        if (isAdded()) {
            if (mPhotoRecyclerView.getAdapter() == null) {
                mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));

            } else {
                mPhotoRecyclerView.scrollToPosition(currentPosition);
            }
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.fragment_gallery_item_iamge_view);
        }

        public void bindDrawable(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        //private List<GalleryItem> mItems;

        public PhotoAdapter(List<GalleryItem> list) {
            mItems = list;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item = mItems.get(position);
            Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
            holder.bindDrawable(drawable);
            mThumbnailDownloader.queneThumbnail(holder, item.getUrl_s());
            mThumbnailDownloader.preCacheDownload(mItems, position);


        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems.addAll(mItems.size(), galleryItems);
            setupAdapter(currentPosition);
            closeProgress();
        }
    }

    private void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("loading...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } else {
            mProgressDialog.show();
        }
    }

    private void closeProgress() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQuene();
        closeProgress();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
    }
}
