package com.example.maxverhoeven.notificationsuspender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import static com.example.maxverhoeven.notificationsuspender.NotificationSuspender.BCNLS_NAME;
import static com.example.maxverhoeven.notificationsuspender.NotificationSuspender.COMMAND_DISABLE;
import static com.example.maxverhoeven.notificationsuspender.NotificationSuspender.COMMAND_ENABLE;
import static com.example.maxverhoeven.notificationsuspender.NotificationSuspender.COMMAND_REVIVE;
import static com.example.maxverhoeven.notificationsuspender.NotificationSuspender.EXTRA_KEY;

/**
 * Requires api 24 because of android bug with reconnection of NotificationListenerService?
 * Created by Max Verhoeven on 17-5-2017.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class NotificationSuspenderManager {

    public static final int NOTIFICATION_ACCESS_REQUESTCODE = 9001;
    public static final String NOTIF_LISTENERS_ENABLED_KEY = "enabled_notification_listeners";
    public static final String NOTIF_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";


    /**
     * checks if the user has granted permission for this app to listen for notifications.
     *
     * @param activity
     * @return
     */
    public static boolean hasPermission(Activity activity) {
        String flat = Settings.Secure.getString(activity.getContentResolver(), NOTIF_LISTENERS_ENABLED_KEY);
        return flat != null && flat.contains(".NotificationSuspender");
    }

    /**
     * Sends the user to the Notification Access Permission screen.
     * Use an {@link Activity#onActivityResult(int, int, Intent)} for the callback.
     * Use {@link NotificationSuspenderManager#hasPermission(Activity)}
     * in combination with {@link NotificationSuspender#isServiceRunning()}
     * To check if the user has granted permission and if the service is connected.
     *
     * @param activity
     */
    public static void askPermission(Activity activity) {
        Intent notificationAcessIntent = new Intent(NOTIF_LISTENER_SETTINGS);
        activity.startActivityForResult(notificationAcessIntent, NOTIFICATION_ACCESS_REQUESTCODE);
    }


    /**
     * Enables the notification suspension and saves all incoming notifications.
     * ( DOES NOT MEAN THE SERVICE IS CONNECTED )
     * Use {@link NotificationSuspender#isServiceRunning()} to check if the service is connected.
     *
     * @param c
     */
    public static void enable(Context c) {
        execute(c, COMMAND_ENABLE);
    }

    /**
     * Disables the notification suspension but does not revive the saved notifications yet.
     *
     * @param c
     */
    public static void disable(Context c) {
        execute(c, COMMAND_DISABLE);
    }

    /**
     * Disables the notification suspender just like {@link NotificationSuspenderManager#disable(Context)}
     * And also revives all saved notifications.
     *
     * @param c
     */
    public static void revive(Context c) {
        execute(c, COMMAND_REVIVE);
    }

    private static void execute(Context c, int command) {
        Intent i = new Intent(c.getPackageName() + BCNLS_NAME);
        i.putExtra(EXTRA_KEY, command);
        c.sendBroadcast(i);
    }
}
