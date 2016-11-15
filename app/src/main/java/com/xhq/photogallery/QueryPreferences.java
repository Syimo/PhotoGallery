package com.xhq.photogallery;

import android.animation.FloatArrayEvaluator;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceManager;

/**
 * Created by xhq on 2016/11/7.
 */

public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_LAST_ID = "lastId";
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public static String getPrefLastId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LAST_ID, null);
    }

    public static void setPrefLastId(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_ID, value)
                .apply();
    }

    public static boolean isAlarmOn(Context context) {

        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_IS_ALARM_ON, false);
    }

    public static void setAlarmOn(Context context, boolean isAlarmOn) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isAlarmOn)
                .apply();
    }
}
