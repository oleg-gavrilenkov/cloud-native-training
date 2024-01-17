package com.task10.handlers.impl;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.data.ReservationData;
import com.task10.data.ReservationsList;
import com.task10.handlers.SecParams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GetReservationsHandler extends AbstractEventHandler {

	private ObjectMapper objectMapper = new ObjectMapper();

	{
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	@Override
	public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event, Context context,
											   DynamoDB dynamoDB, SecParams secParams) {
		if (!isEventAuthorized(event, secParams, context)) {
			return new APIGatewayProxyResponseEvent().withStatusCode(400);
		}
		Table reservationsTable = dynamoDB.getTable("cmtr-dbb8bb3b-Reservations-test");
		ItemCollection<ScanOutcome> items = reservationsTable.scan();

		Iterator<Item> iterator = items.iterator();
		List<ReservationData> reservations = new ArrayList<>();

		while (iterator.hasNext()) {
			reservations.add(objectMapper.convertValue(iterator.next().asMap(), ReservationData.class));
		}

		ReservationsList reservationsList = new ReservationsList();
		reservationsList.setReservations(reservations);

		APIGatewayProxyResponseEvent response;
		try {
			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(objectMapper.writeValueAsString(reservationsList));
		} catch (JsonProcessingException e) {
			logError(e, context);
			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(400);
		}
		return response;
	}
	@Override
	protected String getResource() {
		return "/reservations";
	}

	@Override
	protected String getMethod() {
		return "GET";
	}
}
