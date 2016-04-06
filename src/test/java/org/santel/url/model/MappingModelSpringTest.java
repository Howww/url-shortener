package org.santel.url.model;

import com.google.common.base.*;
import org.santel.url.*;
import org.santel.url.dao.*;
import org.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.*;
import org.springframework.test.context.testng.*;
import org.testng.*;
import org.testng.annotations.*;

import javax.inject.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.regex.*;

@ContextConfiguration(classes = ShorteningService.class)
public class MappingModelSpringTest extends AbstractTestNGSpringContextTests {
    private static final Logger LOG = LoggerFactory.getLogger(MappingModelSpringTest.class);
    private static final URL LONG_URL;
    private static final String SHORT_URL_PREFIX = "http://";
    private static final Pattern SHORT_URL_PATTERN;
    static {
        try {
            LONG_URL = new URL("https://some.domain.of.mine/path?param1=foo&param2=bar#section");
            SHORT_URL_PATTERN = Pattern.compile(SHORT_URL_PREFIX + InetAddress.getLocalHost().getHostName() + "/[0-9a-z-A-Z]{1," + AlphanumericEncoder.BASE_ALPHANUMERIC + "}");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize static fields", e);
        }
    }
    private static DynamoDbBroker DYNAMO_DB_BROKER = new DynamoDbBroker(
            System.getProperty("dynamodb.url", "http://localhost:8000"),
            "TestTableMappingModelSpringTest");

    private static AtomicBoolean threadWaitSignal;
    private static CountDownLatch countDownLatch1;
    private static CountDownLatch countDownLatch2;
    private static AtomicReference<URL> threadShortUrl;

    @Configuration
    static class TestShorteningConfiguration {
        @Bean
        public MappingDao getMappingDaoBean() {
            return new DynamoDbMappingDao(DYNAMO_DB_BROKER) {
                @Override
                public boolean addUrlEntry(URL shortUrl, URL longUrl) { // to be called by model's shortenUrl
                    if (threadWaitSignal.get()) {
                        try {
                            countDownLatch1.await(); // wait in the middle of shortening to reproduce race condition
                        } catch (InterruptedException e) {
                            throw new RuntimeException("DynamoDB mock was interrupted!", e);
                        }
                    }
                    try {
                        return super.addUrlEntry(shortUrl, longUrl); // call the real method
                    } finally {
                        countDownLatch2.countDown(); // let main thread know that we are done
                    }
                }
            };
        }
    }

    @Inject
    private MappingModel mappingModel;

    @BeforeMethod
    void beforeMappingModelSpringTestMethod() {
        threadWaitSignal = new AtomicBoolean(false);
        countDownLatch1 = new CountDownLatch(1);
        countDownLatch2 = new CountDownLatch(1);
        threadShortUrl = new AtomicReference<>(null);
        DYNAMO_DB_BROKER.deleteTable();
        Preconditions.checkState(DYNAMO_DB_BROKER.createTable());
    }
    @AfterMethod
    void afterMappingModelSpringTestMethod() {
        Preconditions.checkState(DYNAMO_DB_BROKER.deleteTable());
    }

    @Test
    void shortUrlHasExpectedForm() {
        URL shortUrl = mappingModel.shortenUrl(LONG_URL);
        AssertJUnit.assertTrue(String.format("Short url %s for %s should have expected format %s", shortUrl, LONG_URL, SHORT_URL_PATTERN),
                SHORT_URL_PATTERN.matcher(shortUrl.toString()).matches());
    }

    @Test
    void longUrlAlwaysGetsSameShortUrl() {
        URL firstShortUrl = mappingModel.shortenUrl(LONG_URL);
        for (int i = 0; i < 10; ++i) {
            URL nextShortUrl = mappingModel.shortenUrl(LONG_URL);
            AssertJUnit.assertEquals(String.format("Short url %s for long url %s must match first short url %s", nextShortUrl, LONG_URL, firstShortUrl),
                    firstShortUrl, nextShortUrl);
        }
    }

    @Test
    void shortUrlExpansionMatchesOriginalLongUrl() {
        URL shortUrl = mappingModel.shortenUrl(LONG_URL);
        String shortUrlAsString = shortUrl.toString();
        String shortCode = shortUrlAsString.substring(shortUrlAsString.lastIndexOf('/') + 1);
        URL expandedUrl = mappingModel.expandUrl(shortCode);
        AssertJUnit.assertEquals(String.format("Expanded url %s from short url %s should match original long url %s", expandedUrl, shortUrl, LONG_URL),
                LONG_URL, expandedUrl);
    }

    @Test
    void concurrentShorteningCreatesSameShortUrl() throws Exception {
        threadWaitSignal.set(true); // the other thread will wait
        new Thread(() -> {
            threadShortUrl.set(mappingModel.shortenUrl(LONG_URL));
        }).start();
        Thread.sleep(100); // give the other thread time to realize that it must wait before setting threadWaitSignal to false

        threadWaitSignal.set(false); // this thread will not wait
        URL firstShortUrl = mappingModel.shortenUrl(LONG_URL); // create this thread's short url before the other thread
        LOG.info("First short url in main thread: {}", firstShortUrl);

        countDownLatch1.countDown(); // release the other thread so it can create its own short url (after this thread's short url)
        countDownLatch2.await(); // wait for the other thread to finish adding entry
        Thread.sleep(100); // give the other thread time to complete shortenUrl
        LOG.info("Short url from the other thread: {}", threadShortUrl.get());

        AssertJUnit.assertFalse("Shortened url must not be the same in both threads", firstShortUrl.equals(threadShortUrl.get()));

        URL secondShortUrl = mappingModel.shortenUrl(LONG_URL); // shorten the long URL again; result should be that of the other thread
        LOG.info("Second short url in main thread: {}", firstShortUrl);
        AssertJUnit.assertTrue("New short url must is one of the two created in parallel (this thread's first or the other thread's)",
                secondShortUrl.equals(firstShortUrl) || secondShortUrl.equals(threadShortUrl.get()));

        for (int i = 0; i < 10; ++i) {
            URL additionalShortUrl = mappingModel.shortenUrl(LONG_URL);
            LOG.info("Additional {}th short url in main thread: {}", i, additionalShortUrl);
            AssertJUnit.assertTrue("From now on, short url should stabilize",
                    additionalShortUrl.equals(firstShortUrl) || additionalShortUrl.equals(threadShortUrl.get()));
        }
    }
}
