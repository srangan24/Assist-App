package org.assist.purplemetro.assist.Services;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import org.assist.purplemetro.assist.Alert;
import org.assist.purplemetro.assist.CancelActivity;
import org.assist.purplemetro.assist.R;
import org.assist.purplemetro.assist.ScheduledFutureHolder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;
import static java.util.concurrent.TimeUnit.SECONDS;

public class AccelerometerService extends Service implements SensorEventListener {
    private static final String DEBUG_TAG = "AccelerometerService";

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private long lastUpdate = 0;
    private long fallTime = 0;

    private static final double FALL_THRESHOLD = 12;
    private static final double SMALL_MOVEMENT_THRESHOLD = 3;

    private final int vibrate_delay = 10;
    private final int alert_delay = 20;

    private String contact1Number;
    private String contact2Number;

    //private ScheduledFuture<?> scheduledFuture;

    //private boolean fallMode = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        ScheduledFutureHolder.FALL_MODE = false;

        setupContacts();

        return START_STICKY;
    }

    private void setupContacts(){
        SharedPreferences sharedPref = getSharedPreferences("contactInfo", Context.MODE_PRIVATE);
        if(!sharedPref.getString("contact1_num", "").equals("")){
            contact1Number = sharedPref.getString("contact1_num", "");
        }
        if(!sharedPref.getString("contact2_num", "").equals("")){
            contact2Number = sharedPref.getString("contact2_num", "");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
                Log.d("accel", "" + acceleration);
                if (!ScheduledFutureHolder.FALL_MODE) {
                    if (acceleration > FALL_THRESHOLD) {
                        ScheduledFutureHolder.cancelAllFutures();
                        Log.d("Main", "ENTERING FALL MODE");
                        ScheduledFutureHolder.FALL_MODE = true;
                        fallTime = curTime;


                        ScheduledFutureHolder.VIBRATE = scheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("main", "notify");
                                createNotification();

                                Log.d("main", "VIBRATE");
                                ScheduledFutureHolder.VIBRATOR = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    ScheduledFutureHolder.VIBRATOR.vibrate(VibrationEffect.createWaveform(new long[]{0, 400, 200, 400, 100, 800}, new int[]{250, 250, 250, 250, 250, 250}, 0));
                                } else {
                                    //deprecated in API 26
                                    ScheduledFutureHolder.VIBRATOR.vibrate(new long[]{0, 400, 200, 400, 800, 100}, 0);
                                }
//                                ScheduledFutureHolder.VIBRATOR = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                    ScheduledFutureHolder.VIBRATOR.vibrate(VibrationEffect.createWaveform(new long[]{0, 400, 200, 400}, new int[]{250}, -1));
//                                } else {
//                                    //deprecated in API 26
//                                    ScheduledFutureHolder.VIBRATOR.vibrate(new long[]{0, 400, 200, 400}, -1);
//                                }
                            }
                        }, vibrate_delay, SECONDS);

                        ScheduledFutureHolder.ALERT = scheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("Main", "ALERT");
                                Alert.ALERT(getApplicationContext(), contact1Number, contact2Number);
//                                senSensorManager.unregisterListener(AccelerometerService.this);
//                                stopSelf();
                            }
                        }, alert_delay, SECONDS);
                    }
                }


                if(ScheduledFutureHolder.FALL_MODE && curTime - fallTime > 3000) {
                    if (acceleration > SMALL_MOVEMENT_THRESHOLD) {
                        ScheduledFutureHolder.cancelAllFutures();
                    }
                }
            }
        }

    }

    private void createNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(AccelerometerService.this, "notification_channel")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Fall Detected")
                .setContentText("Tap if you are okay")
                .setAutoCancel(true)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        Intent intent = new Intent(AccelerometerService.this, CancelActivity.class);
        //intent.putExtra("action","cancel");
        PendingIntent pendingIntent = PendingIntent.getActivity(AccelerometerService.this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //builder.addAction(0,"I'M OKAY", pendingIntent);
        builder.setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId = "notification_channel";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Assist Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        notificationManager.notify(0, builder.build());

    }

}
