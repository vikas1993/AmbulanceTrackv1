package com.vicky.ifeelsafe.ambulancetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
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
import com.vicky.ifeelsafe.DemoLocationActivity;
import com.vicky.ifeelsafe.Dialog.Dialouge_ForgetPassword;
import com.vicky.ifeelsafe.Dialog.Loader;
import com.vicky.ifeelsafe.LocationUpdateActivity;
import com.vicky.ifeelsafe.R;
import com.vicky.ifeelsafe.Utils.Utils_prefrences;


/**
 * Created by Prince on 22-11-2016.
 */

public class Activity_login extends AppCompatActivity {

    private TextInputLayout tinl_Id, tinl_mpin,tinl_amb_id;
    private EditText edt_id, edt_mpin,edt_amb_id;
    private Button btn_login;
    private TextView txt_forget;
    private SharedPreferences pref;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        createIDS();
        clickEvent();
    }

    private void clickEvent() {

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkValidation();
            }
        });
        txt_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialouge_ForgetPassword dialog = new Dialouge_ForgetPassword(Activity_login.this);
                dialog.show();

            }
        });

    }

    private void checkValidation() {

        String user_id = edt_id.getText().toString().trim();
        String mpin = edt_mpin.getText().toString().trim();
        String amb_id = edt_amb_id.getText().toString().trim();

        if (amb_id.equalsIgnoreCase("")) {
            tinl_amb_id.setError(Html
                    .fromHtml("<font color='orange'>Enter ambulance Plate No</font>"));

            return;
        } else if (user_id.equalsIgnoreCase("")) {
            tinl_Id.setError(Html
                    .fromHtml("<font color='orange'>Enter ambulance Mobile</font>"));

            return;
        } else if (mpin.equalsIgnoreCase("")) {
            tinl_mpin.setError(Html
                    .fromHtml("<font color='orange'>Enter Your- password </font>"));

            return;

        } else {

            SaveDetails(user_id, mpin,amb_id);
        }

    }

    private void SaveDetails(String user_id, String mpin ,String amb_id) {
        String url ="http://27.54.92.106/ambulance_mgmt/ip-mgmt/administrator/androidphp/amb_login.php?mobile="+user_id+"&pass="+mpin+"&amb_id="+amb_id;
       url = url.replaceAll(" ","%20");
        uploadUsingVolley(url,user_id);
    }
    private void uploadUsingVolley(String url, final String user_id) {
     final  Loader l = Loader.getInstance(Activity_login.this,"Connecting Server..");
        l.startLoader();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.e("url",url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        if(response.equalsIgnoreCase("Success")){
                         Toast.makeText(Activity_login.this,"Login Succesfull",Toast.LENGTH_SHORT).show();
                            pref.edit().putBoolean(Utils_prefrences.pref_isLogin, true).commit();
                            pref.edit().putString(Utils_prefrences.pref_mobile, user_id).commit();
                            l.stopLoader();
                         Intent i = new Intent(Activity_login.this, DemoLocationActivity.class);

                         startActivity(i);
                         finish();
                        }else{
                            l.stopLoader();
                            Toast.makeText(Activity_login.this,"Credentials are wrong please contact admin",Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                l.stopLoader();
                Toast.makeText(Activity_login.this,"There is server error please contact developer",Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    private void createIDS() {
        edt_id = (EditText) findViewById(R.id.edt_email_mobile);
        edt_mpin = (EditText) findViewById(R.id.edt_mpin);
        edt_amb_id = (EditText) findViewById(R.id.edt_ambulane_id);
        txt_forget = (TextView) findViewById(R.id.txt_forgetPassword);
        tinl_Id = (TextInputLayout) findViewById(R.id.imp_edt_Emai_mobile);
        tinl_mpin = (TextInputLayout) findViewById(R.id.imp_edt_mpin);
        tinl_amb_id = (TextInputLayout) findViewById(R.id.imp_ambulance_id);
        btn_login = (Button) findViewById(R.id.btn_login);
        //edt_amb_id.setText("HR26-CW 2934");
        //edt_id.setText("9999999990");
       // edt_mpin.setText("54321");
    }


}

