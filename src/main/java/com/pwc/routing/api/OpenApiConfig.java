package com.pwc.routing.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("Country land-routing service")
                .description("Calculates land routes between countries from the mledoze/countries "
                        + "dataset. Countries are identified by their cca3 code — "
                        + "GET /countries lists the current dataset.")
                .version("v1"));
    }
}
