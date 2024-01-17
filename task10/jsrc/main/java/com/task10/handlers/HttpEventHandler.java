package com.task10.handlers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;


public interface HttpEventHandler {

	boolean isEventSupported(APIGatewayProxyRequestEvent event);
	APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event, Context context, DynamoDB dynamoDB, SecParams secParams);

}
