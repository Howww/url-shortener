package org.santel.url.dao;

import org.testng.*;
import org.testng.annotations.*;

/** Tests will pass only if a DynamoDB instance is located in the given or default url */
@Test
public class DynamoDbBrokerTest {

    public static final String TEST_KEY = "a key";
    public static final String TEST_VALUE = "some value";
    private DynamoDbBroker dynamoDbBroker;

    @BeforeMethod
    void beforeDynamoDbBrokerMethod() {
        String dynamoDbUrl = System.getProperty("dynamodb.url", "http://localhost:8000"); //TODO add to Spring property system?
        dynamoDbBroker = new DynamoDbBroker(dynamoDbUrl, "TestTable");
        dynamoDbBroker.deleteTable(); // make sure test table starts clean
    }

    @AfterMethod
    void afterDynamoDbBrokerMethod() {
        dynamoDbBroker.deleteTable(); // clean up test table after test
    }

    @Test
    void canCreateAndDestroyTestTable() {
        AssertJUnit.assertTrue("Table creation should succeed", dynamoDbBroker.createTable());
        AssertJUnit.assertTrue("Table deletion should succeed", dynamoDbBroker.deleteTable());
    }

    @Test
    void canStoreAndReadUrlEntry() {
        AssertJUnit.assertTrue("Table creation should succeed", dynamoDbBroker.createTable());
        AssertJUnit.assertTrue("First insertion should succeed", dynamoDbBroker.insertEntry(TEST_KEY, TEST_VALUE)); // first insertion succeeds
        AssertJUnit.assertEquals("Value for key should be one we inserted", TEST_VALUE, dynamoDbBroker.queryValue(TEST_KEY));
    }

    @Test
    void doubleInsertionFails() {
        AssertJUnit.assertTrue("Table creation should succeed", dynamoDbBroker.createTable());
        AssertJUnit.assertTrue("First insertion should succeed", dynamoDbBroker.insertEntry(TEST_KEY, TEST_VALUE)); // first insertion succeeds
        AssertJUnit.assertFalse("Repeated insertion should fail", dynamoDbBroker.insertEntry(TEST_KEY, TEST_VALUE)); // second insertion fails
    }
}
