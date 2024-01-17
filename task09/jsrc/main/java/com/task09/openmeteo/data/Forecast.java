package com.task09.openmeteo.data;

import java.math.BigDecimal;


public class Forecast {

	private BigDecimal latitude;
	private BigDecimal longitude;
	private BigDecimal generationtime_ms;
	private String utc_offset_seconds;
	private String timezone;
	private String timezone_abbreviation;
	private String elevation;
	private HourlyUnits hourly_units;
	private Hourly hourly;

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public BigDecimal getGenerationtime_ms() {
		return generationtime_ms;
	}

	public void setGenerationtime_ms(BigDecimal generationtime_ms) {
		this.generationtime_ms = generationtime_ms;
	}

	public String getUtc_offset_seconds() {
		return utc_offset_seconds;
	}

	public void setUtc_offset_seconds(String utc_offset_seconds) {
		this.utc_offset_seconds = utc_offset_seconds;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getTimezone_abbreviation() {
		return timezone_abbreviation;
	}

	public void setTimezone_abbreviation(String timezone_abbreviation) {
		this.timezone_abbreviation = timezone_abbreviation;
	}

	public String getElevation() {
		return elevation;
	}

	public void setElevation(String elevation) {
		this.elevation = elevation;
	}

	public HourlyUnits getHourly_units() {
		return hourly_units;
	}

	public void setHourly_units(HourlyUnits hourly_units) {
		this.hourly_units = hourly_units;
	}

	public Hourly getHourly() {
		return hourly;
	}

	public void setHourly(Hourly hourly) {
		this.hourly = hourly;
	}
}
