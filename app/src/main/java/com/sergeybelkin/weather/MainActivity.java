package com.sergeybelkin.weather;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.io.IOException;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.sergeybelkin.weather.model.Weather;
import com.sergeybelkin.weather.model.Forecast;

public class MainActivity extends AppCompatActivity {

    private Weather mWeather;

    LoadRelevantWeatherTask mTask;
    DatabaseHelper mHelper;
    LocationManager mLocationManager;
    Config mConfig;

    ImageView pic;
    TextView cityName, temperature, humidity, pressure, wind_speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mConfig = Config.getConfig(this);

        mTask = (LoadRelevantWeatherTask) getLastCustomNonConfigurationInstance();
        if (mTask == null){
            mTask = new LoadRelevantWeatherTask();
        }
        mTask.link(this);

        pic = findViewById(R.id.pic);
        cityName = findViewById(R.id.city);
        temperature = findViewById(R.id.temperature);
        humidity = findViewById(R.id.humidity);
        pressure = findViewById(R.id.pressure);
        wind_speed = findViewById(R.id.wind);

        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.OUT_STATE)){
            mWeather = (Weather) savedInstanceState.getSerializable(Constants.OUT_STATE);
            showCurrentWeather(mWeather);
        }

        mHelper = DatabaseHelper.getInstance(this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Constants.OUT_STATE, mWeather);
    }

    private void requestLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.unavailable_permission), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000*10, 1000*10, locationListener);
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000*10, 1000*10, locationListener);
        } else {
            showGeoMissingDialog();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mWeather != null) return;

        if (!mConfig.isUpdatingOnStartup()) return;

        if (mConfig.isUpdatingByWifiOnly()){
            if (ConnectionDetector.isConnectedViaWifi(this)){
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "WI-FI отключен", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (ConnectionDetector.isConnected(this)){
                requestLocationUpdates();
            } else {
                showNetMissingDialog();
            }
        }
    }

    private void showNetMissingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.net_title));
        builder.setMessage(getString(R.string.net_message));
        builder.setNegativeButton(getString(R.string.btn_cancel), netClickListener);
        builder.setPositiveButton(getString(R.string.btn_ok), netClickListener);
        builder.setCancelable(false);
        builder.show();
    }

    private void showGeoMissingDialog(){
        AlertDialog.Builder builderGeo = new AlertDialog.Builder(this);
        builderGeo.setTitle(getString(R.string.geo_title));
        builderGeo.setMessage(getString(R.string.geo_message));
        builderGeo.setNegativeButton(getString(R.string.btn_cancel), geoClickListener);
        builderGeo.setPositiveButton(getString(R.string.btn_ok), geoClickListener);
        builderGeo.setCancelable(false);
        builderGeo.show();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        mTask.unlink();
        return mTask;
    }

    static class LoadRelevantWeatherTask extends AsyncTask<Double, Weather, Weather>{

        MainActivity activity;
        private String apiKey;
        private String baseUrl;

        void link(MainActivity act){
            activity = act;
            apiKey = activity.getString(R.string.api_key);
            baseUrl = activity.getString(R.string.base_url);
        }

        void unlink(){
            activity = null;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            activity.mWeather = weather;
        }

        @Override
        protected void onProgressUpdate(Weather... values) {
            super.onProgressUpdate(values);
            if (values[0] != null) {
                activity.showCurrentWeather(values[0]);
                activity.mWeather = values[0];
            }
        }

        @Override
        protected Weather doInBackground(Double... coordinates) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            APIService apiService = retrofit.create(APIService.class);
            Call<Weather> currentWeather = apiService.getCurrentWeather(coordinates[0], coordinates[1], apiKey);
            Call<Weather> forecastWeather = apiService.getForecastWeather(coordinates[0], coordinates[1], apiKey);
            try {
                Response<Weather> response = currentWeather.execute();
                Weather weather = response.body();
                publishProgress(weather);
                response = forecastWeather.execute();
                List<Forecast> forecasts = response.body().getForecasts();
                if (weather != null & forecasts != null) {
                    weather.setForecasts(forecasts);
                    activity.mHelper.updateDB(weather);
                }
                return weather;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double latitude = (double) Math.round(location.getLatitude()*100)/100;
            double longitude = (double) Math.round(location.getLongitude()*100)/100;
            Double[] coordinates = new Double[]{latitude, longitude};

            if (mHelper.isCacheRelevant(latitude, longitude)) {
                Weather weather = mHelper.getWeather(latitude, longitude);
                showCurrentWeather(weather);
                mWeather = weather;
            } else {
                if (mTask.getStatus() == AsyncTask.Status.FINISHED){
                    mTask = new LoadRelevantWeatherTask();
                    mTask.link(MainActivity.this);
                }
                if (mTask.getStatus() != AsyncTask.Status.RUNNING){
                    mTask.execute(coordinates);
                }
            }

            mLocationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private DialogInterface.OnClickListener geoClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.unavailable_geo), Toast.LENGTH_SHORT).show();
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener netClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.unavailable_info), Toast.LENGTH_SHORT).show();
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.update_btn:
                requestLocationUpdates();
                break;
            case R.id.search_btn:
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(this);
                    startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.go_to:
                /*
                if (mWeather != null && mWeather.getForecasts() != null){
                    Intent intent = new Intent(this, ForecastActivity.class);
                    intent.putExtra(Constants.PARAM_WEATHER, mWeather);
                    startActivity(intent);
                }
                */
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    double latitude = (double) Math.round(place.getLatLng().latitude*100)/100;
                    double longitude = (double) Math.round(place.getLatLng().longitude*100)/100;
                    Double[] coordinates = new Double[]{latitude, longitude};

                    if (mHelper.isCacheRelevant(latitude, longitude)) {
                        Weather weather = mHelper.getWeather(latitude, longitude);
                        showCurrentWeather(weather);
                        mWeather = weather;
                    } else {
                        if (mTask.getStatus() == AsyncTask.Status.FINISHED){
                            mTask = new LoadRelevantWeatherTask();
                            mTask.link(MainActivity.this);
                        }
                        if (mTask.getStatus() != AsyncTask.Status.RUNNING){
                            mTask.execute(coordinates);
                        }
                    }
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Toast.makeText(this, getString(R.string.unavailable_info), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showCurrentWeather(Weather weather){
        if (weather != null){
            pic.setImageResource(getImageResId(weather));
            cityName.setText(weather.getName());
            temperature.setText(weather.getTemperature());
            humidity.setText(weather.getHumidity());
            pressure.setText(weather.getPressure());
            wind_speed.setText(weather.getWindSpeed());
        } else {
            Toast.makeText(this, getString(R.string.unavailable_info), Toast.LENGTH_SHORT).show();
        }
    }

    private int getImageResId(Weather weather){
        String icon = weather.getCondition().get(0).getIcon();
        return getResources().getIdentifier("_" + icon, "drawable", getPackageName());
    }
}
