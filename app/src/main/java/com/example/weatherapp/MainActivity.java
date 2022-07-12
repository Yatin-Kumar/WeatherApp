package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV,conditionTV,AQITV;
    private RecyclerView weather;
    private TextInputEditText cityEditText;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private int PERMISSION_CODE = 1;
    private String CityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        homeRL=findViewById(R.id.RLHome);
        loadingPB=findViewById(R.id.PBLoading);
        cityNameTV=findViewById(R.id.idCityName);
        temperatureTV=findViewById(R.id.idTemperature);
        conditionTV=findViewById(R.id.Condition);
        AQITV=findViewById(R.id.AQI);
        weather=findViewById(R.id.idRvWeather);
        cityEditText=findViewById(R.id.idEdtCity);
        backIV=findViewById(R.id.idIVBlack);
        iconIV=findViewById(R.id.idIVIcon);
        searchIV=findViewById(R.id.idIVSearch);
        weatherRVModalArrayList=new ArrayList<>();
        weatherRVAdapter =new WeatherAdapter(this,weatherRVModalArrayList);
        weather.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String []{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }
        Location location =locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        CityName=getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(CityName);

        searchIV.setOnClickListener(new View.OnClickListener () {
            @Override
                    public void onClick(View v){
                        String city = cityEditText.getText().toString();
                        if(city.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please Enter City Name",Toast.LENGTH_LONG).show();
                        }else{
                               cityNameTV.setText(CityName);
                               getWeatherInfo(city);
                        }
            }
        });


    }

    public void onRequestPermissionsResult (int requestCode, @NonNull String [] permissions, int [] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted ", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "Permission not granted",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private String getCityName (double Longitude, double Latitude){
        String cityName="Not found";
        Geocoder gcd=new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses=gcd.getFromLocation(Latitude,Longitude, 10);
            for(android.location.Address adr : addresses ){
                if(adr!=null){
                    String city=adr.getLocality();
                    if(city!=null && !city.equals("")){
                        cityName=city;
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=b56d74191ceb4e8f854163119220407&q="+cityName+"&days=1&aqi=yes&alerts=yes";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();


                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition=response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String AQI=response.getJSONObject("current").getJSONObject("air_quality").getString("pm2_5");
                    Float fAqi=Float.parseFloat(AQI);
                    Integer faqi=Math.round(fAqi);
                    String icon=response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(icon)).into(iconIV);
                    conditionTV.setText(condition);
                    AQITV.setText("PM 2.5 : " + faqi.toString());
                    if(isDay==1){
                        Picasso.get().load("https://img.lovepik.com/background/20211101/medium/lovepik-morning-beauty-mobile-phone-wallpaper-background-image_400492696.jpg").into(backIV);
                    }
                    else{
                        Picasso.get().load("https://cdn.pixabay.com/photo/2016/11/21/15/01/stars-1845852__340.jpg").into(backIV);
                    }
                    JSONObject forecastObj=response.getJSONObject("forecast");
                    JSONObject forecast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray=forecast0.getJSONArray("hour");
                    for(int i=0;i<hourArray.length();i++){
                        JSONObject hourObj =hourArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String temp=hourObj.getString("temp_c");
                        String img=hourObj.getJSONObject("condition").getString("icon");
                        weatherRVModalArrayList.add(new WeatherRVModal(time,temp,img));
                    }
                    weatherRVAdapter.notifyDataSetChanged();

                }
                catch(JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please enter valid city name",Toast.LENGTH_LONG).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}