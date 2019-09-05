package com.tablet.smartedu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
    public static final String ACTION_CONTENT_PUSH = "com.lenovo.smartedu.action.CONTENT_PUSH";
    public static final String EDU_COURSE = "101";
    public static final String ORAL_TRAINING = "0";
    public static final String ACTION_CONTENT_FEEDBACK = "com.lenovo.smartedu.action.CONTENT_FEEDBACK";

    public static final String ACTION_OPEN_COURSE = "com.tablet.smartedu.action.OPEN_COURSE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), ACTION_CONTENT_PUSH)) {
            String type = intent.getStringExtra("type");
            if (Objects.equals(type, EDU_COURSE) || Objects.equals(type, ORAL_TRAINING)) {
                String content = intent.getStringExtra("content");
                sendNotification(context, type, content);
            }
        } else if (Objects.equals(intent.getAction(), ACTION_OPEN_COURSE)) {
            Log.d(TAG, "onReceive: " + ACTION_OPEN_COURSE
                    + "\n type: " + intent.getStringExtra("type")
                    + "\n content: " + intent.getStringExtra("content"));
            pushOutLeftScreen();
            intent.getStringExtra("type");
            intent.getStringExtra("content");
            sendFeedback(context, getCode(intent));
        }
    }

    private String getCode(Intent intent) {
        String type = intent.getStringExtra("type");

        String code = "";
        if (Objects.equals(type, ORAL_TRAINING)) {
            code = "english";
        } else if (Objects.equals(type, EDU_COURSE)) {
            String content = intent.getStringExtra("content");
            try {
                JSONObject jsonObject = null;
                if (content != null) {
                    jsonObject = new JSONObject(content);
                }
                if (jsonObject != null) {
                    code = jsonObject.getString("code");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return code;
    }

    private void sendFeedback(Context context, String code) {
        Log.d(TAG, "sendFeedback: " + ACTION_CONTENT_FEEDBACK + " code:" + code);
        Intent intent = new Intent(ACTION_CONTENT_FEEDBACK);
        intent.putExtra("code", code);
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
        Intent intent = new Intent(ACTION_OPEN_COURSE);
        intent.putExtra("type", type);
        intent.putExtra("content", content);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentTitle = "";
        String contentText = "";
        if (type.equals(EDU_COURSE)) {
            contentTitle = "学科视频";
            String lessonName = "";
            try {
                lessonName = new JSONObject(content).getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            contentText = "妈妈向你推送了“" + lessonName + "”的学科视频，点击马上开始观看。";
        } else if (type.equals(ORAL_TRAINING)) {
            contentTitle = "英语口训";
            contentText = "妈妈向你发起了英语口训，点击马上开始训练。";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Id", "Name", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Id");
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_snooze, context.getString(R.string.study), pendingIntent);
        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, notification);
    }

}
