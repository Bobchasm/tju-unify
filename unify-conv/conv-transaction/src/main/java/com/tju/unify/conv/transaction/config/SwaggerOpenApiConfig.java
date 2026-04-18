package com.tju.unify.conv.transaction.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

@SpringBootConfiguration
public class SwaggerOpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        Contact contact = new Contact()
                .name("tju-unify")
                .extensions(new HashMap<String, Object>());

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2" +
                        "." +
                        "...0.html")
                .identifier("Apache-2.0")
                .extensions(new HashMap<String, Object>());

        Info info = new Info()
                .title("TJU-UNIFY TRANSACTION APIS")      // 接口文档标题
                .description("TJU-UNIFY TRANSACTION API FOR DEVELOPERS")     // 接口文档描述
                .version("0.0.1")
                .license(license)
                .contact(contact);

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("token")
                        )
                ).addSecurityItem(
                        new SecurityRequirement().addList("token")
                )
                .openapi("3.0.1")
                .info(info);
    }
}