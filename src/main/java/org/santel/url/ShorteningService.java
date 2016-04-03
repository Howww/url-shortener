package org.santel.url;

import org.santel.url.dao.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;

import java.util.*;

@SpringBootApplication
public class ShorteningService {

    @Bean
    public Random getRandomBean() {
        return new Random();
    }

    @Bean
    public MappingDao getMappingDaoBean() {
        return new InMemoryMappingDao();
    }

    public static void main(String[] args) {
        SpringApplication.run(ShorteningService.class, args);
    }
}
