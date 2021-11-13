package com.mindfulai.customclass;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindfulai.Activites.MainActivity;
import com.mindfulai.Models.NotificationModel;
import com.mindfulai.Utils.CommonUtils;
import com.mindfulai.Utils.SPData;
import com.mindfulai.ministore.R;
import org.jetbrains.annotations.NotNull;
import org.jsoup.select.Evaluator;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class FCMService extends FirebaseMessagingService {

    String id = "";
    Intent intent;
    private ArrayList<NotificationModel> notificationModelArrayList;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onMessageReceived(@NotNull RemoteMessage remoteMessage) {
        Log.e("TAG", "onMessageReceived: "+remoteMessage.getData());
        int count = SPData.getAppPreferences().getNotificationCount();
        count++;
        SPData.getAppPreferences().setNotificationCount(count);
        loadData(remoteMessage);
        id = getString(R.string.app_name) + "_channel";
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String imageUri = remoteMessage.getData().get("image");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);
        builder.setContentTitle(remoteMessage.getData().get("title"));
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        builder.setLargeIcon(icon);
        builder.setFullScreenIntent(pendingIntent, true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setAutoCancel(true);
        builder.setContentText(remoteMessage.getData().get("body"));

        if (CommonUtils.stringIsNotNullAndEmpty(imageUri))
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(CommonUtils.getBitmapfromUri(SPData.getBucketUrl() + imageUri)).setSummaryText(remoteMessage.getData().get("body")));
        else
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("body")));

        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setVibrate(new long[]{10000, 10000, 10000, 10000, 10000});
        builder.setLights(Color.RED, 10000, 10000);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.notification);
            AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            NotificationChannel mChannel = new NotificationChannel(id, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription("Notification");
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setLockscreenVisibility(1);
            mChannel.setSound(sound, attributes);
            manager.createNotificationChannel(mChannel);
        }
        manager.notify(0, builder.build());
    }

    private void saveData() {
        Gson gson = new Gson();
        String json = gson.toJson(notificationModelArrayList);
        SPData.getAppPreferences().setNotificationList(json);
    }

    private void loadData(RemoteMessage remoteMessage) {
        Gson gson = new Gson();
        String json = SPData.getAppPreferences().getNotificationList();
        Type type = new TypeToken<ArrayList<NotificationModel>>() {}.getType();
        notificationModelArrayList = gson.fromJson(json, type);
        if (notificationModelArrayList == null) {
            notificationModelArrayList = new ArrayList<>();
        }
        Map<String, String> hashMap = remoteMessage.getData();
        String date = new Date().toString();
        if (hashMap.get("image") != null)
            notificationModelArrayList.add(0, new NotificationModel(hashMap.get("tag"), hashMap.get("type"), hashMap.get("image"), hashMap.get("title"), hashMap.get("body"), date));
        else
            notificationModelArrayList.add(0, new NotificationModel(hashMap.get("tag"), hashMap.get("type"), "", hashMap.get("title"), hashMap.get("body"), date));
        saveData();
    }
}