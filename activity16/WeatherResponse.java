package com.weather.app;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    private String product;

    @SerializedName("dataseries")
    private List<Forecast> forecasts;

    public String getProduct() {
        return product;
    }

    public List<Forecast> getForecasts() {
        return forecasts;
    }
}