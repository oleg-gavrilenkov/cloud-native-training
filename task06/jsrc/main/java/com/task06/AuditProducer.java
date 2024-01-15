package com.task06;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.Dependencies;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


@LambdaHandler(lambdaName = "audit_producer",
	roleName = "audit_producer-role"
)
@Dependencies({
		@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE),
		@DependsOn(name = "Audit", resourceType = ResourceType.DYNAMODB_TABLE)
})
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

	private DynamoDB dynamoDB;

	public Void handleRequest(DynamodbEvent event, Context context) {
		System.out.println("Request body " + event);

		initDynamoDbClient();

		for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
			if ("INSERT".equals(record.getEventName())) {
				handleInsert(record.getDynamodb());
			} else if ("MODIFY".equals(record.getEventName())) {
				handleUpdate(record.getDynamodb());
			}
		}
		return null;
	}

	private void handleInsert(StreamRecord dynamodbStreamRecord) {
		String key = dynamodbStreamRecord.getKeys().get("key").getS();

		Item auditItem = new Item();
		auditItem.withString("itemKey", key)
						 .withString("modificationTime", toISO8601Format(dynamodbStreamRecord.getApproximateCreationDateTime()));
		auditItem.withMap("newValue", convertImageToMap(dynamodbStreamRecord.getNewImage()));
		saveAuditRecord(auditItem);

	}

	private void handleUpdate(StreamRecord dynamodbStreamRecord) {
		String key = dynamodbStreamRecord.getKeys().get("key").getS();
		String modificationTime = toISO8601Format(dynamodbStreamRecord.getApproximateCreationDateTime());
		Map<String, Object> oldValues = new HashMap<>(convertImageToMap(dynamodbStreamRecord.getOldImage()));
		Map<String, Object> newValues =new HashMap<>(convertImageToMap(dynamodbStreamRecord.getNewImage()));

		for (Map.Entry<String, Object> newValueEntry: newValues.entrySet()) {
			if (!Objects.equals(oldValues.get(newValueEntry.getKey()), newValueEntry.getValue())) {
				Item auditItem = new Item();
				auditItem.withString("itemKey", key)
						 .withString("modificationTime", modificationTime)
						 .with("oldValue", oldValues.get(newValueEntry.getKey()))
						 .with("newValue", newValueEntry.getValue())
						 .withString("updatedAttribute", newValueEntry.getKey());
				saveAuditRecord(auditItem);
			}
			oldValues.remove(newValueEntry.getKey());
		}

		for (Map.Entry<String, Object> oldValueEntry: oldValues.entrySet()) {
			Item auditItem = new Item();
			auditItem.withString("itemKey", key)
					 .withString("modificationTime", modificationTime)
					 .with("oldValue", oldValueEntry.getKey())
					 .with("newValue", null)
					 .withString("updatedAttribute", oldValueEntry.getKey());
			saveAuditRecord(auditItem);
		}
	}

	private String toISO8601Format(Date date) {
		LocalDateTime localDateTime = date.toInstant()
												   .atZone(ZoneId.systemDefault())
												   .toLocalDateTime();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
		//return localDateTime.atOffset(ZoneOffset.UTC).format(formatter);
		return localDateTime.format(formatter);
	}


	private void saveAuditRecord(Item item) {
		item.withPrimaryKey("id", UUID.randomUUID().toString());

		Table table = dynamoDB.getTable("cmtr-dbb8bb3b-Audit-test");

		table.putItem(item);
	}

	private void initDynamoDbClient() {
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
		this.dynamoDB = new DynamoDB(client);
	}

	private Map<String, Object> convertImageToMap(Map<String, AttributeValue> image) {
		return image.entrySet().stream()
					   .collect(Collectors
										.toMap(entry -> entry.getKey(), entry -> getValue(entry.getValue())));
	}

	private Object getValue(AttributeValue attributeValue) {
		if (attributeValue.getS() != null) {
			return attributeValue.getS();
		} else if (attributeValue.getN() != null) {
			return new BigDecimal(attributeValue.getN());
		} else if (attributeValue.getB() != null) {
			return attributeValue.getB();
		} else if (attributeValue.getBOOL() != null) {
			return attributeValue.getBOOL();
		} else if (attributeValue.getNULL() != null && attributeValue.getNULL()) {
			return null;
		} else if (attributeValue.getBS() != null) {
			return attributeValue.getBS();
		} else if (attributeValue.getL() != null) {
			return attributeValue.getL().stream()
								 .map(this::getValue)
								 .collect(Collectors.toList());
		} else if (attributeValue.getM() != null) {
			return attributeValue.getM().entrySet().stream()
								 .collect(Collectors.toMap(entry -> entry.getKey(), entry -> getValue(entry.getValue())));
		} else if (attributeValue.getNS() != null) {
			return attributeValue.getNS().stream().map(BigDecimal::new).collect(Collectors.toList());
		} else if (attributeValue.getSS() != null) {
			return attributeValue.getS();
		} else {
			return null;
		}
	}
}
