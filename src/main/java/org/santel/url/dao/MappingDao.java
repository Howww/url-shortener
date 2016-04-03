package org.santel.url.dao;

import java.net.*;

public interface MappingDao {
    URL getShortUrl(URL longUrl);

    boolean addUrlEntry(URL shortUrl, URL longUrl);

    URL getLongUrl(URL shortUrl);
}
