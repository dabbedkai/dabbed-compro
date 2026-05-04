package activity15;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.io.IOException;

public class WeatherFetcher {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Weather Data Fetcher");
        System.out.println("--------------------");
        
        // ask the user for latitude and longitude
        System.out.print("Enter Latitude (e.g., 14.59): ");
        String lat = scanner.nextLine();
        
        System.out.print("Enter Longitude (e.g., 120.98): ");
        String lon = scanner.nextLine();

        // put the url together dynamically based on user input
        String apiUrl = "https://www.7timer.info/bin/astro.php?lon=" + lon + "&lat=" + lat + "&ac=0&unit=metric&output=json";
        
        // wrap network logic in a try-catch just in case the internet drops
        try {
            // set up the http client
            HttpClient client = HttpClient.newHttpClient();
            
            // create the get request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET() 
                    .build();

            System.out.println("\nFetching data...");

            // send the request and save the response as a string
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // check if the request was successful (status code 200)
            if (response.statusCode() == 200) {
                System.out.println("Success! Here is the raw JSON data:\n");
                System.out.println(response.body());
            } else {
                // handle non-200 status codes like 404 or 500
                System.out.println("Oops, something went wrong.");
                System.out.println("HTTP Status Code: " + response.statusCode());
            }

        } catch (IOException e) {
            // handles basic internet connection problems
            System.out.println("Network error: Could not connect. Please check your internet connection.");
        } catch (InterruptedException e) {
            // handles if the program gets interrupted while waiting
            System.out.println("Error: The request was interrupted.");
        } finally {
            // close the scanner 
            scanner.close();
        }
    }
}