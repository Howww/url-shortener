package org.santel.url;

import com.google.common.base.*;
import com.google.common.collect.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.net.*;

@Component
public class MappingModel {
    private static final Logger LOG = LoggerFactory.getLogger(MappingModel.class);

    private final BiMap<URL, URL> shortLongUrlMap = HashBiMap.create(4096); // arbitrary initial expected size for demo purposes
    @Autowired
    private AlphanumericEncoder alphanumericEncoder;

    public URL shortenUrl(URL longUrl) {
        Preconditions.checkNotNull(longUrl);

        // first, check store for existence of entry for long url
        URL shortUrl = shortLongUrlMap.inverse().get(longUrl);
        if (shortUrl != null) {
            LOG.info("Long url {} has already been shortened to {}. Store still has {} entries.", longUrl, shortUrl, shortLongUrlMap.size());
            return shortUrl;
        }

        // create short url, trying multiple times in case of collisions
        int tries = 10;
        while (tries-- > 0) {
            // encode long url as an alphanumeric-base string
            String code = alphanumericEncoder.encodeAlphanumeric(longUrl.toString());
            String hostName = getLocalHostName();
            try {
                shortUrl = new URL(longUrl.getProtocol(), hostName, "/" + code);
            } catch (MalformedURLException e) {
                LOG.error("Exception trying to form URL from protocol {}, local host name {}, and code {}: {}", longUrl.getProtocol(), hostName, code, e);
                throw new RuntimeException("Can't form short URL from original url, local host name, and code", e);
            }

            // check for entry in data store; store and return it if it doesn't exist (i.e. no collision)
            if (!shortLongUrlMap.containsKey(shortUrl)) {
                shortLongUrlMap.put(shortUrl, longUrl);
                LOG.info("Shortened {} to {}. Store has now {} entries.", longUrl, shortUrl, shortLongUrlMap.size());
                return shortUrl;
            }
            Preconditions.checkState(!shortLongUrlMap.get(shortUrl).equals(longUrl)); // it must be a collision
        }

        String errorMessage = "Exhausted tries to generate a non-colliding short url";
        LOG.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }

    private String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error("Exception identifying local host name", e);
            throw new RuntimeException("Can't identify local host name", e);
        }
    }
}
