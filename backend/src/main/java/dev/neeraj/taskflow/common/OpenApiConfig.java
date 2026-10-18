package dev.neeraj.taskflow.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI taskFlowOpenApi() {
        return new OpenAPI().info(new Info()
                .title("TaskFlow API")
                .description("REST API for a small team task board")
                .version("v1")
                .contact(new Contact().name("Neeraj Kumar").url("https://github.com/Neeraj95464"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
