package com.task10;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.ListUserPoolClientsRequest;
import com.amazonaws.services.cognitoidp.model.ListUserPoolsRequest;
import com.amazonaws.services.cognitoidp.model.UserPoolClientDescription;
import com.amazonaws.services.cognitoidp.model.UserPoolDescriptionType;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.Dependencies;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.task10.handlers.HandlersRegistry;
import com.task10.handlers.SecParams;

import java.util.Optional;


@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role"
)
@Dependencies({
		@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "Tables"),
		@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "Reservations"),
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final String USER_POOL_NAME = "cmtr-dbb8bb3b-simple-booking-userpool-test";

	private HandlersRegistry handlersRegistry;
	private DynamoDB dynamoDB;
	private SecParams secParams;

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		System.out.println("Event " + event);

		initHandlersRegistry();
		initDynamoDbClient();
		initSecParams();

		return handlersRegistry.getHandler(event).map(handler -> handler.handle(event, context, dynamoDB, secParams))
				.orElseGet(this::getErrorResponse);
	}

	private void initHandlersRegistry() {
		if (handlersRegistry == null) {
			handlersRegistry = new HandlersRegistry();
		}
	}

	private void initDynamoDbClient() {
		if (dynamoDB != null) {
			return;
		}
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
		this.dynamoDB = new DynamoDB(client);
	}

	private void initSecParams() {
		if (secParams != null) {
			return;
		}
		AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClient.builder().build();
		secParams = new SecParams();
		secParams.setCognitoIdentityProvider(cognitoIdentityProvider);
		secParams.setUserPoolId(getUserPoolId(cognitoIdentityProvider));
		secParams.setClientId(getClientId(cognitoIdentityProvider, secParams.getUserPoolId()));
	}

	private String getUserPoolId(AWSCognitoIdentityProvider cognitoIdentityProvider) {
		ListUserPoolsRequest listUserPoolsRequest = new ListUserPoolsRequest();
		listUserPoolsRequest.setMaxResults(50);
		return cognitoIdentityProvider.listUserPools(listUserPoolsRequest)
									  .getUserPools()
									  .stream()
									  .filter(pool -> pool.getName().equals(USER_POOL_NAME))
									  .findFirst()
									  .map(userPool -> {
										  System.out.println(userPool);
										  return userPool;
									  })
									  .map(UserPoolDescriptionType::getId)
									  .orElseThrow(() -> new RuntimeException("User pool not found"));
	}

	private String getClientId(AWSCognitoIdentityProvider cognitoIdentityProvider, String userPoolId) {
		ListUserPoolClientsRequest listUserPoolClientsRequest = new ListUserPoolClientsRequest();
		listUserPoolClientsRequest.setUserPoolId(userPoolId);
		listUserPoolClientsRequest.setMaxResults(50);

		return cognitoIdentityProvider.listUserPoolClients(listUserPoolClientsRequest)
									  .getUserPoolClients()
									  .stream()
									  .findFirst()
									  .map(userPoolClient -> {
										  System.out.println(userPoolClient);
										  return userPoolClient;
									  })
									  .map(UserPoolClientDescription::getClientId)
									  .orElseThrow(() -> new RuntimeException("User pool client not found"));
	}

	private APIGatewayProxyResponseEvent getErrorResponse() {
		return new APIGatewayProxyResponseEvent()
				.withStatusCode(400);
	}

}
