package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.events.SnsEvents;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sns_handler",
	roleName = "sns_handler-role"
)
@SnsEventSource(targetTopic = "lambda_topic")
public class SnsHandler implements RequestHandler<Object, Void> {

	public Void handleRequest(Object message, Context context) {
		System.out.println(message);
		return null;
	}
}
