package com.tui.proof;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Component
public class SwaggerConfiguration
{
    @Bean
    public OpenAPI configureOpenApi()
    {
        Info info = new Info().title("Pilotes of the great Miquel Montoro").version("1.0");
        return new OpenAPI().info(info);
    }
}
