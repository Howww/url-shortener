package org.santel.url.model;

import com.google.common.base.*;
import org.santel.exception.*;
import org.santel.net.*;
import org.santel.url.dao.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

import javax.inject.*;
import java.net.*;

@Component
public class MappingModel {
    private static final Logger LOG = LoggerFactory.getLogger(MappingModel.class);
    private static final String SHORT_URL_PROTOCOL = "http"; //TODO make more secure with https

    @Inject
    private MappingDao mappingDao;
    @Inject
    private AlphanumericEncoder alphanumericEncoder;

    public URL shortenUrl(URL longUrl) {
        Preconditions.checkNotNull(longUrl);

        // first, check store for existence of entry for long url
        URL shortUrl = mappingDao.getShortUrl(longUrl);
        if (shortUrl != null) {
            LOG.info("Long url {} has already been shortened to {}", longUrl, shortUrl);
            return shortUrl;
        }

        // create short url, trying multiple times in case of collisions
        int tries = 100;
        while (tries-- > 0) {
            // encode long url as an alphanumeric-base string
            String shortCode = alphanumericEncoder.encodeAlphanumeric(longUrl.toString());
            shortUrl = getShortUrlFromCode(shortCode);

            // check for entry in data store; store and return it if it doesn't exist (i.e. no collision)
            if (mappingDao.addUrlEntry(shortUrl, longUrl)) {
                LOG.info("Shortened {} to {}", longUrl, shortUrl);
                return shortUrl;
            }

            // long URL might have been added by another thread or instance in the meantime
            URL anotherShortUrl = mappingDao.getShortUrl(longUrl);
            if (anotherShortUrl != null) {
                LOG.info("Long url {} was shortened in the meantime to {}", longUrl, anotherShortUrl);
                return anotherShortUrl;
            }

            URL storedLongUrl = mappingDao.getLongUrl(shortUrl);
            Preconditions.checkState(storedLongUrl != null && !storedLongUrl.equals(longUrl)); // it must be a collision
        }

        String errorMessage = "Exhausted tries to generate a non-colliding short url";
        LOG.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }

    public URL expandUrl(String shortCode) {
        URL shortUrl = getShortUrlFromCode(shortCode);
        URL longUrl = mappingDao.getLongUrl(shortUrl);
        if (longUrl == null) {
            Exceptions.logAndThrow(LOG, String.format("Short url %s not found in store", shortUrl));
        }
        LOG.info("Expanded {} to {}", shortUrl, longUrl);
        return longUrl;
    }

    private URL getShortUrlFromCode(String shortCode) {
        URL shortUrl;
        String hostName = Network.getLocalHostName();
        try {
            shortUrl = new URL(SHORT_URL_PROTOCOL, hostName, "/" + shortCode);
        } catch (MalformedURLException e) {
            LOG.error("Exception trying to form URL from protocol {}, local host name {}, and code {}: {}", SHORT_URL_PROTOCOL, hostName, shortCode, e);
            throw new RuntimeException("Can't form short URL from original url, local host name, and code", e);
        }
        return shortUrl;
    }
}
