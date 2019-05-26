package org.assist.purplemetro.assist;

import android.os.Vibrator;
import android.util.Log;

import java.util.concurrent.ScheduledFuture;

public class ScheduledFutureHolder {
    public static Vibrator VIBRATOR;
    public static boolean FALL_MODE;
    public static ScheduledFuture VIBRATE;
    public static ScheduledFuture ALERT;

    public static void cancelAllFutures(){
        if(VIBRATE != null && ALERT != null) {
            Log.d("main", "cancelling all futures");
            VIBRATE.cancel(true);
            ALERT.cancel(true);
            Log.d("main", "EXITING FALL MODE");
            FALL_MODE = false;
        }
        if(VIBRATOR != null){
            VIBRATOR.cancel();
        }
    }

    public static ScheduledFuture getVIBRATE() {
        return VIBRATE;
    }

    public static void setVIBRATE(ScheduledFuture VIBRATE) {
        ScheduledFutureHolder.VIBRATE = VIBRATE;
    }

    public static ScheduledFuture getALERT() {
        return ALERT;
    }

    public static void setALERT(ScheduledFuture ALERT) {
        ScheduledFutureHolder.ALERT = ALERT;
    }
}
