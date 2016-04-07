package org.santel.url;

import org.springframework.boot.builder.*;
import org.springframework.boot.context.web.*;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		System.setProperty("spring.profiles.active", "production");
		return application.sources(ShorteningService.class, ShorteningService.ProductionShorteningConfiguration.class);
	}

}
