# Android-Notification-Suspender
Cancels out all notifications. When stopped, re-initiates all suspended notifications. 

Only for on Android N+. 

## Step 1 : Manifest
To make the NotificationSuspender work, the following service needs to be added to the manifest:  

        <service android:name=".NotificationSuspender"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>
        
Also the following permissions are needed:

 <uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY" />
    <uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"/>
    <uses-permission android:name="android.permission.VIBRATE"/> <!-- headsup will not work without this permission -->
