package com.tju.unify.conv.errand.config;

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
                .extensions(new HashMap<>());

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                .identifier("Apache-2.0")
                .extensions(new HashMap<>());

        Info info = new Info()
                .title("TJU-UNIFY ERRAND APIS")
                .description("校园跑腿模块。调试时在 Authorize 中填写登录用户名，请求头 username 与 7070 网关注入一致。")
                .version("0.0.1")
                .license(license)
                .contact(contact);

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("username",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("username")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("username"))
                .openapi("3.0.1")
                .info(info);
    }
}
