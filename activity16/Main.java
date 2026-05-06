package com.weather.app;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Weather Data Parser");
        System.out.println("-------------------");

        System.out.print("Enter Latitude (e.g., 14.59): ");
        double lat = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter Longitude (e.g., 120.98): ");
        double lon = Double.parseDouble(scanner.nextLine().trim());

        scanner.close();

        WeatherService service = new WeatherService();
        System.out.println("\nFetching forecast...");

        WeatherResponse response = service.getForecast(lat, lon);

        // service returns null on any failure
        if (response == null || response.getForecasts() == null) {
            System.out.println("Could not retrieve weather data.");
            return;
        }

        List<Forecast> forecasts = response.getForecasts();
        int limit = Math.min(3, forecasts.size());

        System.out.println("\n--- 3-Entry Forecast ---");
        for (int i = 0; i < limit; i++) {
            Forecast f = forecasts.get(i);
            Wind wind = f.getWind();

            if (wind == null) {
                System.out.printf("At hour %d: %d°C (wind data unavailable).%n",
                        f.getTimepoint(), f.getTemperature());
            } else {
                System.out.printf("At hour %d: %d°C with %s speed winds from the %s.%n",
                        f.getTimepoint(),
                        f.getTemperature(),
                        wind.getSpeed(),
                        wind.getDirection());
            }
        }
    }
}