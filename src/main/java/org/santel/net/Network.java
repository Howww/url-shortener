package org.santel.net;

import org.slf4j.*;

import java.net.*;

public class Network {
    private static final Logger LOG = LoggerFactory.getLogger(Network.class);

    public static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error("Exception identifying local host name", e);
            throw new RuntimeException("Can't identify local host name", e);
        }
    }
}
