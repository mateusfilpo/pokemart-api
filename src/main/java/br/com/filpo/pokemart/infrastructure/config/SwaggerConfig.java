package br.com.filpo.pokemart.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "cookieAuth";

        return new OpenAPI()
            .info(
                new Info()
                    .title("PokéMart API")
                    .version("1.0.0")
                    .description(
                        "API oficial do PokéMart. Controle de catálogo, carrinho e pedidos com segurança via HttpOnly Cookies e banco de dados Neo4j."
                    )
                    .contact(
                        new Contact()
                            .name("Mateus Filpo")
                            .email("mateusfilpo27@gmail.com")
                            .url("https://github.com/mateusfilpo")
                    )
            )
            .addSecurityItem(
                new SecurityRequirement().addList(securitySchemeName)
            )
            .components(
                new Components().addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name("jwt_token")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .description(
                            "Token JWT armazenado em um cookie HttpOnly seguro."
                        )
                )
            );
    }
}
