package com.task08;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Main {

	private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude=50.4547&longitude=30.5238&hourly=temperature_2m&timezone=Europe%2FKyiv";

	public static void main(String[] args) {
		try {
			String weatherData = getWeatherForecast();
			System.out.println("Weather Forecast:\n" + weatherData);
		} catch (IOException e) {
			System.err.println("Error retrieving weather forecast: " + e.getMessage());
		}
	}

	public static String getWeatherForecast() throws IOException {
		URL url = new URL(API_URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Set request method
		connection.setRequestMethod("GET");

		// Set API key in request header

		// Get the response code
		int responseCode = connection.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			// Read the response
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			reader.close();
			return response.toString();
		} else {
			throw new IOException("Failed to retrieve weather forecast. Response Code: " + responseCode);
		}
	}
}
