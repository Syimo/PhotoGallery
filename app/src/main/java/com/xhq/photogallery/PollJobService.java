package com.xhq.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by xhq on 2016/11/13.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PollJobService extends JobService {
    private static final String TAG = "PollJobService";
    private PollTask mPollTask;
    private static final int JOB_ID = 1;
    public static final String ACTION_SHOW_NOTIFICATION = "com.xhq.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.xhq.photogallery.PRIVATE";

    public static String REQUEST_CODE = "REQUEST_CODE";
    public static String NOTIFICATION = "NOTIFICATION";

    @Override
    public boolean onStartJob(JobParameters params) {
        mPollTask = new PollTask();
        mPollTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mPollTask != null)
            mPollTask.cancel(true);

        return true;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(JobParameters... params) {
            JobParameters jobParameters = params[0];

            String query = QueryPreferences.getStoredQuery(getApplicationContext());

            List<GalleryItem> list;
            if (query == null) {
                list = new FlickrFetchr().fetchRecentPhotos(1);
            } else {
                list = new FlickrFetchr().searchPhotos(query, 1);
            }


            jobFinished(jobParameters, false);
            return list;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> list) {
            if (list.size() == 0)
                return;
            String lastId = QueryPreferences.getPrefLastId(getApplicationContext());
            String newId = list.get(0).getId();


            File file = new File(Environment.getExternalStorageDirectory(), "polljobservice.txt");

            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
                bufferedWriter.write(new Date().toString() + "\n" + " resultcode: " + lastId + "::::" + newId);
                bufferedWriter.newLine();

                bufferedWriter.write("-----------------------");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                throw new RuntimeException("sssssssssssssssss");
            }
            if (newId.equals(lastId)) {
                Log.d(TAG, "不需要更新");
            } else {
                Resources resources = getResources();
                Intent i = PhotoGalleryActivity.newIntent(getApplicationContext());
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
                Notification notification = new Notification.Builder(getApplicationContext())
                        .setTicker(resources.getString(R.string.new_pictures_title_job))
                        .setContentTitle(resources.getString(R.string.new_pictures_title_job))
                        .setContentText(resources.getString(R.string.new_pictures_text))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();
                showBackgroundNotification(0, notification);

            }
            QueryPreferences.setPrefLastId(getApplicationContext(), newId);
        }

        private void showBackgroundNotification(int requestCode, Notification notification) {

            Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
            intent.putExtra(REQUEST_CODE, requestCode);
            intent.putExtra(NOTIFICATION, notification);
            sendOrderedBroadcast(intent, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
        }
    }

    public static boolean hasScheduled(Context context) {
        boolean hasScheduled = false;
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID)
                hasScheduled = true;
        }

        return hasScheduled;
    }

    public static void setJobSchedule(Context context, boolean isSet) {
        if (isSet) {
            JobScheduler js = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollJobService.class))
                    .setPeriodic(1000 * 60 * 15)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)
                    .build();
            js.schedule(jobInfo);
        } else {
            cancelJob(context);
        }
        QueryPreferences.setAlarmOn(context, isSet);


    }

    public static void cancelJob(Context context) {
        JobScheduler js = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        js.cancel(JOB_ID);
    }
}
