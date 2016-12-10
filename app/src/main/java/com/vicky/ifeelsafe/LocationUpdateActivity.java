package com.vicky.ifeelsafe;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
import com.vicky.ifeelsafe.Utils.Utils_prefrences;
import com.vicky.ifeelsafe.ambulancetracker.Activity_login;
import com.vicky.ifeelsafe.ambulancetracker.HomePage;
import com.vicky.ifeelsafe.reciever.ConnectivityReceiver;
import com.vicky.ifeelsafe.reciever.SavePointsBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LocationUpdateActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int TIMER_START_TIME_IN_SEC = 10000;
    TextView textview_tracking_details, textview_tracking_updates, textViewLastUpdatedTime, textViewPointsCount;
    Button button_tracking, button_history;
    String string_tracking;
    String lat, lng, mobileNO;
    EditText editTextMobile;
    //offline save
    ArrayList<SavePointsBean> locationAllPoints;
    boolean isTrackStarted, isInternetAvailable, isAllPointsUploaded = true;

    String url;
    // location manager for controlling updates
    LocationManager locationManager;
    LocationListener locationListener;
    Location updatedLocation;
    private SharedPreferences pref;
    // timer handling
    CountDownTimer timer;

    private boolean isstart = false;

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
                                pref.edit().putBoolean(Utils_prefrences.pref_isStart, false).commit();
                                Intent i = new Intent(LocationUpdateActivity.this, Activity_login.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                stopLocationUpdates();

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_update);
        // For initialising textview and buttons;
        initId();
        isstart = pref.getBoolean(Utils_prefrences.pref_isStart,false);
        String no = pref.getString(Utils_prefrences.pref_mobile, "999999999");

        editTextMobile.setText(no);
        // function for creating location updates;
        if (editTextMobile.getText().toString().isEmpty()) {
            editTextMobile.setEnabled(false);
        } else {
            editTextMobile.setEnabled(false);
        }

        locationUpdates();

        //onClickListener
        onClickListener();

        timerFunction();

        if(isstart){
            button_tracking.setText("Stop Tracking");
            isTrackStarted  = true;
            timer.start();
        }else{
            button_tracking.setText("Start Tracking");
            isTrackStarted  = false;
        }
    }

    private void timerFunction() {
        timer = new CountDownTimer(TIMER_START_TIME_IN_SEC, 1000) {

            public void onTick(long millisUntilFinished) {
                textview_tracking_details.setText("Updating Location in : " + millisUntilFinished / 1000);
            }

            public void onFinish() {

                isInternetAvailable = ConnectivityReceiver.isConnected();
                // internet check
                if (isInternetAvailable && isAllPointsUploaded) {
                    updateLocationToServerHere();
                } else {
                    if (isInternetAvailable) {
                        // uploadAllPoints
                        String STR_MOBILE = "", STR_LAT = "", STR_LONG = "", STR_LATE_TIME = "";
                        for (int i = 0; i < locationAllPoints.size(); i++) {

                            String mobile = locationAllPoints.get(i).getMobile();
                            String lat = locationAllPoints.get(i).getLat();
                            String longitude = locationAllPoints.get(i).getLongitude();
                            String latTime = locationAllPoints.get(i).getLessTime();

                            if (i == 0) {
                                STR_MOBILE = STR_MOBILE + mobile;
                                STR_LAT = STR_LAT + lat;
                                STR_LONG = STR_LONG + longitude;
                                STR_LATE_TIME = STR_LATE_TIME + latTime;

                            } else {
                                STR_MOBILE = STR_MOBILE + "," + mobile;
                                STR_LAT = STR_LAT + "," + lat;
                                STR_LONG = STR_LONG + "," + longitude;
                                STR_LATE_TIME = STR_LATE_TIME + "," + latTime;

                            }
                        }
                        Log.e("Subbbb", STR_MOBILE);
                        Log.e("testtt", STR_LAT);
                        Log.e("testtt", STR_LONG);
                        Log.e("testtt", STR_LATE_TIME);

                        uploadAllPointsInVolley(STR_MOBILE, STR_LAT, STR_LONG, STR_LATE_TIME);

                    } else {
                        //savePointsToLocal
                        isAllPointsUploaded = false;
                        mobileNO = editTextMobile.getText().toString().trim();
                        lat = Double.toString(updatedLocation.getLatitude());
                        lng = Double.toString(updatedLocation.getLongitude());
                        locationAllPoints.add(new SavePointsBean(mobileNO.toString(), lat.toString(), lng.toString(), getLateTime()));
                        textViewPointsCount.setText("Pending Points :" + locationAllPoints.size());
                        timer.start();
                    }
                }
            }

        };

    }

    private void uploadAllPointsInVolley(String str_mobile, String str_lat, String str_long, String str_late_time) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        String upload_all = "http://27.54.92.106/ambulance_mgmt/ip-mgmt/administrator/androidphp/update_all_location.php?mobile=" + str_mobile + "&latitude=" + str_lat + "&longitude=" + str_long + "&date=" + str_late_time + "&lacid=vh&cellid=hbh";
        String newUrl = upload_all.replaceAll(" ", "%20");
        Log.e("newurl", newUrl);
        //String urlNew =  upload_all.replaceAll("", "%20");

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, newUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        isAllPointsUploaded = true;
                        // remove from local
                        locationAllPoints.clear();
                        textViewPointsCount.setText("Pending Points : All Updated");

                        // make it start on server
                        timer.start();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textview_tracking_updates.setText("Update details : Got an error from server side please check internet");
                timer.start();
            }
        });
// Add the request to the RequestQueue.
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

    // Method to manually check connection status

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    private void updateLocationToServerHere() {


        String locationProvider = LocationManager.NETWORK_PROVIDER;
        // Or use LocationManager.GPS_PROVIDER

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (!lastKnownLocation.toString().isEmpty()) {
            Log.e("Updating Location", lastKnownLocation.toString());
            // starting timer here again after updating data
            try {
                mobileNO = editTextMobile.getText().toString().trim();
                lat = Double.toString(updatedLocation.getLatitude());
                lng = Double.toString(updatedLocation.getLongitude());
                url = "http://27.54.92.106/ambulance_mgmt/ip-mgmt/administrator/androidphp/update_location.php?mobile=" + mobileNO + "&latitude=" + lat + "&longitude=" + lng + "&date=asds&lacid=asds&cellid=adsds";
                Log.e("url", url);
                uploadUsingVolley(url);
                textview_tracking_updates.setText("Update details : \n \nUpdating TimeStamp : " + getCurrentTime() + "\n\nLat/Long : " + updatedLocation.getLatitude() + "/" + updatedLocation.getLongitude() + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            textview_tracking_updates.setText("NO data recived");
        }


    }

    private void uploadUsingVolley(String url) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);


// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        textview_tracking_updates.setText(response.toString());
                        textview_tracking_updates.setText("Update details :\n\nUpdating TimeStamp : " + getCurrentTime() + "\n\nLat/Long : " + updatedLocation.getLatitude() + "/" + updatedLocation.getLongitude() + "\n");
                        textViewLastUpdatedTime.setText("Last point Updated on " + getCurrentTime());
                        timer.start();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textview_tracking_updates.setText("Update details : Got an error from server side please check internet");
                timer.start();
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void onClickListener() {
        button_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTrackStarted) {
                    new AlertDialog.Builder(LocationUpdateActivity.this)
                            .setMessage("Are you sure you want to Stop Tracking?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    pref.edit().putBoolean(Utils_prefrences.pref_isStart,false).commit();
                                    button_tracking.setText("Start Tracking");
                                    isTrackStarted = false;
                                    //stopLocationUpdates
                                    stopLocationUpdates();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();


                } else {

                    mobileNO = editTextMobile.getText().toString().trim();

                    if (mobileNO.isEmpty()) {
                        editTextMobile.setError(getString(R.string.err_msg_mobile));
                    } else {
                        //save pref
                        pref.edit().putBoolean(Utils_prefrences.pref_isStart,true).commit();
                        button_tracking.setText("Stop Tracking");
                        isTrackStarted = true;
                        mobileNO = editTextMobile.toString().trim();
                        //startLocationUpdates
                        isGpsEnabled(LocationUpdateActivity.this);
                        startLocationUpdates();
                        timer.start();
                    }
                }
            }

        });
        button_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(LocationUpdateActivity.this,"Coming Soon",Toast.LENGTH_SHORT).show();
                // checkConnection();
                String demo = "27.54.92.106/administrator/androidvikasphp/update_all_location.php?mobile=0987654321,0987654321&latitude=28.5863635,28.5866044415161&longitude=77.3151667,77.315265359357&date=2016-11-30 01:29:58,2016-11-30 01:30:08&lacid=vh&cellid=hbh";

                Log.e("Late Time", demo.replaceAll(" ", "%20"));
                if (editTextMobile.isEnabled() == true) {
                    editTextMobile.setEnabled(false);
                } else {
                    editTextMobile.setEnabled(true);
                }
            }
        });
    }


    private static boolean isValidMobile(String mobile) {
        return !TextUtils.isEmpty(mobile) && Patterns.PHONE.matcher(mobile).matches();
    }

    private void stopLocationUpdates() {
        // Remove the listener you previously added
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);
        timer.cancel();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);

    }

    private void locationUpdates() {
        // Define a listener that responds to location updates
      locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    private void makeUseOfNewLocation(Location location) {
        Log.e("Location Data","data : "+location.toString());
        updatedLocation = location;
    }

    private void initId() {
        textview_tracking_details = (TextView)findViewById(R.id.text_tracking_details);
        textview_tracking_updates = (TextView)findViewById(R.id.text_tracking_updates);
        textViewLastUpdatedTime = (TextView)findViewById(R.id.text_timestamp);
        textViewPointsCount = (TextView)findViewById(R.id.text_location_points);
        button_tracking = (Button) findViewById(R.id.button_tracking);
        button_history = (Button) findViewById(R.id.button_history);
        editTextMobile = (EditText) findViewById(R.id.editTextMob);
        string_tracking = "";
        isTrackStarted = false;
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationAllPoints = new ArrayList<SavePointsBean>();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
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
                            status.startResolutionForResult(LocationUpdateActivity.this, REQUEST_CHECK_SETTINGS);
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
    protected String getCurrentTime(){
        Calendar calander = Calendar.getInstance();
        calander.getTime();
        int year = calander.get(Calendar.YEAR);
        int month = calander.get(Calendar.MONTH);
        int day = calander.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a");
        String time = simpleDateFormat.format(calander.getTime());;
        return day + "/" + (month+1) + "/" + year + "  " + time;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
}
