package com.task05;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.task05.data.Request;
import com.task05.data.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role"
)
@DependsOn(name = "Events", resourceType = ResourceType.DYNAMODB_TABLE)
public class ApiHandler implements RequestHandler<Request, Response> {

	private DynamoDB dynamoDB;

	public Response handleRequest(Request request, Context context) {
		System.out.println("Request content " + request);
		initDynamoDbClient();

		Item result = persistData(request);

		System.out.println(result);

		Response response = new Response();
		response.setStatus("201");
		response.setEvent(result.asMap());

		return response;
	}

	private void initDynamoDbClient() {
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
		this.dynamoDB = new DynamoDB(client);
	}

	private Item persistData(Request request) {
		Table table = dynamoDB.getTable("cmtr-dbb8bb3b-Events-test");

		String id = UUID.randomUUID().toString();

		table.putItem(new PutItemSpec().withItem(
				new Item().withPrimaryKey("id", id)
						  .withInt("principalId", request.getPrincipalId())
						  .withString("createdAt", getCurrentDateTimeAsISO8601())
						  .withMap("body", request.getContent())));

		GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", id);

		return table.getItem(getItemSpec);
	}

	public String getCurrentDateTimeAsISO8601() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
		return currentDateTime.format(formatter);
	}
}
