package com.sergeybelkin.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.sergeybelkin.weather.model.Forecast;
import com.sergeybelkin.weather.model.Weather;

import static com.sergeybelkin.weather.Constants.*;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "WeatherDB";
    private static final int DB_VERSION = 1;
    private static DatabaseHelper dbHelper;

    private DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DatabaseHelper getInstance(Context context){
        if (dbHelper == null){
            dbHelper = new DatabaseHelper(context);
        } return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE "+ TABLE_NAME +" ("+
                DB_KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                DB_KEY_NAME +" TEXT, "+
                DB_KEY_LONGITUDE +" REAL, "+
                DB_KEY_LATITUDE +" REAL, "+
                DB_KEY_TEMPERATURE +" REAL, "+
                DB_KEY_PRESSURE +" REAL, "+
                DB_KEY_HUMIDITY +" INTEGER, "+
                DB_KEY_WIND_SPEED +" REAL, "+
                DB_KEY_FORECAST_DATE +" INTEGER, "+
                DB_KEY_ICON +" TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { }

    public void updateDB(Weather weather){
        new Thread(new UpdateDBRunnable(weather)).start();
    }

    private Cursor checkCache(double latitude, double longitude){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String selection = DB_KEY_LATITUDE + " = ? AND " + DB_KEY_LONGITUDE + " = ?";
        String[] selectionArgs = new String[]{latitude+"", longitude+""};
        return database.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
    }

    private boolean isCachePresent(double latitude, double longitude){
        Cursor cursor = checkCache(latitude, longitude);
        return cursor.moveToFirst();
    }

    private Cursor checkCacheRelevance(double latitude, double longitude){
        Cursor cursor = checkCache(latitude, longitude);
        if (cursor.moveToFirst()){
            long currentTime = System.currentTimeMillis()/1000;
            long calcTime = cursor.getInt(cursor.getColumnIndex(DB_KEY_FORECAST_DATE));
            if ((currentTime - calcTime) < 6000000) return cursor;
        }
        return null;
    }

    public boolean isCacheRelevant(double latitude, double longitude){
        return checkCacheRelevance(latitude, longitude) != null;
    }

    public Weather getWeather(double latitude, double longitude){

        Cursor cursor = checkCacheRelevance(latitude, longitude);
        if (cursor != null){
            String name = cursor.getString(cursor.getColumnIndex(DB_KEY_NAME));
            double temperature = cursor.getDouble(cursor.getColumnIndex(DB_KEY_TEMPERATURE));
            double pressure = cursor.getDouble(cursor.getColumnIndex(DB_KEY_PRESSURE));
            int humidity = cursor.getInt(cursor.getColumnIndex(DB_KEY_HUMIDITY));
            double windSpeed = cursor.getDouble(cursor.getColumnIndex(DB_KEY_WIND_SPEED));
            int calcDate = cursor.getInt(cursor.getColumnIndex(DB_KEY_FORECAST_DATE));
            String icon = cursor.getString(cursor.getColumnIndex(DB_KEY_ICON));

            Weather weather = new Weather();
            weather.setLatitude(latitude);
            weather.setLongitude(longitude);
            weather.setName(name);
            weather.setTemperature(temperature);
            weather.setPressure(pressure);
            weather.setHumidity(humidity);
            weather.setWindSpeed(windSpeed);
            weather.setCalculationDate(calcDate);
            weather.setIcon(icon);
            weather.setForecasts(getForecasts(cursor));

            return weather;
        } else return null;
    }

    private List<Forecast> getForecasts(Cursor cursor){
        List<Forecast> list = new ArrayList<>();
        while (cursor.moveToNext()){
            double temperature = cursor.getDouble(cursor.getColumnIndex(DB_KEY_TEMPERATURE));
            double pressure = cursor.getDouble(cursor.getColumnIndex(DB_KEY_PRESSURE));
            int humidity = cursor.getInt(cursor.getColumnIndex(DB_KEY_HUMIDITY));
            double windSpeed = cursor.getDouble(cursor.getColumnIndex(DB_KEY_WIND_SPEED));
            int forecastDate = cursor.getInt(cursor.getColumnIndex(DB_KEY_FORECAST_DATE));
            String icon = cursor.getString(cursor.getColumnIndex(DB_KEY_ICON));

            Forecast forecast = new Forecast();
            forecast.setTemperature(temperature);
            forecast.setPressure(pressure);
            forecast.setHumidity(humidity);
            forecast.setWindSpeed(windSpeed);
            forecast.setForecastDate(forecastDate);
            forecast.setIcon(icon);
            list.add(forecast);
        }
        cursor.close();
        return list;
    }

    private class UpdateDBRunnable implements Runnable{

        Weather weather;

        UpdateDBRunnable(Weather weather) {
            this.weather = weather;
        }

        @Override
        public void run() {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.beginTransaction();

            double latitude = (double) Math.round(weather.getCoordinates().getLatitude()*10)/10;
            double longitude = (double) Math.round(weather.getCoordinates().getLongitude()*10)/10;

            ContentValues cv = new ContentValues();
            cv.put(DB_KEY_TEMPERATURE, weather.getMain().getTemperature());
            cv.put(DB_KEY_PRESSURE, weather.getMain().getPressure());
            cv.put(DB_KEY_HUMIDITY, weather.getMain().getHumidity());
            cv.put(DB_KEY_WIND_SPEED, weather.getWind().getSpeed());
            cv.put(DB_KEY_FORECAST_DATE, weather.getCalculationDate());
            cv.put(DB_KEY_ICON, weather.getIcon());

            if (isCachePresent(latitude, longitude)){
                String selection = "_id = ? AND " + DB_KEY_LATITUDE + " = ? AND " + DB_KEY_LONGITUDE + " = ?";
                database.update(TABLE_NAME, cv, selection, new String[]{"1", latitude+"", longitude+""});
                for (int i = 0; i < weather.getForecasts().size(); i++){
                    ContentValues values = getContentValues(weather.getForecasts().get(i));
                    database.update(TABLE_NAME, values, selection,
                            new String[]{String.valueOf(i+2), String.valueOf(latitude), String.valueOf(longitude)});
                }
            } else {
                cv.put(DB_KEY_NAME, weather.getName());
                cv.put(DB_KEY_LATITUDE, latitude);
                cv.put(DB_KEY_LONGITUDE, longitude);
                database.insert(TABLE_NAME, null, cv);
                for (int i = 0; i < weather.getForecasts().size(); i++){
                    ContentValues values = getContentValues(weather.getForecasts().get(i));
                    values.put(DB_KEY_NAME, weather.getName());
                    values.put(DB_KEY_LATITUDE, latitude);
                    values.put(DB_KEY_LONGITUDE, longitude);
                    database.insert(TABLE_NAME, null, values);
                }
            }

            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    private ContentValues getContentValues(Forecast forecast){
        ContentValues cv = new ContentValues();
        cv.put(DB_KEY_TEMPERATURE, forecast.getMain().getTemperature());
        cv.put(DB_KEY_PRESSURE, forecast.getMain().getPressure());
        cv.put(DB_KEY_HUMIDITY, forecast.getMain().getHumidity());
        cv.put(DB_KEY_WIND_SPEED, forecast.getWind().getSpeed());
        cv.put(DB_KEY_FORECAST_DATE, forecast.getForecastDate());
        cv.put(DB_KEY_ICON, forecast.getCondition().get(0).getIcon());
        return cv;
    }
}
