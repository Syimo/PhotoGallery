package com.xhq.photogallery;


import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Environment;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xhq on 2016/11/15.
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
 /*       if (getResultCode() != Activity.RESULT_OK) {
            return;
        }*/
        int requestCode = intent.getIntExtra(PollJobService.REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getParcelableExtra(PollJobService.NOTIFICATION);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(requestCode, notification);
        String str = notification.toString();
//        Log.d(TAG,notification.toString());

        File file = new File(Environment.getExternalStorageDirectory(), "debug.txt");

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
            bufferedWriter.write(new Date().toString() + "\n" + "resultcode: " + getResultCode());
            bufferedWriter.newLine();
            bufferedWriter.write(str);
            bufferedWriter.newLine();
            bufferedWriter.write("-----------------------");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("sssssssssssssssss");
        }

    }
}
