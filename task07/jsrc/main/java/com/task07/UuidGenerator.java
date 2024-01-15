package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.task07.model.FileContent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@LambdaHandler(lambdaName = "uuid_generator",
	roleName = "uuid_generator-role"
)
@DependsOn(resourceType = ResourceType.S3_BUCKET, name = "uuid-storage")
@RuleEventSource(targetRule = "uuid_trigger")
public class UuidGenerator implements RequestHandler<Object, Void> {

	public Void handleRequest(Object request, Context context) {
		System.out.println("Request " + request);

		String fileContent = generateFileContent();
		String fileName = getFileName();

		uploadToS3(fileName, fileContent);
		return null;
	}

	private String getFileName() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		return currentDateTime.format(formatter);
	}

	private String generateFileContent() {
		try {
			return new ObjectMapper().writeValueAsString(buildFileContent());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private FileContent buildFileContent() {
		FileContent fileContent = new FileContent();
		List<String> ids = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			ids.add(UUID.randomUUID().toString());
		}
		fileContent.setIds(ids);
		return fileContent;
	}

	private void uploadToS3(String fileName, String content) {
		AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

		try (InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
			PutObjectRequest request = new PutObjectRequest("cmtr-dbb8bb3b-uuid-storage-test", fileName, stream, null);
			s3Client.putObject(request);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
