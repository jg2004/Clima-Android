package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity {
    private static final String TAG = "WeatherController";
    // Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    private final int REQUEST_CODE = 3;
    // App ID to use OpenWeather data
    final String APP_ID = "e72ca729af228beabd5d20e3b7749713";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // Set LOCATION_PROVIDER here:

    private final String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // Declare a LocationManager and a LocationListener here:

    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent changeCityIntent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(changeCityIntent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");

        Intent myIntent = getIntent();

        String newCity = myIntent.getStringExtra("city");

        if (newCity!=null){

            getWeatherForNewCity(newCity);

        }else{
            getWeatherForCurrentLocation();

        }
    }

    private void getWeatherForNewCity(String newCity){

        RequestParams params = new RequestParams();
        params.put("q", newCity);
        params.put("appId", APP_ID);
        letsDoSomeNetworking(params);

    }

    private void getWeatherForCurrentLocation() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d(TAG, "onLocationChanged: called");

                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d(TAG, "onLocationChanged: lat: " + latitude);
                Log.d(TAG, "onLocationChanged: long: " + longitude);
                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appId", APP_ID);
                letsDoSomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled: called");

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

            return;
        }
        Log.d(TAG, "getWeatherForCurrentLocation: requesting location updates");
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "onRequestPermissionsResult: permission granted!!!");
                getWeatherForCurrentLocation();

            } else {
                Log.d(TAG, "onRequestPermissionsResult: permission denied!!!");
            }
        }


    }

    private void letsDoSomeNetworking(RequestParams params) {

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d(TAG, "Success!" + response.toString());

                WeatherDataModel weatherDataModel = WeatherDataModel.fromJson(response);

                updateUI(weatherDataModel);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Fail " + throwable.toString());
                Log.d(TAG, "status code: " + statusCode);
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void updateUI(WeatherDataModel weatherDataModel) {

        mCityLabel.setText(weatherDataModel.getCity());
        mTemperatureLabel.setText(weatherDataModel.getTemperature());

        int resourceId = getResources().getIdentifier(weatherDataModel.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceId);
        Log.d(TAG, "updateUI: " + getPackageName());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationManager != null){
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
