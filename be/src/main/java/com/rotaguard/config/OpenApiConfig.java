/* Nhom I */
package com.rotaguard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI rotaGuardOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("RotaGuard API")
                .version("v1")
                .description("Rota Guard shift fatigue analysis API."));
  }
}
