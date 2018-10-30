
package com.sergeybelkin.weather.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Forecast implements Serializable{

    @SerializedName("dt")
    @Expose
    private Integer forecastDate;
    @SerializedName("main")
    @Expose
    private Main main;
    @SerializedName("weather")
    @Expose
    private List<Condition> condition = null;
    @SerializedName("clouds")
    @Expose
    private Clouds clouds;
    @SerializedName("wind")
    @Expose
    private Wind wind;

    public Integer getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(Integer forecastDate) {
        this.forecastDate = forecastDate;
    }

    public String getDate(){
        long timestampMillis = TimeUnit.SECONDS.toMillis(forecastDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm\nE, dd/MM");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        return simpleDateFormat.format(new Date(timestampMillis));
    }

    public String getTemperature(){
        return getMain().getTemperature() + " °C";
    }

    public void setTemperature(double temperature){
        if (main == null) setMain(new Main());
        main.setTemperature(temperature);
    }

    public void setPressure(double pressure){
        if (main == null) setMain(new Main());
        main.setPressure(pressure);
    }

    public void setHumidity(int humidity){
        if (main == null) setMain(new Main());
        main.setHumidity(humidity);
    }

    public void setWindSpeed(double speed){
        if (wind == null) setWind(new Wind());
        wind.setSpeed(speed);
    }

    public void setWindDirection(double direction){
        if (wind == null) setWind(new Wind());
        wind.setDegrees(direction);
    }

    public void setCloudiness(int cloudiness){
        if (clouds == null) setClouds(new Clouds());
        clouds.setCloudiness(cloudiness);
    }

    public void setDescription(String description){
        if (condition == null){
            condition = new ArrayList<>();
            condition.add(new Condition());
        }
        condition.get(0).setDescription(description);
    }

    public void setIcon(String icon){
        if (condition == null){
            condition = new ArrayList<>();
            condition.add(new Condition());
        }
        condition.get(0).setIcon(icon);
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public List<Condition> getCondition() {
        return condition;
    }

    public void setCondition(List<Condition> condition) {
        this.condition = condition;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

}
