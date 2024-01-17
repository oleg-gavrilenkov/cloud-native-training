package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import simple.open.meteo.sdk.MeteoClient;
import simple.open.meteo.sdk.data.Forecast;

import java.io.IOException;


@LambdaHandler(lambdaName = "api_handler",
			   roleName = "api_handler-role",
			   layers = {"sdk-layer"}
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/simple-open-meteo-sdk-1.0.jar"},
		runtime = DeploymentRuntime.JAVA8,
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<Object, Forecast> {

	public Forecast handleRequest(Object request, Context context) {
		try {
			return MeteoClient.getForecast("50.4375", "30.5", "Europe/Kiev");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
