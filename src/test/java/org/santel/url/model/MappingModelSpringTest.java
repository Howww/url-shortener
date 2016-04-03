package org.santel.url.model;

import org.santel.url.*;
import org.santel.url.model.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.testng.*;
import org.testng.*;
import org.testng.annotations.*;

import java.net.*;
import java.util.regex.*;

@ContextConfiguration(classes = ShorteningService.class)
public class MappingModelSpringTest extends AbstractTestNGSpringContextTests {
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

    @Autowired
    private MappingModel mappingModel;

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
}
