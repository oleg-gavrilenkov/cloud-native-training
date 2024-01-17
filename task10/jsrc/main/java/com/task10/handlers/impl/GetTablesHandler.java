package com.task10.handlers.impl;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.data.TableData;
import com.task10.data.TablesList;
import com.task10.handlers.SecParams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GetTablesHandler extends AbstractEventHandler {

	private ObjectMapper objectMapper = new ObjectMapper();

	{
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	@Override
	protected String getMethod() {
		return "GET";
	}

	@Override
	protected String getResource() {
		return "/tables";
	}

	@Override
	public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event, Context context,
											   DynamoDB dynamoDB, SecParams secParams) {
		if (!isEventAuthorized(event, secParams, context)) {
			return new APIGatewayProxyResponseEvent().withStatusCode(400);
		}

		Table dbTable = dynamoDB.getTable("cmtr-dbb8bb3b-Tables-test");
		ItemCollection<ScanOutcome> items = dbTable.scan();

		Iterator<Item> iterator = items.iterator();
		List<TableData> tables = new ArrayList<>();

		while (iterator.hasNext()) {
			tables.add(objectMapper.convertValue(iterator.next().asMap(), TableData.class));
		}

		TablesList tablesList = new TablesList();
		tablesList.setTables(tables);

		APIGatewayProxyResponseEvent response;
		try {
			 response = new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(objectMapper.writeValueAsString(tablesList));
		} catch (JsonProcessingException e) {
			logError(e, context);
			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(400);
		}
		return response;
	}
}
