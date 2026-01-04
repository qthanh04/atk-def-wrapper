package com.tool.atkdefbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 * 
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🛡️ ATK-DEF Backend API")
                        .version("1.0.0")
                        .description("""
                                ## Attack-Defense CTF Platform Backend

                                API Gateway cho hệ thống **Attack-Defense CTF Platform**.

                                ### Tính năng chính:
                                - 🔐 **Authentication & Authorization** - JWT-based security
                                - 👥 **Team Management** - CRUD với auto-registration
                                - 📤 **File Upload** - Checker scripts & VulnBox
                                - 🎮 **Game Control Proxy** - Forward requests tới Python Game Server
                                - 📊 **Scoreboard Proxy** - Real-time scoreboard

                                ### Phân quyền (Roles):
                                - `ADMIN` - Full access
                                - `TEACHER` - Read + limited write
                                - `TEAM` - Submit flags, view own data
                                - `PUBLIC` - Scoreboard, current tick

                                ### Proxy APIs:
                                Tất cả `/api/proxy/*` endpoints forward requests tới Python Game Core Engine.
                                """)
                        .contact(new Contact()
                                .name("AnD Platform Team")
                                .email("support@andplatform.io")
                                .url("https://github.com/qthanh04/atk-def-backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development"),
                        new Server().url("https://api.andplatform.io").description("Production")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token (without 'Bearer ' prefix)")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .tags(List.of(
                        new Tag().name("Auth").description("🔐 Authentication & Registration"),
                        new Tag().name("Teams").description("👥 Team Management"),
                        new Tag().name("Upload").description("📤 File Upload (Checker & VulnBox)"),
                        new Tag().name("Game Proxy").description("🎮 Game Control (Proxy to Python)"),
                        new Tag().name("Scoreboard Proxy").description("📊 Scoreboard (Proxy to Python)"),
                        new Tag().name("Submission Proxy").description("🚩 Flag Submission (Proxy to Python)"),
                        new Tag().name("Flag Proxy").description("🏴 Flag Management (Proxy to Python)"),
                        new Tag().name("Tick Proxy").description("⏱️ Tick Management (Proxy to Python)"),
                        new Tag().name("Vulnbox Proxy").description("📦 VulnBox Management (Proxy to Python)"),
                        new Tag().name("Checker Proxy").description("🔍 Checker Management (Proxy to Python)"),
                        new Tag().name("Test").description("🧪 Test Endpoints")));
    }
}
