package com.example.maxverhoeven.notificationsuspender;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    NotificationSuspenderManager notificationSuspenderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setButtons();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        if (notificationSuspenderManager == null) {
            notificationSuspenderManager = NotificationSuspender.getNotificationSuspenderManager();
        }
        super.onPostCreate(savedInstanceState);
    }

    private void setButtons() {

        Button b1 = (Button) findViewById(R.id.button);
        Button b2 = (Button) findViewById(R.id.button2);
        Button b3 = (Button) findViewById(R.id.button3);
        Button b4 = (Button) findViewById(R.id.button4);

        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
        b4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:   hasPermission();
                break;
            case R.id.button2:  askPermission();
                break;
            case R.id.button3:  turnOnOff();
                break;
            case R.id.button4:  stopTheService();
                break;
        }
    }

    private void hasPermission() {
        Toast.makeText(this,NotificationSuspenderManager.hasPermission(this)+"",Toast.LENGTH_SHORT);
    }

    private void askPermission() {
        NotificationSuspenderManager.askPermission(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NotificationSuspenderManager.NOTIFICATION_ACCESS_REQUESTCODE &&
                NotificationSuspender.isServiceConnected()) {
            notificationSuspenderManager = NotificationSuspender.getNotificationSuspenderManager();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void turnOnOff() {
        if (notificationSuspenderManager == null) return;
        boolean suspendingNotifications = notificationSuspenderManager.isSuspendingNotifications();
        notificationSuspenderManager.suspendNotifications(!suspendingNotifications);
    }

    private void stopTheService() {
        if (notificationSuspenderManager == null) return;
        notificationSuspenderManager.reviveNotifications();
    }


}
