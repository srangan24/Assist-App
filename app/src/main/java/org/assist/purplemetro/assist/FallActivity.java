package org.assist.purplemetro.assist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.assist.purplemetro.assist.Services.AccelerometerService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FallActivity extends AppCompatActivity {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall);
        Log.d("main", "in fall activity");
        scheduledFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {

            }
        }, 10, SECONDS);

    }

    public void onTap(View v){
        Log.d("main", "cancelling");
        scheduledFuture.cancel(true);
    }
}
