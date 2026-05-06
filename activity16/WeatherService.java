package com.weather.app;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {
    private HttpClient myClient;
    private Gson gsonParser;

    public WeatherService() {
        this.myClient = HttpClient.newHttpClient();
        this.gsonParser = new Gson();
    }

    public WeatherResponse getForecast(double lat, double lon) {
        String apiUrl = "https://www.7timer.info/bin/astro.php?lon=" + lon
                + "&lat=" + lat + "&ac=0&unit=metric&output=json";

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> apiRes = myClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (apiRes.statusCode() != 200) {
                System.out.println("Could not retrieve weather data. HTTP Status: " + apiRes.statusCode());
                return null;
            }

            String rawJson = apiRes.body();
            if (rawJson == null || rawJson.isEmpty()) {
                System.out.println("Could not retrieve weather data: empty response.");
                return null;
            }

            WeatherResponse result = gsonParser.fromJson(rawJson, WeatherResponse.class);
            if (result == null || result.getForecasts() == null || result.getForecasts().isEmpty()) {
                System.out.println("Could not retrieve weather data: unexpected format.");
                return null;
            }

            return result;

        } catch (IOException e) {
            System.out.println("Could not retrieve weather data: network error.");
        } catch (InterruptedException e) {
            System.out.println("Could not retrieve weather data: request was interrupted.");
            Thread.currentThread().interrupt();
        }

        return null;
    }
}