
package com.sergeybelkin.weather.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Weather implements Serializable{

    @SerializedName("coord")
    @Expose
    private Coordinates coordinates;
    @SerializedName("weather")
    @Expose
    private List<Condition> condition = null;
    @SerializedName("main")
    @Expose
    private Main main;
    @SerializedName("wind")
    @Expose
    private Wind wind;
    @SerializedName("clouds")
    @Expose
    private Clouds clouds;
    @SerializedName("list")
    @Expose
    private List<Forecast> forecasts = null;
    @SerializedName("city")
    @Expose
    private City city;
    @SerializedName("dt")
    @Expose
    private Integer calculationDate;
    @SerializedName("name")
    @Expose
    private String name;

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public List<Condition> getCondition() {
        return condition;
    }

    public void setCondition(List<Condition> condition) {
        this.condition = condition;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Integer getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(Integer calculationDate) {
        this.calculationDate = calculationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Forecast> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<Forecast> forecasts) {
        this.forecasts = forecasts;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setDescription(String description){
        if (condition == null){
            condition = new ArrayList<>();
            condition.add(new Condition());
        }
        condition.get(0).setDescription(description);
    }

    public String getDescription(){
        return condition.get(0).getDescription();
    }

    public void setLatitude(double latitude){
        if (coordinates == null) setCoordinates(new Coordinates());
        coordinates.setLatitude(latitude);
    }

    public void setLongitude(double longitude){
        if (coordinates == null) setCoordinates(new Coordinates());
        coordinates.setLongitude(longitude);
    }

    public void setTemperature(double temperature){
        if (main == null) setMain(new Main());
        main.setTemperature(temperature);
    }

    public String getTemperature(){
        return getMain().getTemperature() + " °C";
    }

    public void setPressure(double pressure){
        if (main == null) setMain(new Main());
        main.setPressure(pressure);
    }

    public String getPressure(){
        return "Давление: " + getMain().getPressure() + " мБар";
    }

    public void setHumidity(int humidity){
        if (main == null) setMain(new Main());
        main.setHumidity(humidity);
    }

    public String getHumidity(){
        return "Влажность: " + getMain().getHumidity() + " %";
    }

    public void setWindSpeed(double speed){
        if (wind == null) setWind(new Wind());
        wind.setSpeed(speed);
    }

    public String getWindSpeed(){
        return "Скорость ветра: " + getWind().getSpeed() + " м/с";
    }

    public void setWindDirection(double direction){
        if (wind == null) setWind(new Wind());
        wind.setDegrees(direction);
    }

    public String getWindDirection(){
        return "Направление ветра: " + getWindDirection(getWind().getDegrees());
    }

    private String getWindDirection(double degrees){

        String direction = null;
        if (degrees >= 337.5 | degrees < 22.5){
            direction = "север";
        } else if (degrees >= 292.5){
            direction = "северо-запад";
        } else if (degrees >= 247.5){
            direction = "запад";
        } else if (degrees >= 202.5){
            direction = "юго-запад";
        } else if (degrees >= 157.5){
            direction = "юг";
        } else if (degrees >= 112.5){
            direction = "юго-восток";
        } else if (degrees >= 67.5){
            direction = "восток";
        } else if (degrees >= 22.5){
            direction = "северо-восток";
        }
        return direction;
    }

    public void setCloudiness(int cloudiness){
        if (clouds == null) setClouds(new Clouds());
        clouds.setCloudiness(cloudiness);
    }

    public String getCloudiness(){
        return "Облачность: " + getClouds().getCloudiness() + " %";
    }

    public void setIcon(String icon){
        if (condition == null){
            condition = new ArrayList<>();
            condition.add(new Condition());
        }
        condition.get(0).setIcon(icon);
    }

    public String getIcon(){
        return condition.get(0).getIcon();
    }
}
