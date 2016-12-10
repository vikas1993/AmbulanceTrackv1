package com.vicky.ifeelsafe.Service;

/**
 * Created by User on 05-Dec-16.
 */

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vicky.ifeelsafe.DemoLocationActivity;
import com.vicky.ifeelsafe.PersonalTimer;
import com.vicky.ifeelsafe.Utils.Utils_prefrences;
import com.vicky.ifeelsafe.reciever.ConnectivityReceiver;
import com.vicky.ifeelsafe.reciever.SavePointsBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MyService extends Service {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    boolean  isInternetAvailable, isAllPointsUploaded = true;
    ArrayList<SavePointsBean> locationAllPoints;
    // countdown driver
    PersonalTimer timer;
    Context ctx;
       int TIMER_START_TIME_IN_SEC = 15000,updatedTime=0;
//fetch location
    Location mLastLocation;
private SharedPreferences pref;

    private class LocationListener implements android.location.LocationListener {


        public LocationListener(String provider) {
            Log.i(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        ctx = getApplicationContext();
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        timer.start();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        locationAllPoints = new ArrayList<SavePointsBean>();
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        timerFunction();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        timer.cancel();
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
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
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.e(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void timerFunction() {


        timer = new PersonalTimer(TIMER_START_TIME_IN_SEC, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.e(TAG,"Updating Location in : " + millisUntilFinished / 1000);
                //Log.e(TAG,TIMER_START_TIME_IN_SEC+" tick");
            }

            public void onFinish() {
                // TODO: restart counter with millisInFuture = 4000 ( 4 seconds )
                //cancel();  // there is no need the call the cancel() method here
                String no = pref.getString(Utils_prefrences.pref_mobile, "");
                Log.e(TAG,"No is "+no +":"+mLastLocation.toString());
              //  Log.e(TAG,TIMER_START_TIME_IN_SEC+" finish");
                //sendMessageToActivity(mLastLocation,"ticktock",getApplicationContext());
                //Todo Please upload data here to server


                sendDataToServer(no,mLastLocation);

            }
        };

    }

    private void sendDataToServer(String no, Location mLastLocation) {
         isInternetAvailable = ConnectivityReceiver.isConnected();
        if(isInternetAvailable && isAllPointsUploaded){
            //Todo upload single point here
            updateLocationToServerHere(no,mLastLocation);
        }else{
            if(isInternetAvailable) {

                //Todo save points for bulk upload make
                // @isAllPoints to true
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

                uploadAllPointsInVolley(STR_MOBILE, STR_LAT, STR_LONG, STR_LATE_TIME,mLastLocation);

            }else{
                //savePointsToLocal
                isAllPointsUploaded = false;

                 String  lat = Double.toString(mLastLocation.getLatitude());
                 String   lng = Double.toString(mLastLocation.getLongitude());
                 locationAllPoints.add(new SavePointsBean(no.toString(), lat.toString(), lng.toString(), getLateTime()));
               //// TODO: 06-Dec-16 send for bulk broadcast here
                sendMessageToActivity(mLastLocation,"",getApplicationContext(),locationAllPoints.size(),true);
                timer.start();
            }
        }
    }

    private void updateLocationToServerHere(String no, final Location mLastLocation) {
       String url = "http://27.54.92.106/ambulance_mgmt/ip-mgmt/administrator/androidphp/update_location.php?mobile=" + no + "&latitude=" + mLastLocation.getLatitude() + "&longitude=" + mLastLocation.getLongitude() + "&date=asds&lacid=asds&cellid=adsds";
        Log.e("url", url);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.e(TAG,response);
                        try {


                            updatedTime = new JSONObject(response).getInt("time");

                          //  Log.e(TAG,updatedTime+" time");
                            timer.setMillisInFuture(updatedTime*1000);
                            timer.setCountdownInterval(1000);
                           // timerFunction();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }finally {
                            sendMessageToActivity(mLastLocation,getCurrentTime(),getApplicationContext(),0,false);
                            timer.start();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.getMessage()+"");
                timer.start();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void uploadAllPointsInVolley(String str_mobile, String str_lat, String str_long, String str_late_time, final Location mLastLocation) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

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
                        Log.e(TAG,"Bulk Uploaded "+response);
                        isAllPointsUploaded = true;
                        // remove from local
                        locationAllPoints.clear();
                       // textViewPointsCount.setText("Pending Points : All Updated");
                sendMessageToActivity(mLastLocation,getCurrentTime(),getApplicationContext(),0,false);
                        // make it start on server
                        timer.start();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"Bulk NotUploaded "+error.getMessage());
               // textview_tracking_updates.setText("Update details : Got an error from server side please check internet");
                timer.start();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    private static void sendMessageToActivity(Location l, String lastUpdatedOn,Context ctx,int pendingPoints,boolean isBulkUpload) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.pycitup.BroadcastReceiver");
        intent.putExtra("latitude", l.getLatitude());
        intent.putExtra("longitude", l.getLongitude());
        intent.putExtra("pendingPoints", pendingPoints);
        intent.putExtra("lastUpdated", lastUpdatedOn);
        intent.putExtra("isBulkUpload", isBulkUpload);
        ctx.sendBroadcast(intent);


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
}