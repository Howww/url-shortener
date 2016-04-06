package org.santel.url.dao;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.internal.*;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.document.utils.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import org.slf4j.*;

import java.util.*;

public class DynamoDbBroker {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoDbMappingDao.class);
    private static final String VALUE_ATTRIBUTE_NAME = "longUrl";
    public static final String KEY_ATTRIBUTE_NAME = "shortUrl";

    private final DynamoDB dynamoDB;
    private final String tableName;

    public DynamoDbBroker(String dynamoDbUrl, String tableName) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient().withEndpoint(dynamoDbUrl);
        this.dynamoDB = new DynamoDB(client);
        this.tableName = tableName;
    }

    public boolean createTable() {
        try {
            ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(10L, 10L);

            CreateTableRequest createTableRequest = getCreateTableRequest(provisionedThroughput);
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();

            LOG.info("Successfully created table");
            return true;
        } catch (Exception e) {
            LOG.error("Failed to create table", e);
            return false;
        }
    }

    private CreateTableRequest getCreateTableRequest(ProvisionedThroughput provisionedThroughput) {
        GlobalSecondaryIndex globalSecondaryIndex = getGlobalSecondaryIndex(provisionedThroughput);

        return new CreateTableRequest(
                ImmutableList.of(new AttributeDefinition(KEY_ATTRIBUTE_NAME, ScalarAttributeType.S), new AttributeDefinition(VALUE_ATTRIBUTE_NAME, ScalarAttributeType.S)),
                tableName,
                ImmutableList.of(new KeySchemaElement(KEY_ATTRIBUTE_NAME, KeyType.HASH)),
                provisionedThroughput)
                .withGlobalSecondaryIndexes(ImmutableList.of(globalSecondaryIndex));
    }

    private GlobalSecondaryIndex getGlobalSecondaryIndex(ProvisionedThroughput provisionedThroughput) {
        return new GlobalSecondaryIndex()
                .withIndexName(VALUE_ATTRIBUTE_NAME)
                .withKeySchema(ImmutableList.of(new KeySchemaElement(VALUE_ATTRIBUTE_NAME, KeyType.HASH)))
                .withProvisionedThroughput(provisionedThroughput)
                .withProjection(new Projection()
                        .withProjectionType(ProjectionType.ALL));
    }

    public boolean deleteTable() {
        try {
            Table table = dynamoDB.getTable(tableName);
            table.delete();
            table.waitForDelete();

            LOG.info("Successfully deleted table");
            return true;
        } catch (Exception e) {
            LOG.error("Failed to delete table", e);
            return false;
        }
    }

    public boolean insertEntry(String key, String value) {
        try {
            Table table = dynamoDB.getTable(tableName);
            PutItemSpec putItemSpec = new PutItemSpec()
                    .withItem(new Item()
                            .withPrimaryKey(KEY_ATTRIBUTE_NAME, key)
                            .with(VALUE_ATTRIBUTE_NAME, value))
                    .withConditionExpression("attribute_not_exists(" + KEY_ATTRIBUTE_NAME + ") AND attribute_not_exists(" + VALUE_ATTRIBUTE_NAME + ")");
            table.putItem(putItemSpec);

            LOG.info("Successfully inserted entry {} -> {}", key, value);
            return true;
        } catch (Exception e) {
            LOG.error(String.format("Failed to insert entry %s -> %s", key, value), e);
            return false;
        }
    }

    public String queryValue(String key) {
        Table table = dynamoDB.getTable(tableName);
        ItemCollection<QueryOutcome> items = table.query(new KeyAttribute(KEY_ATTRIBUTE_NAME, key));
        IteratorSupport<Item, QueryOutcome> itemIterator = items.iterator();
        if (!itemIterator.hasNext()) {
            return null;
        }
        Item item = itemIterator.next();
        Preconditions.checkState(!itemIterator.hasNext());

        for (Map.Entry<String, Object> entry : item.attributes()) {
            if (entry.getKey().equals(VALUE_ATTRIBUTE_NAME)) {
                return String.class.cast(entry.getValue());
            }
            Preconditions.checkState(entry.getKey().equals(KEY_ATTRIBUTE_NAME));
        }
        throw new IllegalStateException("Shouldn't get here");
    }

    public String queryKey(String value) {
        Table table = dynamoDB.getTable(tableName);
        Index index = table.getIndex(VALUE_ATTRIBUTE_NAME);
        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("#v = :value")
                .withNameMap(new NameMap().with("#v", VALUE_ATTRIBUTE_NAME))
                .withValueMap(new ValueMap().withString(":value", value));
        ItemCollection<QueryOutcome> items = index.query(querySpec);
        IteratorSupport<Item, QueryOutcome> itemIterator = items.iterator();
        if (!itemIterator.hasNext()) {
            return null;
        }
        Item item = itemIterator.next();
        //Preconditions.checkState(!itemIterator.hasNext()); // there might be more than one key for this indexed value

        for (Map.Entry<String, Object> entry : item.attributes()) {
            if (entry.getKey().equals(KEY_ATTRIBUTE_NAME)) {
                return String.class.cast(entry.getValue());
            }
            Preconditions.checkState(entry.getKey().equals(VALUE_ATTRIBUTE_NAME));
        }
        throw new IllegalStateException("Shouldn't get here");
    }
}
