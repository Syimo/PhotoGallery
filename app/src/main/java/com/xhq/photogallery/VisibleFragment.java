package com.xhq.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.widget.Toast;

/**
 * Created by xhq on 2016/11/13.
 */

public abstract class VisibleFragment extends Fragment {

    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(PollJobService.ACTION_SHOW_NOTIFICATION);
        myBroadcastReceiver = new MyBroadcastReceiver();
        getActivity().registerReceiver(myBroadcastReceiver, intentFilter, PollJobService.PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (myBroadcastReceiver != null)
            getActivity().unregisterReceiver(myBroadcastReceiver);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            setResultCode(Activity.RESULT_CANCELED);
        }
    }

}
