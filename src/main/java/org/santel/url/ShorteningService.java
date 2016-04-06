package org.santel.url;

import org.santel.url.dao.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.builder.*;
import org.springframework.context.annotation.*;

import java.util.*;

@SpringBootApplication
public class ShorteningService {

    @Bean
    public Random getRandomBean() {
        return new Random();
    }

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "production");
        System.setProperty("server.port", "80");
        new SpringApplicationBuilder()
                .bannerMode(Banner.Mode.OFF)
                .sources(ShorteningService.class)
                .run(args);
    }

    @Configuration
    @Profile("production")
    static class ProductionShorteningConfiguration {
        @Bean
        public MappingDao getMappingDaoBean() {
            String useInMemoryStore = System.getProperty("santel.url.inmemory"); //TODO add to Spring property system?
            return useInMemoryStore == null?
                    new DynamoDbMappingDao(System.getProperty("santel.dynamodb.url", "http://localhost:8000")) : //TODO add to Spring property system?
                    new InMemoryMappingDao();
        }
    }
}
