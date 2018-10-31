package com.sergeybelkin.weather;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {

    private SharedPreferences mPreferences;

    public static Config getConfig(Context context){
        return new Config(context);
    }

    private Config(Context context){
        mPreferences = context.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE);
    }

    public void setUpdateByWifiOnly(boolean isUpdatingByWifiOnly){
        mPreferences
                .edit()
                .putBoolean(Constants.PREFS_KEY_UPDATE_BY_WIFI, isUpdatingByWifiOnly)
                .apply();
    }

    public boolean isUpdatingByWifiOnly(){
        return mPreferences
                .getBoolean(Constants.PREFS_KEY_UPDATE_BY_WIFI, false);
    }

    public void setUpdateOnStartUp(boolean isUpdatingOnStartUp){
        mPreferences
                .edit()
                .putBoolean(Constants.PREFS_KEY_UPDATE_ON_STARTUP, isUpdatingOnStartUp)
                .apply();
    }

    public boolean isUpdatingOnStartup(){
        return mPreferences
                .getBoolean(Constants.PREFS_KEY_UPDATE_ON_STARTUP, true);
    }

    public void setAskingWhenLeaving(boolean isAskingWhenLeaving){
        mPreferences
                .edit()
                .putBoolean(Constants.PREFS_KEY_ASK_WHEN_LEAVING, isAskingWhenLeaving)
                .apply();
    }

    public boolean isAskingWhenLeaving(){
        return mPreferences
                .getBoolean(Constants.PREFS_KEY_ASK_WHEN_LEAVING, true);
    }
}
