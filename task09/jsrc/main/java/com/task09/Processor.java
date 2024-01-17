package com.task09;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task09.openmeteo.OpenMeteoClient;
import com.task09.openmeteo.data.Forecast;

import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "processor",
			   roleName = "processor-role",
			   tracingMode = TracingMode.Active
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@DependsOn(name = "Weather", resourceType = ResourceType.DYNAMODB_TABLE)
public class Processor implements RequestHandler<Object, Object> {

	private DynamoDB dynamoDB;

	public Object handleRequest(Object request, Context context) {
		try {
			Forecast forecast = OpenMeteoClient.getForecast("50.4375", "30.5", "Europe/Kiev");
			Map<String, Object> forecastAsMap = new ObjectMapper()
					.convertValue(forecast, new TypeReference<Map<String, Object>>() {});
			initDynamoDbClient();
			saveForecast(forecastAsMap);
			return forecast;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void saveForecast(Map<String, Object> forecastAsMap) {
		Item item = new Item();
		item.withPrimaryKey("id", UUID.randomUUID().toString());
		item.withMap("forecast", forecastAsMap);

		Table table = dynamoDB.getTable("cmtr-dbb8bb3b-Weather-test");

		table.putItem(item);
	}

	private void initDynamoDbClient() {
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
		this.dynamoDB = new DynamoDB(client);
	}
}
