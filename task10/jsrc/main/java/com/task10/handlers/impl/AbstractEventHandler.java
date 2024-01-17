package com.task10.handlers.impl;

import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.handlers.HttpEventHandler;
import com.task10.handlers.SecParams;

import java.util.Optional;


public abstract class AbstractEventHandler implements HttpEventHandler {

	@Override
	public boolean isEventSupported(APIGatewayProxyRequestEvent event) {
		return getResource().equals(event.getResource()) && getMethod().equals(event.getHttpMethod());
	}

	protected void logError(Exception e, Context context) {
		context.getLogger().log("Error during handling " + getMethod() + " " + getResource() + ": " + e.getMessage());
	}

	protected boolean isEventAuthorized(APIGatewayProxyRequestEvent event, SecParams secParams, Context context) {
		try {
			Optional<String> token = getToken(event);
			if (!token.isPresent()) {
				return false;
			}
			GetUserRequest getUserRequest = new GetUserRequest();
			getUserRequest.setAccessToken(token.get());

			secParams.getCognitoIdentityProvider().getUser(getUserRequest);
			return true;
		} catch (Exception e) {
			logError(e, context);
			return false;
		}
	}

	protected Optional<String> getToken(APIGatewayProxyRequestEvent event) {
		String authHeaderValue = event.getHeaders().get("Authorization");
		if (authHeaderValue != null) {
			String[] authHeaderValueParts = authHeaderValue.split(" ");
			if (authHeaderValueParts.length != 2) {
				return Optional.empty();
			}
			if (!authHeaderValueParts[0].equals("Bearer")) {
				return Optional.empty();
			}
			return Optional.of(authHeaderValueParts[1]);
		} else {
			return Optional.empty();
		}
	}

	protected abstract String getResource();
	protected abstract String getMethod();

}
