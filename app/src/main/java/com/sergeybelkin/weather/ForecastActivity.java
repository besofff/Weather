package com.sergeybelkin.weather;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sergeybelkin.weather.model.Forecast;
import com.sergeybelkin.weather.model.Weather;

import java.util.List;

public class ForecastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        Weather weather = (Weather) getIntent().getSerializableExtra(Constants.PARAM_WEATHER);
        MyAdapter adapter = new MyAdapter(weather.getForecasts());
        ListView forecastList = findViewById(R.id.forecast_list);
        forecastList.setAdapter(adapter);
    }

    class MyAdapter extends BaseAdapter {

        List<Forecast> mForecasts;

        int[] colors = new int[]{0xff4527a0, 0xff5e35b1,
                0xff0277bd, 0xff039be5, 0xff00838f, 0xff00acc1,
                0xffff8f00, 0xffffb300, 0xffdd2c00, 0xffff3d00};
        int count = 0;

        MyAdapter(List<Forecast> forecasts){
            mForecasts = forecasts;
        }

        @Override
        public int getCount() {
            return mForecasts.size();
        }

        @Override
        public Object getItem(int position) {
            return mForecasts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null){
                view = getLayoutInflater().inflate(R.layout.item, parent, false);
            }

            Forecast forecast = mForecasts.get(position);

            view.findViewById(R.id.descriptionParent).setBackgroundColor(colors[count]);
            count++;
            view.findViewById(R.id.pictureParent).setBackgroundColor(colors[count]);
            count++;
            if (count == colors.length) count = 0;

            ImageView pic = view.findViewById(R.id.item_pic);
            TextView temperature = view.findViewById(R.id.item_temp);
            TextView date = view.findViewById(R.id.item_date);
            TextView humidity = view.findViewById(R.id.item_humidity);
            TextView pressure = view.findViewById(R.id.item_pressure);

            pic.setImageResource(getImageResId(forecast));
            temperature.setText(forecast.getTemperature());
            date.setText(forecast.getDate());
            humidity.setText(forecast.getHumidity());
            pressure.setText(forecast.getPressure());

            return view;
        }

        private int getImageResId(Forecast forecast){
            String icon = forecast.getCondition().get(0).getIcon();
            return getResources().getIdentifier("_" + icon, "drawable", getPackageName());
        }
    }
}
