package com.sergeybelkin.weather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener{

    Switch updateByWifi, updateOnStartup, askWhenLeaving;

    private Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mConfig = Config.getConfig(this);

        updateByWifi = findViewById(R.id.switch_update_by_wifi);
        updateOnStartup = findViewById(R.id.switch_update_on_startup);
        askWhenLeaving = findViewById(R.id.switch_ask);

        updateByWifi.setChecked(mConfig.isUpdatingByWifiOnly());
        updateOnStartup.setChecked(mConfig.isUpdatingOnStartup());
        askWhenLeaving.setChecked(mConfig.isAskingWhenLeaving());

        updateByWifi.setOnCheckedChangeListener(this);
        updateOnStartup.setOnCheckedChangeListener(this);
        askWhenLeaving.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.switch_update_by_wifi:
                mConfig.setUpdateByWifiOnly(isChecked);
                break;
            case R.id.switch_update_on_startup:
                mConfig.setUpdateOnStartUp(isChecked);
                break;
            case R.id.switch_ask:
                mConfig.setAskingWhenLeaving(isChecked);
                break;
        }
    }
}
