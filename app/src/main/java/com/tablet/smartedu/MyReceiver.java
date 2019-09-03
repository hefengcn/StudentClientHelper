package com.tablet.smartedu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.Objects;

public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = "MyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("com.lenovo.smartedu.action.CONTENT_PUSH")) {
            String type = intent.getStringExtra("type");
            if (Objects.equals(type, "101") | Objects.equals(type, "0")) {
                String content = intent.getStringExtra("content");
                sendNotification(context, type, content);
            }
        } else if (intent.getAction() != null && intent.getAction().equals("com.tablet.smartedu.action.CONTENT_PUSH")) {
            Log.d(TAG, "onReceive: Action =" + intent.getAction());
            pushOutLeftScreen();
            sendFeedback(context);
        }
    }

    private void sendFeedback(Context context) {
        Intent intent = new Intent("com.lenovo.smartedu.action.CONTENT_FEEDBACK");
        intent.putExtra("code", "english");
        context.sendBroadcast(intent);
    }

    private void pushOutLeftScreen() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    execShellCmd("input keyevent 3");
                    Thread.sleep(500);
                    execShellCmd("input swipe 200 250 500 250");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void execShellCmd(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec("sh");
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private void sendNotification(Context context, String type, String content) {
        Intent intent = new Intent("com.tablet.smartedu.action.CONTENT_PUSH");
        intent.putExtra("type", type);
        intent.putExtra("content", content);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Id", "Name", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        try {
            JSONObject json = new JSONObject(content);
            json.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Id");
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(type)
                .setContentText(content)
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, notification);
    }

}
