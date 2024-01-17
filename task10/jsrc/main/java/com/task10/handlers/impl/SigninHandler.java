package com.task10.handlers.impl;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.data.SigninRequest;
import com.task10.data.SigninResponse;
import com.task10.handlers.SecParams;


public class SigninHandler extends AbstractEventHandler {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event, Context context,
											   DynamoDB dynamoDB, SecParams secParams) {
		APIGatewayProxyResponseEvent response;
		try {
			SigninRequest signinRequest = objectMapper.readValue(event.getBody(), SigninRequest.class);
			AWSCognitoIdentityProvider cognitoIdentityProvider = secParams.getCognitoIdentityProvider();
			AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest().withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
					.withUserPoolId(secParams.getUserPoolId())
					.withClientId(secParams.getClientId())
					.addAuthParametersEntry("USERNAME", signinRequest.getEmail())
					.addAuthParametersEntry("PASSWORD", signinRequest.getPassword());

			AdminInitiateAuthResult result = cognitoIdentityProvider.adminInitiateAuth(authRequest);

			SigninResponse signinResponse = new SigninResponse();
			signinResponse.setAccessToken(result.getAuthenticationResult().getAccessToken());

			response = new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(objectMapper.writeValueAsString(signinResponse));

		} catch (Exception e) {
			logError(e, context);
			response = new APIGatewayProxyResponseEvent().withStatusCode(400);
		}
		return response;
	}

	@Override
	protected String getResource() {
		return "/signin";
	}

	@Override
	protected String getMethod() {
		return "POST";
	}
}
