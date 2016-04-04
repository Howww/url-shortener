package org.santel.url.dao;

import java.net.*;

public interface MappingDao {
    /** @return Stored short url for long url, or null if entry does not exist */
    URL getShortUrl(URL longUrl);

    /** @return Stored long url for short url, or null if entry does not exist */
    URL getLongUrl(URL shortUrl);

    /** @return True if a short url did not exist and therefore the new entry was added; false otherwise */
    boolean addUrlEntry(URL shortUrl, URL longUrl);
}
