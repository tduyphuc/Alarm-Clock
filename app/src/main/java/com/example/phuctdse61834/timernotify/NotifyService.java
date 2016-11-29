package com.example.phuctdse61834.timernotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by PhucTDSE61834 on 11/2/2016.
 */

public class NotifyService extends Service{
    private LocalBroadcastManager broadcast;
    private MediaPlayer mediaPlayer;
    private NotificationManager notificationManager;

    static final public String SERVICE_COMPLETE = "com.example.phuctdse61834.timernotify.SERVICE_COMPLETE";
    private static final String ACTION_SERVICE_STOP = "com.example.phuctdse61834.timernotify.ACTION_SERVICE_STOP";
    private static final int NOTIFICATION_ID = 100;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcast = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // check if user dismiss notification
        if(ACTION_SERVICE_STOP.equals((String)intent.getAction())){
            stopSelf();
            notificationManager.cancel(NOTIFICATION_ID);
            return START_NOT_STICKY;
        }

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // intent to activity
        Intent backIntent = new Intent(this.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, backIntent, 0);

        // intent to stop this service
        Intent stopIntent = new Intent(this, NotifyService.class);
        stopIntent.setAction(this.ACTION_SERVICE_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Message")
                .setContentText("Hello")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_alarm_white_48dp)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDeleteIntent(stopPending)
                .addAction(R.drawable.ic_alarm_black_18dp, "Dismiss", stopPending);

        Notification notification = builder.build();

        SharedPreferences mPref = getSharedPreferences("SAVE", MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean("IS_STARTING", false);
        editor.putLong("TIME", -1);
        editor.commit();

        // use starForeground to make sure that when app turn off, service still run, notifi won't crash
        startForeground(this.NOTIFICATION_ID, notification);
        Intent complete = new Intent(SERVICE_COMPLETE);

        int songId = mPref.getInt("SONG_ID", R.raw.kisstherain);
        if(songId >= 0){
            if(mediaPlayer != null){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.release();
                }
            }
            mediaPlayer = MediaPlayer.create(getApplicationContext(), songId);
            mediaPlayer.start();
            float volume = (float) mPref.getInt("VOLUME", 100) / 100;
            mediaPlayer.setVolume(volume, volume);
        }

        broadcast.sendBroadcast(complete);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
