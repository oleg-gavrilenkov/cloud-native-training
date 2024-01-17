package com.task10.handlers.impl;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.MessageActionType;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.data.SignupRequest;
import com.task10.handlers.SecParams;


public class SignupHandler extends AbstractEventHandler {

	private ObjectMapper objectMapper = new ObjectMapper();


	@Override
	public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event, Context context,
											   DynamoDB dynamoDB, SecParams secParams) {
		APIGatewayProxyResponseEvent response;
		try {
			AWSCognitoIdentityProvider cognitoIdentityProvider = secParams.getCognitoIdentityProvider();
			SignupRequest signupRequest = objectMapper.readValue(event.getBody(), SignupRequest.class);
			AdminCreateUserRequest adminCreateUserRequest = new AdminCreateUserRequest().withUserPoolId(secParams.getUserPoolId())
																				  .withUsername(signupRequest.getEmail())
																				  .withUserAttributes(new AttributeType().withName("email").withValue(signupRequest.getEmail()),
																									  new AttributeType().withName("email_verified").withValue("true"),
																									  new AttributeType().withName("given_name").withValue(signupRequest.getFirstName()),
																									  new AttributeType().withName("family_name").withValue(signupRequest.getLastName()))
																				  .withMessageAction(MessageActionType.SUPPRESS);

			AdminCreateUserResult result = cognitoIdentityProvider.adminCreateUser(adminCreateUserRequest);
			if (result.getUser() != null) {
				AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest().withPassword(signupRequest.getPassword())
						.withUserPoolId(secParams.getUserPoolId())
						.withUsername(signupRequest.getEmail())
						.withPermanent(true);

				cognitoIdentityProvider.adminSetUserPassword(adminSetUserPasswordRequest);

				response = new APIGatewayProxyResponseEvent().withStatusCode(200);
			} else {
				throw new RuntimeException("User creation failed");
			}

		} catch (Exception e) {
			logError(e, context);
			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(400);
		}
		return response;
	}

	@Override
	protected String getResource() {
		return "/signup";
	}

	@Override
	protected String getMethod() {
		return "POST";
	}

}
