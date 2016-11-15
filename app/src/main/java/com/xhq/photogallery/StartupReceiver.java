package com.xhq.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Created by xhq on 2016/11/13.
 */

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast" + intent.getAction());
        boolean isOn = QueryPreferences.isAlarmOn(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PollJobService.setJobSchedule(context, isOn);
        } else {
            PollService.setServiceAlarm(context, isOn);
        }

    }
}
