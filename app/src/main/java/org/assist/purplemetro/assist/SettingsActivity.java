package org.assist.purplemetro.assist;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    Button contact1;
    Button contact2;

    String contact1Name;
    String contact2Name;
    String contact1Number;
    String contact2Number;

    private final int CONTACT1_REQUEST_CODE = 1;
    private final int CONTACT2_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contact1 = (Button) findViewById(R.id.contact1);
        contact2 = (Button) findViewById(R.id.contact2);
        updateViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateViews(){
        SharedPreferences sharedPref = getSharedPreferences("contactInfo", Context.MODE_PRIVATE);
        if(!sharedPref.getString("contact1_num", "").equals("")){
            contact1Number = sharedPref.getString("contact1_num", "");
            contact1Name = sharedPref.getString("contact1_name", "UNKNOWN");
           contact1.setText(contact1Name + ": " + contact1Number);
        }
        if(!sharedPref.getString("contact2_num", "").equals("")){
            contact2Number = sharedPref.getString("contact2_num", "");
            contact2Name = sharedPref.getString("contact2_name", "UNKNOWN");
            contact2.setText(contact2Name + ": " + contact2Number);
        }
    }

    public void pickContact1(View v){
        pickContact(CONTACT1_REQUEST_CODE);
    }

    public void pickContact2(View v){
        pickContact(CONTACT2_REQUEST_CODE);

    }

    private void pickContact(int requestCode){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("main", "permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 0);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, requestCode);

    }

    public void saveInfo(View v){
        SharedPreferences sharedPref = getSharedPreferences("contactInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("contact1_num", contact1Number);
        editor.putString("contact1_name", contact1Name);
        editor.putString("contact2_num", contact2Number);
        editor.putString("contact2_name", contact2Name);
        editor.apply();
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {

            Uri uri = data.getData();
            Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);

            if (cursor.moveToFirst()) {
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                contact1Number = cursor.getString(phoneIndex);
                contact1Name = cursor.getString(nameIndex);
                if(contact1Number != null) {
                    contact1.setText(contact1Name + ": " + contact1Number);
                }


                //Log.e("onActivityResult()", phoneIndex + " " + phoneNo + " " + nameIndex + " " + name);
            }
            cursor.close();
        }
        if (resultCode == Activity.RESULT_OK && requestCode == 2) {

            Uri uri = data.getData();
            Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);

            if (cursor.moveToFirst()) {
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                contact2Number = cursor.getString(phoneIndex);
                contact2Name = cursor.getString(nameIndex);
                if(contact2Number != null) {
                    contact2.setText(contact2Name + ": " + contact2Number);
                }

                //Log.e("onActivityResult()", phoneIndex + " " + phoneNo + " " + nameIndex + " " + name);
            }
            cursor.close();
        }
    }
}
