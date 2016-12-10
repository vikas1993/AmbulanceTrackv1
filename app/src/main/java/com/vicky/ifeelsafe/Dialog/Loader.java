package com.vicky.ifeelsafe.Dialog;

import android.app.ProgressDialog;
import android.content.Context;


/**
 * Created by User on 02-Dec-16.
 */

public class Loader {
    Context ctx;
    String loadingMessage;
    ProgressDialog progressDialog;
    private Loader(Context ctx, String loadingMessage) {
        this.ctx = ctx;
        this.loadingMessage = loadingMessage;
        progressDialog = new ProgressDialog(ctx);
    }
    public  static Loader getInstance(Context ctx,String msg){
        return new Loader(ctx,msg);
    }
    public void startLoader(){
        progressDialog.setTitle(loadingMessage);
        progressDialog.show();
    }
    public   void stopLoader(){
        progressDialog.dismiss();
    }
    public void setTitle(){
        progressDialog.setTitle(loadingMessage);
    }
}
