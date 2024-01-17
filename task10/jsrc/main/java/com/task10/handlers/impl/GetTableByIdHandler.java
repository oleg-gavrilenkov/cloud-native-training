package com.task10.handlers.impl;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.handlers.SecParams;


public class GetTableByIdHandler extends AbstractEventHandler {

	private ObjectMapper objectMapper = new ObjectMapper();

	{
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	@Override
	public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event, Context context,
											   DynamoDB dynamoDB, SecParams secParams) {
		if (!isEventAuthorized(event, secParams, context)) {
			return new APIGatewayProxyResponseEvent().withStatusCode(400);
		}
		APIGatewayProxyResponseEvent response;
		try {
			int id = getTableId(event);

			Table dbTable = dynamoDB.getTable("cmtr-dbb8bb3b-Tables-test");

			GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", id);

			Item item = dbTable.getItem(getItemSpec);

			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(objectMapper.writeValueAsString(item.asMap()));
		} catch (Exception e) {
			logError(e, context);
			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(400);
		}
		return response;
	}

	@Override
	protected String getResource() {
		return "/tables/{tableId}";
	}

	@Override
	protected String getMethod() {
		return "GET";
	}

	private int getTableId(APIGatewayProxyRequestEvent event) {
		String id = event.getPath().split("/")[2];
		return Integer.valueOf(id);
	}
}
