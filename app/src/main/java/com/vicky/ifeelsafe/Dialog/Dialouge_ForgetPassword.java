package com.vicky.ifeelsafe.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.vicky.ifeelsafe.R;


public class Dialouge_ForgetPassword extends Dialog {
	public Button btn_create, btn_cancel;
	public EditText edt_group;
	public Activity mContext;
	public View v = null;

	public Dialouge_ForgetPassword(Activity con) {
		super(con);
		this.mContext = con;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dilouge_forget_password);
		v = getWindow().getDecorView();
		v.setBackgroundResource(android.R.color.transparent);


		
		
		
	

	}

}