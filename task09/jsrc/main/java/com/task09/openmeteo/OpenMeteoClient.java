package com.task09.openmeteo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task09.openmeteo.data.Forecast;

import java.io.IOException;
import java.net.URL;

public class OpenMeteoClient {

	private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=temperature_2m&timezone=%s";

	public static Forecast getForecast(String latitude, String longitude, String timezone) throws IOException {
		String url = String.format(API_URL, latitude, longitude, timezone);

		return new ObjectMapper().readValue(new URL(url), Forecast.class);
	}
}
