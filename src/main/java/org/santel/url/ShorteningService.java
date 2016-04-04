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
            return new InMemoryMappingDao();
        }
    }
}
