package simple.open.meteo.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import simple.open.meteo.sdk.data.Forecast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MeteoClient {

	private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=temperature_2m&timezone=%s";


	public static Forecast getForecast(String latitude, String longitude, String timezone) throws IOException {
		String url = String.format(API_URL, latitude, longitude, timezone);
		//String response = getWeatherForecast(url);

		return new ObjectMapper().readValue(new URL(url), Forecast.class);
	}

	private static String getWeatherForecast(String apiUrl) throws IOException {
		URL url = new URL(apiUrl);
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
