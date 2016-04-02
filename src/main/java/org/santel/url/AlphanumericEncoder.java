package org.santel.url;

import com.google.common.collect.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.stream.*;

@Component
public class AlphanumericEncoder {
    private static final List<String> ALPHANUMERIC_DIGITS = getAlphanumericDigitsAsStrings();
    private static final int BASE_ALPHANUMERIC = ALPHANUMERIC_DIGITS.size();

    private static List<String> getAlphanumericDigitsAsStrings() {
        ImmutableList.Builder<String> digitListBuilder = ImmutableList.builder();
        addRangeToListBuilder(digitListBuilder, '0', '9');
        addRangeToListBuilder(digitListBuilder, 'a', 'z');
        addRangeToListBuilder(digitListBuilder, 'A', 'Z');
        return digitListBuilder.build();
    }
    private static void addRangeToListBuilder(ImmutableList.Builder<String> digitListBuilder, char from, char to) {
        IntStream.rangeClosed(from, to)
                .mapToObj(c -> Character.toString((char) c))
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
        StringBuilder codeBuilder = new StringBuilder();
        while (randomValue > 0) {
            int alphanumericPoint = randomValue % BASE_ALPHANUMERIC;
            codeBuilder.append(ALPHANUMERIC_DIGITS.get(alphanumericPoint));
            randomValue /= BASE_ALPHANUMERIC;
        }

        return codeBuilder.toString();
    }
}
