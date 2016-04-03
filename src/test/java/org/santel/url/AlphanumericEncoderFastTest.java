package org.santel.url;

import org.testng.*;
import org.testng.annotations.*;

public class AlphanumericEncoderFastTest {
    @DataProvider
    Object[][] alphanumericEncodings() {
        return new Object[][]{
                {0, "0"}, {9, "9"},
                {10, "a"}, {35, "z"},
                {36, "A"}, {61, "Z"},
                {AlphanumericEncoder.BASE_ALPHANUMERIC, "01"},
                {Integer.MAX_VALUE, "1BCkl2"},
        };
    }

    @Test(dataProvider = "alphanumericEncodings")
    void encodingProvidesAlphanumericStrings(int value, String expectedCode) {
        AlphanumericEncoder alphanumericEncoder = new AlphanumericEncoder();
        String actualCode = alphanumericEncoder.encodeAlphanumeric(value);
        AssertJUnit.assertEquals("Code for value " + value + " should be as expected", expectedCode, actualCode);
    }
}
