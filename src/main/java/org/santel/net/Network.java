package org.santel.net;

import com.amazonaws.regions.*;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.*;

import java.net.*;
import java.util.*;
import java.util.stream.*;

public class Network {
    private static final Logger LOG = LoggerFactory.getLogger(Network.class);

    public static String getLocalHostName() {
        String localHostName = getAwsPublicDnsName();

        if (localHostName == null) {
            try {
                localHostName = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                LOG.error("Exception identifying local host name", e);
                throw new RuntimeException("Can't identify local host name", e);
            }
        }
        return localHostName;
    }

    private static String getAwsPublicDnsName() {
        String localHostName = null;
        try {
            AmazonEC2Client amazonEC2Client = selectEc2Client("http://localhost");
            DescribeInstancesRequest request = new DescribeInstancesRequest();
            DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances(request);
            List<Reservation> reservations = describeInstancesResult.getReservations();
            Instance instance = reservations.stream()
                    .flatMap(r -> r.getInstances().stream())
                    .collect(Collectors.toList()).stream()
                    .findFirst().orElse(null);
            if (instance != null) {
                localHostName = instance.getPublicDnsName();
            }
            LOG.info("Found AWS public DNS instance name {}", localHostName);
            return localHostName;
        } catch (Exception e) {
            LOG.warn("Exception while trying to get AWS public DNS instance name");
            return null;
        }
    }

    private static AmazonEC2Client selectEc2Client(String url) {
        return System.getProperty("santel.url.local") == null?
                new AmazonEC2Client().withRegion(Regions.US_WEST_2) :
                new AmazonEC2Client().withEndpoint(url);
    }

}
