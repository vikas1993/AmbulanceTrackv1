package com.vicky.ifeelsafe.reciever;

/**
 * Created by User on 29-Nov-16.
 */

public class SavePointsBean {
    String mobile,lat,longitude,lessTime;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLessTime() {
        return lessTime;
    }

    public void setLessTime(String lessTime) {
        this.lessTime = lessTime;
    }

    public SavePointsBean(String mobile, String lat, String longitude, String lessTime) {

        this.mobile = mobile;
        this.lat = lat;
        this.longitude = longitude;
        this.lessTime = lessTime;
    }
}
