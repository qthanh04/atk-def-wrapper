package com.tool.atkdefbackend.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * HTTP Client Configuration for WebClient
 * Provides non-blocking HTTP communication with Python Core API
 * 
 * Based on NewTech.md Section 9: WebClient for Non-Blocking HTTP Communication
 */
@Configuration
public class HttpClientConfig {

    @Value("${python.server-url:http://localhost:8000}")
    private String pythonServerUrl;

    /**
     * Configure HttpClient with connection and timeout settings
     */
    @Bean
    public HttpClient httpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));
    }

    /**
     * Create WebClient with configured HttpClient
     * Used for non-blocking HTTP communication with Python Core API
     */
    @Bean
    public WebClient webClient(HttpClient httpClient) {
        return WebClient.builder()
                .baseUrl(pythonServerUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
