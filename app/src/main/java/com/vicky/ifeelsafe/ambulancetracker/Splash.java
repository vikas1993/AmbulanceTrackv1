package com.vicky.ifeelsafe.ambulancetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.vicky.ifeelsafe.DemoLocationActivity;
import com.vicky.ifeelsafe.LocationUpdateActivity;
import com.vicky.ifeelsafe.R;
import com.vicky.ifeelsafe.Utils.Utils_prefrences;


/**
 * Created by Prince on 22-11-2016.
 */

public class Splash extends AppCompatActivity {


    private SharedPreferences pref;
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        isLogin = pref.getBoolean(Utils_prefrences.pref_isLogin, false);
        startSplash();
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.fadein, R.anim.fd);

    }

    private void startSplash() {
        // TODO Auto-generated method stub
        Thread mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent i;
                if (isLogin) {
                    i = new Intent(Splash.this,
                            DemoLocationActivity.class);

                } else {
                    i = new Intent(Splash.this,
                            Activity_login.class);


                }
                startActivity(i);
                finish();


            }
        });
        mThread.start();


    }


}


