package com.task10.handlers.impl;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.data.CreateReservationResponse;
import com.task10.data.ReservationData;
import com.task10.handlers.SecParams;
import org.joda.time.LocalTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class PostReservationsHandler extends AbstractEventHandler {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event, Context context,
											   DynamoDB dynamoDB, SecParams secParams) {
		if (!isEventAuthorized(event, secParams, context)) {
			return new APIGatewayProxyResponseEvent().withStatusCode(400);
		}
		APIGatewayProxyResponseEvent response;
		try {
			ReservationData reservationData = objectMapper.readValue(event.getBody(), ReservationData.class);
			validateReservation(reservationData);
			checkIfReservationPossible(reservationData, dynamoDB);

			String resUid = createReservation(reservationData, dynamoDB);
			CreateReservationResponse createReservationResponse = new CreateReservationResponse();
			createReservationResponse.setReservationId(resUid);

			response =  new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(objectMapper.writeValueAsString(createReservationResponse));


		} catch (Exception e) {
			logError(e, context);
			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(400);
		}
		return response;
	}

	private void checkIfReservationPossible(ReservationData reservationData, DynamoDB dynamoDB) {
		if (!doestTableExist(reservationData, dynamoDB)) {
			throw new RuntimeException("Table with number " + reservationData.getTableNumber() + " doesn't exist");
		}

		if (isTableReserved(reservationData, dynamoDB)) {
			throw new RuntimeException("Table with number " + reservationData.getTableNumber() + " already reserved");
		}
	}

	private boolean isTableReserved(ReservationData reservationData, DynamoDB dynamoDB) {
		Table dbTable = dynamoDB.getTable("cmtr-dbb8bb3b-Reservations-test");

		Map<String, String> nameMap = new HashMap<>();
		nameMap.put("#tableNumber", "tableNumber");
		ScanSpec scanSpec = new ScanSpec().withFilterExpression("#tableNumber = :v_precip")
										  .withNameMap(nameMap)
										  .withValueMap(new ValueMap().withNumber(":v_precip", reservationData.getTableNumber()));


		return dbTable.scan(scanSpec).iterator().hasNext();
	}

	private boolean doestTableExist(ReservationData reservationData, DynamoDB dynamoDB) {
		Table dbTable = dynamoDB.getTable("cmtr-dbb8bb3b-Tables-test");

		Map<String, String> nameMap = new HashMap<>();
		nameMap.put("#number", "number");
		ScanSpec scanSpec = new ScanSpec().withFilterExpression("#number = :v_precip")
										  .withNameMap(nameMap)
										  .withValueMap(new ValueMap().withNumber(":v_precip", reservationData.getTableNumber()));

		return dbTable.scan(scanSpec).iterator().hasNext();
	}

	private String createReservation(ReservationData reservationData, DynamoDB dynamoDB) {
		String uid = UUID.randomUUID().toString();

		Item item = new Item();
		item.withPrimaryKey("id", uid)
			.with("tableNumber", reservationData.getTableNumber())
			.with("clientName", reservationData.getClientName())
			.with("phoneNumber", reservationData.getPhoneNumber())
			.with("date", reservationData.getDate())
			.with("slotTimeStart", reservationData.getSlotTimeStart())
			.with("slotTimeEnd", reservationData.getSlotTimeEnd());

		Table reservationsTable = dynamoDB.getTable("cmtr-dbb8bb3b-Reservations-test");
		reservationsTable.putItem(item);

		return uid;
	}

	private void validateReservation(ReservationData reservationData) {
		if (reservationData.getTableNumber() == null) {
			throw new RuntimeException("Reservation: Table number cannot not be null");
		}
		if (reservationData.getClientName() == null) {
			throw new RuntimeException("Reservation: Client name cannot not be null");
		}
		if (reservationData.getPhoneNumber() == null) {
			throw new RuntimeException("Reservation: Phone number cannot not be null");
		}
		if (reservationData.getDate() == null) {
			throw new RuntimeException("Reservation: Date cannot not be null");
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			formatter.parse(reservationData.getDate());
		} catch (ParseException e) {
			throw new RuntimeException("Reservation: Date has incorrect format", e);
		}
		if (reservationData.getSlotTimeStart() == null) {
			throw new RuntimeException("Reservation: SlotTimeStart cannot not be null");
		}
		LocalTime.parse(reservationData.getSlotTimeStart());
		if (reservationData.getSlotTimeEnd() == null) {
			throw new RuntimeException("Reservation: SlotTimeEnd cannot not be null");
		}
		LocalTime.parse(reservationData.getSlotTimeEnd());
	}

	@Override
	protected String getResource() {
		return "/reservations";
	}

	@Override
	protected String getMethod() {
		return "POST";
	}
}
