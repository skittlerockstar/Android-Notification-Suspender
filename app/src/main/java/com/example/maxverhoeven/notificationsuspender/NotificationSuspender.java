package com.example.maxverhoeven.notificationsuspender;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Requires api 24 because of android bug with reconnection of NotificationListenerService
 * Created by Max Verhoeven on 17-5-2017.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class NotificationSuspender extends NotificationListenerService {

    public static final String BCNLS_NAME = ".NOTIFICATION_LISTENER";
    public static final String EXTRA_KEY = "COMMAND";
    private static final int STICKY_ID = 130124;
    public static final int COMMAND_DISABLE = 0,
            COMMAND_ENABLE = 1,
            COMMAND_REVIVE = 2;

    private NotificationManager mNotificationManager;
    private Notification mHeadsUpPreventionNotification;
    private Notification mStickyNSIndicator;
    private RemoteViews mEmptyHeadsUpView;

    private Set<StatusBarNotification> mSuspendedNotifications = new HashSet<>();
    private Set<String> mPackageExceptions = new HashSet<>();

    private boolean mSuspendNotifications = false;

    private static boolean mIsServiceRunning = false;
    private NotificationReceiver mNSReceiver;

    public static boolean isServiceRunning() {
        return mIsServiceRunning;
    }


    /**
     * if the service is connected and receives a notification,
     * that notification is saved and cancelled if the package of that notification is not in the exceptionlist
     * and if the notificationsuspender is turned on.
     *
     * @param sbn
     * @param rankingMap
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        if (mSuspendNotifications && !isException(sbn)) {
            saveNotification(sbn);
            autoCancelHeadsUpNotification(sbn);
            super.cancelNotification(sbn.getKey());
        }
    }

    @Override
    public void onListenerConnected() {
        setFields();
        setExceptions();
        setPolicy();
        registerNSReceiver();
        NSLogger("Service Connected");
        super.onListenerConnected();
    }

    @Override
    public void onListenerDisconnected() {
        NotificationSuspender.mIsServiceRunning = false;
        NSLogger("Service Disconnected");
        super.onListenerDisconnected();
    }

    /**
     * returns true if the package of the StatusBarNotification is in the list of exceptions.
     *
     * @param sbn
     * @return
     */
    private boolean isException(StatusBarNotification sbn) {
        String pName = sbn.getPackageName();
        return mPackageExceptions.contains(pName);
    }

    /**
     * saves the incoming notification if it is not a sticky.
     *
     * @param sbn
     */
    private void saveNotification(StatusBarNotification sbn) {
        if (!sbn.isOngoing() && sbn.isClearable()) {
            mSuspendedNotifications.add(sbn);
        }
    }

    /**
     * retrieves the exceptions from the notification_suspender_values.xml and adds them to the exception list
     */
    private void setExceptions() {
        String[] stringArray = getResources().getStringArray(R.array.exceptions);
        for (String s : stringArray) {
            mPackageExceptions.add(s);
        }
        mPackageExceptions.add(getPackageName());
    }

    /**
     * sets the :
     * broadcastreceiver ,
     * the notificationmanager,
     * the custom heads-up remoteview,
     * the heads-up-notification-overrerider
     * the sticky
     * and the isRunning boolean.
     */
    private void setFields() {
        mNSReceiver = new NotificationReceiver();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mEmptyHeadsUpView = new RemoteViews(getPackageName(), R.layout.empty_headsup_custom_view); // Empty Headsup Notification
        mHeadsUpPreventionNotification = new Notification.Builder(this)
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setVibrate(new long[0])
                .setCustomHeadsUpContentView(mEmptyHeadsUpView)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX).build();
        mStickyNSIndicator = new Notification.Builder(this)
                .setContentTitle("Testing if is running")
                .setContentText("Testing if is running")
                .setOngoing(true)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_DEFAULT).build();
        NotificationSuspender.mIsServiceRunning = true;
    }

    /**
     * sets the policy for notifications so that they can be cancelled correctly
     */
    private void setPolicy() {
        NotificationManager.Policy policy = null;
        policy = new NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_MESSAGES,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.SUPPRESSED_EFFECT_SCREEN_OFF);
        mNotificationManager.setNotificationPolicy(policy);
        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
    }

    /**
     * registers the broadcastReceiver.
     */
    private void registerNSReceiver() {
        IntentFilter i = new IntentFilter();
        i.addAction(getPackageName() + BCNLS_NAME);
        registerReceiver(mNSReceiver, i);
    }


    /**
     * Cancels the headsup generated by notifications with PRIORITY_HIGH / PRIORITY_MAX because this isn't cancelled by default
     * this is done by generating a custom empty heads up notification that hides the first one.
     *
     * @param sbn
     */
    private void autoCancelHeadsUpNotification(StatusBarNotification sbn) {
        boolean isThisApp = sbn.getPackageName().equals(getPackageName());
        boolean isHighPriority = sbn.getNotification().priority == Notification.PRIORITY_HIGH;
        boolean isMaxPriority = sbn.getNotification().priority == Notification.PRIORITY_MAX;
        NSLogger(sbn.getPackageName());
        if (!isThisApp && (isHighPriority || isMaxPriority)) {
            int rID = (int) (Math.random() * 10000);
            mNotificationManager.notify(rID, mHeadsUpPreventionNotification);
            mNotificationManager.cancel(rID);
        }
    }


    /**
     * sets a sticky notification for the user to see if the app is suspending notifications.
     */
    private void placeSticky() {
        mNotificationManager.notify(STICKY_ID, mStickyNSIndicator);
    }

    /**
     * removes the sticky notification.
     */
    private void removeSticky() {
        mNotificationManager.cancel(STICKY_ID);
    }

    /**
     * turns on / off the service without reviving the notifications which stay saved until they are revived.
     *
     * @param b
     */
    private void suspend(boolean b) {
        if (b) {
            placeSticky();
        } else {
            removeSticky();
        }
        NSLogger("Setting Suspender to:" + String.valueOf(b));
        mSuspendNotifications = b;
    }

    /**
     * turns off the service and revives all saved notifications
     */
    private void revive() {
        mSuspendNotifications = false;
        Iterator<StatusBarNotification> iterator = mSuspendedNotifications.iterator();
        while (iterator.hasNext()) {
            reviveNotification(iterator.next());
            iterator.remove();
        }
    }

    /**
     * revives a notification.
     *
     * @param sbn
     */
    private void reviveNotification(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        int id = sbn.getId();
        Notification notification = sbn.getNotification();

        mNotificationManager.notify(packageName, id, notification);
        NSLogger("Reviving Notification for: " + packageName);
    }

    private void NSLogger(String s) {
        Log.d("NotificationSuspender: ", s);
    }


    /**
     * Entrypoint for {@link NotificationSuspender}.
     * Commands are received and parsed to actions.
     */
    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationSuspender ns = NotificationSuspender.this;
            NSLogger("Received BC in NS:");
            int intExtra = intent.getIntExtra(EXTRA_KEY, -1);
            switch (intExtra) {
                case COMMAND_ENABLE:
                    ns.suspend(true);
                    break;
                case COMMAND_DISABLE:
                    ns.suspend(false);
                    break;
                case COMMAND_REVIVE:
                    ns.revive();
                    break;
            }
        }
    }

}
