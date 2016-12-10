package com.vicky.ifeelsafe;

import android.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.vicky.ifeelsafe.Service.LastService;
import com.vicky.ifeelsafe.Service.LocationService;
import com.vicky.ifeelsafe.Service.MyService;
import com.vicky.ifeelsafe.Utils.Utils_prefrences;
import com.vicky.ifeelsafe.ambulancetracker.Activity_login;
import com.vicky.ifeelsafe.reciever.ConnectivityReceiver;
import com.vicky.ifeelsafe.reciever.SavePointsBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DemoLocationActivity extends AppCompatActivity {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int TIMER_START_TIME_IN_SEC = 60000;
    private static final String TAG = "DemoLocationActivity";
    TextView textview_tracking_details, textview_tracking_updates, textViewLastUpdatedTime, textViewPointsCount;
    Button button_tracking, button_history;
    String string_tracking;
    String lat, lng, mobileNO;
    EditText editTextMobile;

    Geocoder geocoder;
    //offline save
    ArrayList<SavePointsBean> locationAllPoints;
    boolean isTrackStarted = false, isstart = false, isInternetAvailable, isAllPointsUploaded = true;

    public static final String BROADCAST = "prince.ambulancetracker.android.action.broadcast";
    //reciver


    // location manager for controlling updates
    LocationManager locationManager;
    private SharedPreferences pref;
    // timer handling
    Intent locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_update);

        initId();
        onClickListener();


        isstart = pref.getBoolean(Utils_prefrences.pref_isStart, false);
        String no = pref.getString(Utils_prefrences.pref_mobile, "");

        editTextMobile.setText(no);

        if(no.isEmpty()){
            editTextMobile.setEnabled(true);
        }else {
            editTextMobile.setEnabled(false);
            if (isstart && !no.equalsIgnoreCase("")) {
                button_tracking.setText("Stop Tracking");

                isTrackStarted = true;
              //  timer.start();
            } else {
                button_tracking.setText("Start Tracking");
                isTrackStarted = false;
            }
        }
        //reciver
        reciveMessage();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mMessageReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       try {
           unregisterReceiver(mMessageReceiver);
       }catch (Exception e){
           e.printStackTrace();
       }

    }

    private void onClickListener() {
        button_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTrackStarted) {
                    new AlertDialog.Builder(DemoLocationActivity.this)
                            .setMessage("Are you sure you want to Stop Tracking?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    button_tracking.setText("Start Tracking");
                                    isTrackStarted = false;
                                    //stopLocationUpdates
                                    stopLocationUpdates();
                                    pref.edit().putBoolean(Utils_prefrences.pref_isStart,false).commit();
                                    sendTrackingStatus(false);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();


                } else {

                    mobileNO = editTextMobile.getText().toString().trim();

                    if (mobileNO.isEmpty()) {
                        editTextMobile.setError(getString(R.string.err_msg_mobile));
                    } else {
                        button_tracking.setText("Stop Tracking");
                        isTrackStarted = true;
                        mobileNO = editTextMobile.getText().toString().trim();
                        pref.edit().putString(Utils_prefrences.pref_mobile,mobileNO).commit();
                        pref.edit().putBoolean(Utils_prefrences.pref_isStart,true).commit();
                        //startLocationUpdates
                        isGpsEnabled(DemoLocationActivity.this);
                        startLocationUpdates();
                        textViewLastUpdatedTime.setText("Please wait ...");
                        textViewPointsCount.setText("Please wait ...");
                        textview_tracking_updates.setText("Please wait ..." );

                        //timer.start();
                    }
                }
            }

        });
        button_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 Toast.makeText(DemoLocationActivity.this,"Coming Soon",Toast.LENGTH_SHORT).show();
                // checkConnection();
                String demo = "27.54.92.106/administrator/androidvikasphp/update_all_location.php?mobile=0987654321,0987654321&latitude=28.5863635,28.5866044415161&longitude=77.3151667,77.315265359357&date=2016-11-30 01:29:58,2016-11-30 01:30:08&lacid=vh&cellid=hbh";

                Log.e("Late Time", demo.replaceAll(" ", "%20"));
             /*   if (editTextMobile.isEnabled() == true) {
                    editTextMobile.setEnabled(false);
                } else {
                    editTextMobile.setEnabled(true);
                }*/
            }
        });
    }

    private void sendTrackingStatus(boolean isLoogedOut) {
        String url = "http://27.54.92.106/ambulance_mgmt/ip-mgmt/" +
                "administrator/androidphp/location_update_stop.php?mobile="+
                editTextMobile.getText().toString().trim()+"&stoptime="+getLateTime()+
                "&isloggedout="+isLoogedOut;
       url=url.replaceAll(" ","%20");
        Log.e("url",url);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
            Toast.makeText(DemoLocationActivity.this,response,Toast.LENGTH_SHORT).show();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.getMessage()+"");

            }
        });
        queue.add(stringRequest);
    }


    private String getLateTime() {
        Calendar calander = Calendar.getInstance();

        calander.add(Calendar.MINUTE, 500);

        calander.getTime();
        int year = calander.get(Calendar.YEAR);
        int month = calander.get(Calendar.MONTH);
        int day = calander.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = simpleDateFormat.format(calander.getTime());
        return year + "/" + (month + 1) + "/" + day + " " + time;
    }
    private void startLocationUpdates() {
        mobileNO = editTextMobile.getText().toString().trim();
        locationService.putExtra("mobile", mobileNO);
        startService(locationService);
    }

    private void stopLocationUpdates() {
        stopService(locationService);
    }

    private void isGpsEnabled(Context context) {
        final String TAG = "hi";

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(DemoLocationActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.menu_logout:

                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to exit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                pref.edit().putBoolean(Utils_prefrences.pref_isLogin, false).commit();
                                pref.edit().clear().commit();
                                stopService(locationService);
                                sendTrackingStatus(true);
                                Intent i = new Intent(DemoLocationActivity.this, Activity_login.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                // stopLocationUpdates();

                                startActivity(i);

                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    private void initId() {
        textview_tracking_details = (TextView) findViewById(R.id.text_tracking_details);
        textview_tracking_updates = (TextView) findViewById(R.id.text_tracking_updates);
        textViewLastUpdatedTime = (TextView) findViewById(R.id.text_timestamp);
        textViewPointsCount = (TextView) findViewById(R.id.text_location_points);
        button_tracking = (Button) findViewById(R.id.button_tracking);
        button_history = (Button) findViewById(R.id.button_history);
        editTextMobile = (EditText) findViewById(R.id.editTextMob);
        string_tracking = "";
        isTrackStarted = false;
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationAllPoints = new ArrayList<SavePointsBean>();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        locationService = new Intent(this, MyService.class);
         geocoder = new Geocoder(this, Locale.getDefault());

    }

    private void reciveMessage() {
        registerReceiver(mMessageReceiver, new IntentFilter("com.pycitup.BroadcastReceiver"));

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.e("i m in Broadcast recive", "i m in Broadcast recive");
            Double latitude = intent.getDoubleExtra("latitude",0.0);
            Double longitude = intent.getDoubleExtra("longitude",0.0);
            int pendingPoints = intent.getIntExtra("pendingPoints",0);
            String timeStamp = intent.getStringExtra("lastUpdated");
            boolean isBulkUpload = intent.getBooleanExtra("isBulkUpload",false);


                textViewLastUpdatedTime.setText("Last Updated On: "+timeStamp);
                textViewPointsCount.setText("Pending Points: "+pendingPoints);
                if(getAddress(latitude,longitude).isEmpty()){
                    textview_tracking_updates.setText("Updating Details: \n"+latitude+" / "+longitude );
                }else{
                textview_tracking_updates.setText("Updating Details: \n\nLast Updated Location is : "+getAddress(latitude,longitude));
                }


        }
    };

    private String getAddress(Double latitude, Double longitude) {
        List<Address> addresses = null;
        String address="";
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    // In this sample, get just a single address.
                    1);
            address = addresses.get(0).getCountryName()+", "+addresses.get(0).getLocality()+", "+addresses.get(0).getSubLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return address;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        reciveMessage();

    }

}
