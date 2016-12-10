package com.vicky.ifeelsafe.Service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vicky.ifeelsafe.reciever.ConnectivityReceiver;
import com.vicky.ifeelsafe.reciever.SavePointsBean;

import java.util.ArrayList;

/**
 * Created by User on 06-Dec-16.
 */

public class LastService extends Service {
    final String Tag ="LastService";
    // countdown driver
    CountDownTimer timer;
    private static final int TIMER_START_TIME_IN_SEC = 10000;
    boolean isTrackStarted, isInternetAvailable, isAllPointsUploaded = true;
    ArrayList<SavePointsBean> locationAllPoints;

    String mobileNo;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(Tag,"inside onbind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.e(Tag,"inside oncreate");
        super.onCreate();
        initId();
        timerFunction();

    }
    private void initId() {

        isTrackStarted = false;
        // Acquire a reference to the system Location Manager
        locationAllPoints = new ArrayList<SavePointsBean>();
        //getting intent


    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(Tag,"inside onstartcommand");
        if(intent.hasExtra("mobile")){
            Log.e(Tag,intent.getStringExtra("mobile"));
            mobileNo = intent.getStringExtra("mobile");
            timer.start();
        }else{
            Log.e(Tag,"No Mobile Found");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(Tag,"inside ondestroy");
        super.onDestroy();
    }

    private void timerFunction() {
        timer = new CountDownTimer(TIMER_START_TIME_IN_SEC, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.e(Tag,"Updating Location in : " + millisUntilFinished / 1000);
            }

            public void onFinish() {

                isInternetAvailable = ConnectivityReceiver.isConnected();
                // internet check
                if (isInternetAvailable && isAllPointsUploaded) {
                   // updateLocationToServerHere();
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

                      //  uploadAllPointsInVolley(STR_MOBILE, STR_LAT, STR_LONG, STR_LATE_TIME);

                    } else {
                        //savePointsToLocal
                        isAllPointsUploaded = false;

                    //    locationAllPoints.add(new SavePointsBean(mobileNO.toString(), lat.toString(), lng.toString(), getLateTime()));

                        timer.start();
                    }
                }
            }

        };

    }

}
