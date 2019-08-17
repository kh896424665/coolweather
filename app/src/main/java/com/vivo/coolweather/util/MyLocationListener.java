package com.vivo.coolweather.util;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

public class MyLocationListener implements BDLocationListener {
    private static final String TAG = "MyLocationListener";
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        Location.province=bdLocation.getProvince();
        Location.city = bdLocation.getCity();
        Location.country = bdLocation.getDistrict();
        Location.jingdu = bdLocation.getLongitude();
        Location.weidu = bdLocation.getLatitude();
        Location.cityCode = bdLocation.getCityCode();
//        Location.setCity(bdLocation.getCity());
//        Location.setCountry(bdLocation.getDistrict());
//        Location.setJingdu(bdLocation.getLongitude());
//        Location.setWeidu(bdLocation.getLatitude());
//        Log.d(TAG, "onReceiveLocation: "+bdLocation.getCity());
//        Log.d(TAG, "onReceiveLocation: "+bdLocation.getLatitude());
//        Log.d(TAG, "onReceiveLocation: "+bdLocation.getCityCode());
    }
}
