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

    public URL shortenUrl(URL url) {
        Preconditions.checkNotNull(url);

        // encode url into an alphanumeric-base string
        String code = alphanumericEncoder.encodeAlphanumeric(url.toString());
        String hostName = getLocalHostName();
        try {
            return new URL(url.getProtocol(), hostName, "/" + code);
        } catch (MalformedURLException e) {
            LOG.error("Exception trying to form URL from protocol {}, local host name {}, and code {}: {}", url.getProtocol(), hostName, code, e);
            throw new RuntimeException("Can't form short URL from original url, local host name, and code", e);
        }
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
