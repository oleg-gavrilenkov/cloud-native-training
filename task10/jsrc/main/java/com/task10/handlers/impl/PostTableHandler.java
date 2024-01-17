package com.task10.handlers.impl;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.data.CreateTableResponse;
import com.task10.data.TableData;
import com.task10.handlers.SecParams;


public class PostTableHandler extends AbstractEventHandler {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event, Context context,
											   DynamoDB dynamoDB, SecParams secParams) {
		if (!isEventAuthorized(event, secParams, context)) {
			return new APIGatewayProxyResponseEvent().withStatusCode(400);
		}
		Table dbTable = dynamoDB.getTable("cmtr-dbb8bb3b-Tables-test");

		APIGatewayProxyResponseEvent response;
		try {
			TableData tableData = objectMapper.readValue(event.getBody(), TableData.class);

			Item item = new Item();
			item.with("id", tableData.getId())
				.with("number", tableData.getNumber())
				.with("places", tableData.getPlaces())
				.with("isVip", tableData.getIsVip());
			if (tableData.getMinOrder() != null) {
				item.with("minOrder", tableData.getMinOrder());
			}

			dbTable.putItem(item);

			CreateTableResponse createTableResponse = new CreateTableResponse();
			createTableResponse.setId(tableData.getId());

			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(objectMapper.writeValueAsString(createTableResponse));

		} catch (Exception e) {
			logError(e, context);
			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(400);
		}
		return response;
	}

	@Override
	protected String getResource() {
		return "/tables";
	}

	@Override
	protected String getMethod() {
		return "POST";
	}
}
