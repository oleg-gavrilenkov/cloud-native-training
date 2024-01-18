package com.task10.data;

public class ReservationData {

	private Integer tableNumber;
	private String clientName;
	private String phoneNumber;
	private String date;
	private String slotTimeStart;
	private String slotTimeEnd;

	public Integer getTableNumber() {
		return tableNumber;
	}

	public void setTableNumber(Integer tableNumber) {
		this.tableNumber = tableNumber;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSlotTimeStart() {
		return slotTimeStart;
	}

	public void setSlotTimeStart(String slotTimeStart) {
		this.slotTimeStart = slotTimeStart;
	}

	public String getSlotTimeEnd() {
		return slotTimeEnd;
	}

	public void setSlotTimeEnd(String slotTimeEnd) {
		this.slotTimeEnd = slotTimeEnd;
	}
}
