package com.vivo.coolweather.util;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;

public class BDLocationUtils {
    private static final String TAG = "BDLocationUtils";
    public Context context;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    public BDLocationUtils(Context context) {
        this.context = context;
    }

    public void doLocation(){
        mLocationClient = new LocationClient(context.getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        initLocation();
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);

    }
}
