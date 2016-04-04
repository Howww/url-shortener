package org.santel.url.dao;

import org.santel.exception.*;
import org.slf4j.*;

import java.net.*;

public class DynamoDbMappingDao implements MappingDao {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoDbMappingDao.class);
    private static final String URL_MAP_TABLE_NAME = "UrlMap";

    private final DynamoDbBroker dynamoDbBroker;

    public DynamoDbMappingDao(String dynamoDbUrl) {
        this.dynamoDbBroker = new DynamoDbBroker(dynamoDbUrl, URL_MAP_TABLE_NAME);
        this.dynamoDbBroker.createTable(); // ensure that the table exists
    }

    /** @return Stored short url for long url, or null if entry does not exist */
    public URL getShortUrl(URL longUrl) {
        return getUrl("longUrl", longUrl, "shortUrl");
    }

    /** @return Stored long url for short url, or null if entry does not exist */
    public URL getLongUrl(URL shortUrl) {
        return getUrl("shortUrl", shortUrl, "longUrl");
    }

    private URL getUrl(String keyAttributeName, URL keyUrl, String valueAttributeName) {
        String storedUrlAsString = dynamoDbBroker.query(keyAttributeName, keyUrl.toString(), valueAttributeName);
        URL storedUrl = null;
        if (storedUrlAsString != null) {
            try {
                storedUrl = new URL(storedUrlAsString);
            } catch (MalformedURLException e) {
                Exceptions.logAndThrow(LOG, String.format("Unable to form %s from stored %s value %s", keyAttributeName, valueAttributeName, storedUrlAsString));
            }
        }
        return storedUrl;
    }

    /** @return True if a short url did not exist and therefore the new entry was added; false otherwise */
    public boolean addUrlEntry(URL shortUrl, URL longUrl) {
        throw new IllegalStateException("not implemented yet");
    }
}
