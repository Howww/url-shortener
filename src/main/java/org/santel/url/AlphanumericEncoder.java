package org.santel.url;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

@Component
public class AlphanumericEncoder {
    @Autowired
    private Random random;

    public String encodeAlphanumeric(String original) {
        return "abcd";
    }
}
