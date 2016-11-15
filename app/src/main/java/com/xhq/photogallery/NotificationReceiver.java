package com.xhq.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.sax.RootElement;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Created by xhq on 2016/11/15.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }
        int requestCode = intent.getIntExtra(PollJobService.REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getSerializableExtra(PollJobService.NOTIFICATION);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(requestCode, notification);


    }
}
