package com.vivo.coolweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.vivo.coolweather.gson.Forecast;
import com.vivo.coolweather.gson.Weather;
import com.vivo.coolweather.util.BDLocationUtils;
import com.vivo.coolweather.util.HttpUtil;
import com.vivo.coolweather.util.Location;
import com.vivo.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private ImageView weatherInfoImg;    //天气状态图片
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button locationButton;
    private String mWeatherId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*背景图片融合*/
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_weather);

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        weatherInfoImg = (ImageView) findViewById(R.id.weather_info_img);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);   //获取刷新实例
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);      //设置下拉刷新进度条的颜色
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        locationButton = (Button) findViewById(R.id.location_button);    //定位按钮
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((this));
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

        String weatherString = prefs.getString("weather", null);

        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);

        } else {
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        swipeRefresh.setRefreshing(true);    //这两行代码实现进入界面自动刷新获取信息
        requestWeather(mWeatherId);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {          //点击按钮获取定位信息，存于Location类中
            @Override
            public void onClick(View view) {
                getLocation();
                Toast.makeText(WeatherActivity.this, "当前位置为" + Location.city, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=077b0519028e413ebfda3fad3d142dd4";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            mWeatherId = weather.basic.weatherId;         ////////////////////
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        String weatherInfoImgCode = weather.now.more.infoImgCode;    //天气状态码
        String weatherInfoImgUrl = "https://cdn.heweather.com/cond_icon/" + weatherInfoImgCode + ".png";
        Glide.with(this).load(weatherInfoImgUrl).into(weatherInfoImg);           //加载显示图片
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //        loadWeatherInfoImg(weatherInfoImgCode);      //加载天气状态码对应图片
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);       ////////忘记View了,会提示空指针异常
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);

        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    //    public void loadBingPic() {
    //        String requestBingPic = "http://guolin.tech/api/bing_pic";
    //        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
    //            @Override
    //            public void onFailure(@NotNull Call call, @NotNull IOException e) {
    //                e.printStackTrace();
    //            }
    //
    //            @Override
    //            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
    //                final String bingPic = response.body().string();
    //                Log.d(TAG, "onResponse: "+bingPic);
    //                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
    //                editor.putString("bing_pic", bingPic);
    //                editor.apply();
    //                runOnUiThread(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
    //                    }
    //                });
    //            }
    //        });
    //    }

    public void loadBingPic() {
        String requestBingPic = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String bingPic = response.body().string();
                final String bingPicUrl;
                try {
                    JSONObject jsonObject = new JSONObject(bingPic);
                    JSONArray jsonArray = jsonObject.getJSONArray("images");
                    JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                    bingPicUrl = "http://s.cn.bing.net" + jsonObject1.getString("url");
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("bing_pic", bingPicUrl);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(bingPicUrl).into(bingPicImg);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getLocation() {
        BDLocationUtils bdLocationUtils = new BDLocationUtils(WeatherActivity.this);
        bdLocationUtils.doLocation();
        bdLocationUtils.mLocationClient.start();
    }

    //    public void loadWeatherInfoImg(String weatherInfoImgCode) {
    //        String requestInfoImg = "http://cdn.heweather.com/cond_icon/"+weatherInfoImgCode+".png";
    //        HttpUtil.sendOkHttpRequest(requestInfoImg, new Callback() {
    //            @Override
    //            public void onFailure(@NotNull Call call, @NotNull IOException e) {
    //                e.printStackTrace();
    //            }
    //
    //            @Override
    //            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
    //                final String infoImg = response.body().string();
    //                Log.d(TAG, "onResponse: "+infoImg);
    //                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
    //                editor.putString("weather_info_img", infoImg);
    //                editor.apply();
    //                runOnUiThread(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        Glide.with(WeatherActivity.this).load(infoImg).into(weatherInfoImg);
    //                    }
    //                });
    //            }
    //        });
    //    }
}
