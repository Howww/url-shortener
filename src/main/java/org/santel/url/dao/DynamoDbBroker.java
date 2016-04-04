package org.santel.url.dao;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.internal.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.annotations.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import org.slf4j.*;

import java.util.*;

public class DynamoDbBroker {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoDbMappingDao.class);

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

        CreateTableRequest createTableRequest = new CreateTableRequest(
                ImmutableList.of(new AttributeDefinition("shortUrl", ScalarAttributeType.S), new AttributeDefinition("longUrl", ScalarAttributeType.S)),
                tableName,
                ImmutableList.of(new KeySchemaElement("shortUrl", KeyType.HASH)),
                provisionedThroughput);

        createTableRequest.setGlobalSecondaryIndexes(ImmutableList.of(globalSecondaryIndex));
        return createTableRequest;
    }

    private GlobalSecondaryIndex getGlobalSecondaryIndex(ProvisionedThroughput provisionedThroughput) {
        GlobalSecondaryIndex globalSecondaryIndex = new GlobalSecondaryIndex();
        globalSecondaryIndex.setIndexName("longUrl");
        globalSecondaryIndex.setKeySchema(ImmutableList.of(new KeySchemaElement("longUrl", KeyType.HASH)));
        globalSecondaryIndex.setProvisionedThroughput(provisionedThroughput);
        Projection projection = new Projection();
        projection.setProjectionType(ProjectionType.ALL);
        globalSecondaryIndex.setProjection(projection);
        return globalSecondaryIndex;
    }

    @VisibleForTesting
    boolean deleteTable() {
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

    public String query(String keyAttributeName, Object key, String valueAttributeName) {
        Table table = dynamoDB.getTable(tableName);
        ItemCollection<QueryOutcome> items = table.query(new KeyAttribute(keyAttributeName, key));
        IteratorSupport<Item, QueryOutcome> itemIterator = items.iterator();
        if (!itemIterator.hasNext()) {
            return null;
        }
        Item item = itemIterator.next();
        Preconditions.checkState(!itemIterator.hasNext());

        Iterator<Map.Entry<String, Object>> attributeIterator = item.attributes().iterator();
        Preconditions.checkState(attributeIterator.hasNext());

        Map.Entry<String, Object> valueEntry = attributeIterator.next();
        Preconditions.checkState(!attributeIterator.hasNext());
        Preconditions.checkState(valueEntry.getKey().equals(valueAttributeName));
        return String.class.cast(valueEntry.getValue());
    }
}
