package org.santel.url.dao;

import org.slf4j.*;

import java.net.*;

public class DynamoMappingDao implements MappingDao {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoMappingDao.class);

    public URL getShortUrl(URL longUrl) {
        throw new IllegalStateException("not implemented yet");
    }

    /** @return True if a short url did not exist and therefore the new entry was added; false otherwise */
    public boolean addUrlEntry(URL shortUrl, URL longUrl) {
        throw new IllegalStateException("not implemented yet");
    }

    /** @return Stored long url for short url, or null if it entry does not exist */
    public URL getLongUrl(URL shortUrl) {
        throw new IllegalStateException("not implemented yet");
    }
}
