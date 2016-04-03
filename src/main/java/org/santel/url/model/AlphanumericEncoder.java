package org.santel.url.model;

import com.google.common.annotations.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.stream.*;

@Component
public class AlphanumericEncoder {
    @VisibleForTesting
    static final List<Character> ALPHANUMERIC_DIGITS = getAlphanumericDigitsAsStrings();
    @VisibleForTesting
    static final int BASE_ALPHANUMERIC = ALPHANUMERIC_DIGITS.size();

    private static List<Character> getAlphanumericDigitsAsStrings() {
        ImmutableList.Builder<Character> digitListBuilder = ImmutableList.builder();
        addRangeToListBuilder(digitListBuilder, '0', '9');
        addRangeToListBuilder(digitListBuilder, 'a', 'z');
        addRangeToListBuilder(digitListBuilder, 'A', 'Z');
        return digitListBuilder.build();
    }
    private static void addRangeToListBuilder(ImmutableList.Builder<Character> digitListBuilder, char from, char to) {
        IntStream.rangeClosed(from, to)
                .mapToObj(c -> (char) c)
                .forEach(digitListBuilder::add);
    }

    @Autowired
    private Random random;

    /**
     * TODO: ensure code is unique given collection of used codes
     * @param original Might be used to help encoding, although it's not used right now
     * @return a unique alphanumeric code for the provided string
     */
    public String encodeAlphanumeric(String original) {
        int randomValue = Math.abs(random.nextInt());
        return encodeAlphanumeric(randomValue);
    }

    @VisibleForTesting
    String encodeAlphanumeric(int unsignedValue) {
        Preconditions.checkArgument(unsignedValue >= 0);

        if (unsignedValue == 0) {
            return ALPHANUMERIC_DIGITS.get(0).toString();
        }

        StringBuilder codeBuilder = new StringBuilder();
        while (unsignedValue > 0) {
            int alphanumericPoint = unsignedValue % BASE_ALPHANUMERIC;
            codeBuilder.append(ALPHANUMERIC_DIGITS.get(alphanumericPoint));
            unsignedValue /= BASE_ALPHANUMERIC;
        }

        return codeBuilder.toString();
    }
}
