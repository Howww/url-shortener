package org.santel.url.model;

import org.testng.*;
import org.testng.annotations.*;

public class AlphanumericEncoderFastTest {
    /** Value is tested by {@link AlphanumericEncoderFastTest#maxIntegerEncodingIsCorrect()} */
    private static final String MAX_INT_CODE = "1BCkl2";

    @DataProvider
    Object[][] alphanumericEncodings() {
        return new Object[][]{
                {0, "0"}, {9, "9"},
                {10, "a"}, {35, "z"},
                {36, "A"}, {61, "Z"},
                {AlphanumericEncoder.BASE_ALPHANUMERIC, "01"},
                {Integer.MAX_VALUE, MAX_INT_CODE},
        };
    }

    @Test(dataProvider = "alphanumericEncodings")
    void encodingProvidesAlphanumericStrings(int value, String expectedCode) {
        AlphanumericEncoder alphanumericEncoder = new AlphanumericEncoder();
        String actualCode = alphanumericEncoder.encodeAlphanumeric(value);
        AssertJUnit.assertEquals("Code for value " + value + " should be as expected", expectedCode, actualCode);
    }

    @Test
    void maxIntegerEncodingIsCorrect() {
        int maxInt = Integer.MAX_VALUE;
        for (char c : MAX_INT_CODE.toCharArray()) {
            int digitIndex = maxInt % AlphanumericEncoder.BASE_ALPHANUMERIC;
            AssertJUnit.assertEquals("Unexpected alphanumeric digit for max integer encoding", AlphanumericEncoder.ALPHANUMERIC_DIGITS.get(digitIndex).charValue(), c);
            maxInt /= AlphanumericEncoder.BASE_ALPHANUMERIC;
        }
        AssertJUnit.assertEquals("There is more of the max integer than was supposed to be encoded", 0, maxInt);
    }

}
