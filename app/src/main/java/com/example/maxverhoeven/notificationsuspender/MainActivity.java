package com.example.maxverhoeven.notificationsuspender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    NotificationSuspenderManager notificationSuspenderManager;
    private NotificationReceiverMain nReceiver;
    public static final String BCNLSM_NAME = ".NOTIFICATION_LISTENER_MAIN";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setButtons();
        nReceiver = new NotificationReceiverMain();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName()+BCNLSM_NAME);
        registerReceiver(nReceiver,filter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
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
            case R.id.button3:  sendTest(1);
                break;
            case R.id.button4:  sendTest(2);
                break;
        }
    }

    private void hasPermission() {
        Toast.makeText(this,String.valueOf(NotificationSuspenderManager.hasPermission(this)),Toast.LENGTH_SHORT).show();
    }

    private void askPermission() {
        NotificationSuspenderManager.askPermission(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendTest(int action) {
        Intent i = new Intent(getPackageName()+NotificationSuspender.BCNLS_NAME);
        i.putExtra(NotificationSuspender.EXTRA_KEY,action);
        sendBroadcast(i);
    }

    private void stopTheService() {
    }

    class NotificationReceiverMain extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,"RECEIVED Broadcast in MAIN",Toast.LENGTH_SHORT).show();
        }
    }

}
