# Android-Notification-Suspender
Cancels out all notifications. When stopped, re-initiates all suspended notifications. 

Only for on Android N+. 

## Step 1 : Manifest
To make the NotificationSuspender work, the following needs to be added to the manifest:  

        <service android:name=".NotificationSuspender"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>
