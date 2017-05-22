package com.example.maxverhoeven.notificationsuspender;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Requires api 24 because of android bug with reconnection of NotificationListenerService
 *
 *
 *
 * Created by Max Verhoeven on 17-5-2017.
 */

/**
 * Usage Instructions -
 * Step 1. Check for permission - {@link NotificationSuspenderManager#hasPermission(Activity)} returns boolean.
 * Step 2. If previous returns false use - {@link NotificationSuspenderManager#askPermission(Activity)}
 *         The result will call {@link Activity#onActivityResult(int, int, Intent)}. It is successful when the following if statement passes:
 *         if(requestCode == NotificationSuspenderManager.NOTIFICATION_ACCESS_REQUESTCODE && NotificationSuspender.isServiceConnected())
 * Step 3. Now you can call the {@link NotificationSuspenderManager} instance with {@link NotificationSuspender#getNotificationSuspenderManager()}
 *         WARNING this will be null if {@link NotificationSuspender#isServiceConnected()} is false!
 * Step 4. The service will run by default after permission granted. (SEE mSuspendNotifications in this class)
 *         Call {@link NotificationSuspenderManager#suspendNotifications(boolean)} to turn it off / on.
 *         Only {@link NotificationSuspenderManager#reviveNotifications()} will first turn off the service and then re-initiate the suspended Notifications.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class NotificationSuspenderManager {
    public static final int NOTIFICATION_ACCESS_REQUESTCODE = 9001;

    private NotificationManager mNotificationManager;
    private NotificationSuspender mNotificationSuspender;
    private static NotificationSuspenderManager mNotificationSuspenderManager;

    private boolean mSuspendNotifications = true;

    private Set<StatusBarNotification> mStatusBarNotifications = new HashSet<>();
    private Set<String> mExceptions = new HashSet<>();


    /**
     * Private Constructor
     *
     * @param notificationSuspender
     */
    private NotificationSuspenderManager(NotificationSuspender notificationSuspender) {
        mExceptions.add(notificationSuspender.getPackageName());
        mNotificationManager = (NotificationManager) notificationSuspender.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationSuspender = notificationSuspender;
    }

    /**
     * Get singleton. Only NotificationSuspender can create an instance when the service is connected.
     *
     * @param notificationSuspender
     * @return
     */
    public static NotificationSuspenderManager getInstance(NotificationSuspender notificationSuspender) {
        if (mNotificationSuspenderManager == null) {
            mNotificationSuspenderManager = new NotificationSuspenderManager(notificationSuspender);
        }
        return mNotificationSuspenderManager;
    }

    /**
     * Called when a notification is posted from {@link NotificationSuspender#onNotificationRemoved(StatusBarNotification, NotificationListenerService.RankingMap)}
     * if mSuspendNotifications is set to true, suspend the notification
     *
     * @param sbn
     * @param rm
     */
    public void onPostNotification(StatusBarNotification sbn, NotificationListenerService.RankingMap rm) {
        if (mSuspendNotifications) suspendNotifications(sbn, rm);
    }

    /**
     * Suspends notification if the package of the incoming notification is not in the exceptions list
     *
     * @param sbn
     * @param rm
     */
    private void suspendNotifications(StatusBarNotification sbn, NotificationListenerService.RankingMap rm) {
        boolean isException = isException(sbn);

        if (!isException) {
            mStatusBarNotifications.add(sbn);
            mNotificationSuspender.cancelNotification(sbn.getKey());
        }
    }

    /**
     * checks if the incoming notification's package is in the exceptionList
     *
     * @param sbn
     * @return
     */
    private boolean isException(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        for (String notificationPackage : mExceptions) {
            if (notificationPackage.equals(packageName)) return true;
        }
        return false;
    }

    /**
     * turns of the NotificationSuspender ( the service always keeps running ) and returns all suspended notification to the statusbar
     */
    public void reviveNotifications() {
        this.mSuspendNotifications = false;
        Iterator<StatusBarNotification> iterator = mStatusBarNotifications.iterator();
        while (iterator.hasNext()) {
            StatusBarNotification sbn = iterator.next();
            reviveNotification(sbn);
            iterator.remove();
        }
    }

    /**
     * post the suspended notification to the statusbar
     *
     * @param sbn
     */
    private void reviveNotification(StatusBarNotification sbn) {
        int notificationId = sbn.getId();
        sbn.setOverrideGroupKey(sbn.getPackageName());
        mNotificationManager.notify(sbn.getPackageName(), notificationId, sbn.getNotification());
    }

    /**
     * Checks if the user has given permission for the NotificationSuspender.
     *
     * @param activity
     * @return true if the user has given permission. false otherwise.
     */
    public static boolean hasPermission(Activity activity) {
        String flat = Settings.Secure.getString(activity.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(".NotificationSuspender");
    }

    /**
     * Prompts the user with the NotificationAccess permission screen.
     * To catch the end of the action (user exits permission screen), use {@link Activity#onActivityResult(int, int, Intent)}
     * Then use {@link NotificationSuspenderManager#hasPermission(Activity)}  to check if the use has granted access.
     *
     * @param activity
     */
    public static void askPermission(Activity activity) {
        Intent notificationAcessIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        activity.startActivityForResult(notificationAcessIntent, NOTIFICATION_ACCESS_REQUESTCODE);
    }

    /**
     * Enable / Disable the Notification suspender
     *
     * @param suspend
     */
    public void suspendNotifications(boolean suspend) {
        this.mSuspendNotifications = suspend;
    }

    /**
     * returns if the notification suspender is active or not.
     *
     * @return
     */
    public boolean isSuspendingNotifications() {
        return this.mSuspendNotifications;
    }

}
