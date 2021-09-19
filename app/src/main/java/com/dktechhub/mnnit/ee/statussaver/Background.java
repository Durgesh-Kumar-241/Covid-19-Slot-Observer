package com.dktechhub.mnnit.ee.statussaver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class Background extends Service {
    private static final String CHANNEL_ID = "apache2";
    private static final String ACTION_STOP_SERVICE = "stop_my_service";
    // private final IBinder localBinder = new ServiceBinder();
    NotificationManager notificationManager;
    private ServiceInterface serviceInterface;

    private final int notificationId=12345;
    private Threader threader;

    public Background() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(this.serviceInterface!=null)
        {
            serviceInterface.onServerStarted();
        }
        //httpServer=new HTTPServer(getApplicationContext(), RunningService.this::log);
        //httpServer.startServing();
        if(threader==null)
        threader = new Threader(getApplicationContext(), this::playRingtone);
        threader.start();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {

            stopSelf();
        }
        createNotificationChannel();
        Intent intent2 = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, 0);

        Intent stopSelf = new Intent(this, Background.class);
        stopSelf.setAction(ACTION_STOP_SERVICE);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_slot_obserev)
                .setContentTitle("Covid 19 slot observer")
                .setContentText("Running in background")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_baseline_volume_mute_24, "Stop", pStopSelf)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent);



        Notification notification = builder.build();

        startForeground(1, notification);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.serviceInterface!=null)
        {
            serviceInterface.onServerStopped();
        }

        if(threader!=null)
            threader.isCancelled=true;


        stopRingtone();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    public interface ServiceInterface{
        void onServerStarted();
        void onServerStopped();

    }

    public void log(String s)
    {
        Intent local=new Intent();
        local.setAction("com.dktechhub.mnnit.ee.simplehttpserver.uiupdater");
        local.putExtra("log",s);
        this.sendBroadcast(local);
    }


    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    Ringtone r;
    public void playRingtone(int dose,int min_age,String center) {
        if(r==null)
        {
            try {

                Uri alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (alertSound == null) {
                    alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    if (alertSound == null) {
                        alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    }
                }

                r = RingtoneManager.getRingtone(getApplicationContext(), alertSound);
                r.play();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    r.setVolume(1);
                    //r.setLooping();
                }
                showRingToneNotification("Slot available dose "+dose+", age limit "+min_age+", center "+center);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(!r.isPlaying()){
            r.play();
        }

    }

    public void showRingToneNotification(String text)
    {

        Intent stopSelf = new Intent(this, Background.class);
        stopSelf.setAction(ACTION_STOP_SERVICE);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_slot_obserev)
                .setContentTitle("Slot availble...")
                .setContentText(text)

                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_baseline_volume_mute_24, "Mute", pStopSelf);

        Notification notification = builder.build();
        if(notificationManager==null)
       notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notification);

    }

    public void stopRingtone()
    {
        if(r!=null)
        {
            r.stop();

        }
        if(notificationManager!=null)
        {
            notificationManager.cancelAll();
        }
    }

}