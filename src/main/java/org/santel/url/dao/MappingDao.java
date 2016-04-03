package org.santel.url.dao;

import com.google.common.collect.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

import java.net.*;

@Component
public class MappingDao {
    private static final Logger LOG = LoggerFactory.getLogger(MappingDao.class);

    private final BiMap<URL, URL> shortLongUrlMap = HashBiMap.create(4096); // arbitrary initial size for demo purposes

    public URL getShortUrl(URL longUrl) {
        LOG.debug("Querying short url from store which has {} entries.", shortLongUrlMap.size());
        return shortLongUrlMap.inverse().get(longUrl);
    }

    /** @return True if a short url did not exist and therefore the new entry was added; false otherwise */
    public boolean addUrlEntry(URL shortUrl, URL longUrl) {
        if (!shortLongUrlMap.containsKey(shortUrl)) {
            shortLongUrlMap.put(shortUrl, longUrl);
            LOG.info("Store has now {} entries.", shortLongUrlMap.size());
            return true;
        }
        return false;
    }

    /** @return Stored long url for short url, or null if it entry does not exist */
    public URL getLongUrl(URL shortUrl) {
        LOG.debug("Querying long url from store which has {} entries.", shortLongUrlMap.size());
        return shortLongUrlMap.get(shortUrl);
    }
}
