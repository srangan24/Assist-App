package org.assist.purplemetro.assist;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;

import org.assist.purplemetro.assist.Services.AccelerometerService;

public class Alert {
    public static void ALERT(Context context, String contact1Number, String contact2Number){
        String location = Alert.getLocation(context);
        Log.d("bob", location);
        String message = "MESSAGE FROM ASSIST: A user is in trouble! Location: " + location;
        Log.d("bob", message);
//        String location =  getLocation(context);
//        String message = "MESSAGE FROM ASSIST: Your family member is in danger. Location: " + location;
        //String message = "MESSAGE FROM ASSIST: Your family member is in danger.";
        if(contact1Number != null && !contact1Number.equals("")) {
            SmsManager.getDefault().sendTextMessage(contact1Number, null, message, null, null);
        }
        if(contact2Number != null && !contact2Number.equals("")) {
            SmsManager.getDefault().sendTextMessage(contact2Number, null, message, null, null);
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:6034179936"));
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("main", "permissions");
//                                    ActivityCompat.requestPermissions(AccelerometerService.this,
//                                            new String[]{Manifest.permission.CALL_PHONE}, 0);
//                                    // TODO: Consider calling
//                                    //    ActivityCompat#requestPermissions
//                                    // here to request the missing permissions, and then overriding
//                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                    //                                          int[] grantResults)
//                                    // to handle the case where the user grants the permission. See the documentation
//                                    // for ActivityCompat#requestPermissions for more details.
//                                    return;
        }
        context.startActivity(callIntent);
    }

    public static String getLocation(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if ( Build.VERSION.SDK_INT >= 23 && context != null &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, context);

        }
        Location l = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(l != null ){
            String location = l.getLatitude() + "N" +", " + l.getLongitude() + "W";
            return location;
        }
        else return "UNKNOWN";
    }
}
