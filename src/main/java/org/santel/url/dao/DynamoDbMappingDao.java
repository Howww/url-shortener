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
//        return getUrl("longUrl", longUrl, "shortUrl");
        throw new IllegalStateException("not implemented yet");
    }

    /** @return Stored long url for short url, or null if entry does not exist */
    public URL getLongUrl(URL shortUrl) {
        String longUrlAsString = dynamoDbBroker.queryValue(shortUrl.toString());
        URL longUrl = null;
        if (longUrlAsString != null) {
            try {
                longUrl = new URL(longUrlAsString);
            } catch (MalformedURLException e) {
                Exceptions.logAndThrow(LOG, String.format("Unable to form short url from stored long url %s", longUrlAsString));
            }
        }
        return longUrl;
    }

    /** @return True if a short url did not exist and therefore the new entry was added; false otherwise */
    public boolean addUrlEntry(URL shortUrl, URL longUrl) {
        return dynamoDbBroker.insertEntry(shortUrl.toString(), longUrl.toString());
    }
}
