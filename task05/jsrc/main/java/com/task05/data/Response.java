package com.task05.data;

import java.util.Map;


public class Response {

	private String status;
	private Map<String, Object> event;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Map<String, Object> getEvent() {
		return event;
	}

	public void setEvent(Map<String, Object> event) {
		this.event = event;
	}
}
