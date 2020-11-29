package com.mosin.weatheronboard;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseServiceMsg extends FirebaseMessagingService {
    private int messageId = 10000;
    public MyFirebaseServiceMsg() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("PushMessage", remoteMessage.getNotification().getBody());
        String title = remoteMessage.getNotification().getTitle();
        if (title == null){
            title = "Push Message";
        }
        String text = remoteMessage.getNotification().getBody();
        // создать нотификацию
        //создаем интент для открытия из пуша
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "2")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                // переход на интент
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(messageId++, builder.build());
    }

    @Override
    public void onNewToken(String token) {
        // Если надо посылать сообщения этому экземпляру приложения
        // или управлять подписками приложения на стороне сервера,
        // сохраните этот токен в базе данных. отправьте этот токен вашему
        Log.d("PushMessage", "Token " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
    }
}
