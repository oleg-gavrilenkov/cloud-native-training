package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

@LambdaHandler(lambdaName = "sqs_handler",
	roleName = "sqs_handler-role"
)
@SqsTriggerEventSource(targetQueue = "async_queue", batchSize = 4)
public class SqsHandler implements RequestHandler<Object, Void> {

	public Void handleRequest(Object message, Context context) {
		System.out.println(message);
		return null;
	}
}
