package org.santel.url.dao;

import com.google.common.annotations.*;
import org.santel.exception.*;
import org.slf4j.*;

import java.net.*;

public class DynamoDbMappingDao implements MappingDao {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoDbMappingDao.class);
    private static final String URL_MAP_TABLE_NAME = "UrlMap";

    private final DynamoDbBroker dynamoDbBroker;

    public DynamoDbMappingDao(String dynamoDbUrl) {
        this(new DynamoDbBroker(dynamoDbUrl, URL_MAP_TABLE_NAME));
    }

    @VisibleForTesting
    public DynamoDbMappingDao(DynamoDbBroker dynamoDbBroker) {
        this.dynamoDbBroker = dynamoDbBroker;
        this.dynamoDbBroker.createTable(); // ensure that the table exists
    }

    /** @return Stored short url for long url, or null if entry does not exist */
    public URL getShortUrl(URL longUrl) {
        String shortUrlAsString = dynamoDbBroker.queryKey(longUrl.toString());
        return getUrl(shortUrlAsString);
    }

    /** @return Stored long url for short url, or null if entry does not exist */
    public URL getLongUrl(URL shortUrl) {
        String longUrlAsString = dynamoDbBroker.queryValue(shortUrl.toString());
        return getUrl(longUrlAsString);
    }

    private URL getUrl(String urlAsString) {
        URL url = null;
        if (urlAsString != null) {
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                Exceptions.logAndThrow(LOG, String.format("Unable to form url from %s", urlAsString));
            }
        }
        return url;
    }

    /** @return True if a short url did not exist and therefore the new entry was added; false otherwise */
    public boolean addUrlEntry(URL shortUrl, URL longUrl) {
        return dynamoDbBroker.insertEntry(shortUrl.toString(), longUrl.toString());
    }
}
