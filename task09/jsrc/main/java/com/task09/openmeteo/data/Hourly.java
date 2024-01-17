package com.task09.openmeteo.data;

import java.math.BigDecimal;
import java.util.List;


public class Hourly {

	private List<String> time;
	private List<BigDecimal> temperature_2m;

	public List<String> getTime() {
		return time;
	}

	public void setTime(List<String> time) {
		this.time = time;
	}

	public List<BigDecimal> getTemperature_2m() {
		return temperature_2m;
	}

	public void setTemperature_2m(List<BigDecimal> temperature_2m) {
		this.temperature_2m = temperature_2m;
	}
}
