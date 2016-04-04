package org.santel.url.dao;

import org.testng.*;
import org.testng.annotations.*;

@Test
public class DynamoDbBrokerTest {

    /** Test will pass only if a DynamoDB instance is located in the given or default url */
    @Test
    void canCreateAndDestroyTestTable() {
        String dynamoDbUrl = System.getProperty("dynamodb.url", "http://localhost:8000"); //TODO add to Spring property system?
        DynamoDbBroker dynamoDbBroker = new DynamoDbBroker(dynamoDbUrl, "TestTable");
        dynamoDbBroker.deleteTable(); // make sure test table creation can succeed

        AssertJUnit.assertTrue("Table creation should succeed", dynamoDbBroker.createTable());
        AssertJUnit.assertTrue("Table deletion should succeed", dynamoDbBroker.deleteTable());
    }

}
