package com.techtown.bbfindingfriends;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";

    private static final int Notification_ID = 100;
    private static final String Channel_ID = "ForegroundService ID";
    private static final CharSequence Channel_NAME = "BBForegroundService Channel";
    private final IBinder iBinder = new myBinder();

    private LocationManager locManager = null;

    private Thread gpsThread;
    private boolean gpsThread_Status;

    private static Context context;

    private FusedLocationProviderClient locClient;

    public class myBinder extends Binder {
        public ForegroundService getService() {
            return ForegroundService.this;
        }
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();

        super.onCreate();
        Log.d(TAG, "onCreate()");

        locClient = LocationServices.getFusedLocationProviderClient(context);

        startForeground(Notification_ID, getNotification(null));

        gpsThread_Status = true;

        gpsThread = new Thread("Gps Thread") {
            int stack = 0;

            @Override
            public void run() {
                while (gpsThread_Status) {
                    try {
                        Log.d(TAG, "gpsThread() - Count: " + ++stack);
                        //sendGPS();
                        getCurrentLoc();
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        gpsThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        if (intent == null) {
            return START_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendGPS(Location location) {
        Log.d(TAG, "sendGPS()");

        double latitude = location.getLatitude();
        double longtitude = location.getLongitude();
        double altitude = location.getAltitude();

        Intent intent_main = new Intent(getApplicationContext(), MainActivity.class);
        intent_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent_main.putExtra("Latitude", latitude);
        intent_main.putExtra("Longtitude", longtitude);
        intent_main.putExtra("Altitude", altitude);

        startActivity(intent_main);
    }

    @SuppressWarnings("MissingPermission")
    private void getCurrentLoc() {
        Log.d(TAG, "getCurrentLoc()");

        OnCompleteListener<Location> completeListener = new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();

                    String provider = location.getProvider();

                    double latitude = location.getLatitude();
                    double longtitude = location.getLongitude();
                    double altitude = location.getAltitude();

                    Log.d(TAG, "gpsListener() - \n"
                            + "Privider: " + provider + "\n"
                            + "Latitude: " + latitude + "\n"
                            + "Longtitude: " + longtitude + "\n"
                            + "Altitude: " + altitude);

                    sendGPS(location);
                } else {
                    Log.d(TAG, "getCurrentLoc - Failed: " + task.getException());
                }
            }
        };

        locClient.getLastLocation().addOnCompleteListener(completeListener);
    }

    private Notification getNotification(String message) {
        if (message == null) {
            message = "앱이 포그라운드에서 실행중";
        }

        // Custom Notification
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_main);
        notificationLayout.setTextViewText(R.id.tv_message, message);

        NotificationManager manager;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_ID);

        Intent intent_noti = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, R.integer.code_foregroud, intent_noti, 0);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);

        builder.setContentTitle("BB Service")
                .setContentText(message)
                .setPriority(Notification.PRIORITY_HIGH)
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setCustomBigContentView(notificationLayout);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 오레오 이상 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Channel_ID, Channel_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
            builder.setChannelId(Channel_ID);
        }

        return builder.build();
    }

    private void showNotiWithMessage(String message) {
        NotificationManager notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notiManager.notify(Notification_ID, getNotification(message));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }
}