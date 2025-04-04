package com.bayfi.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    // Tatum WebClient
    @Bean
    @Qualifier("tatumWebClient")
    public WebClient tatumWebClient(@Value("${tatum.api.key}") String tatumApiKey) {
        return WebClient.builder()
                .baseUrl("https://api.tatum.io/v3")
                .defaultHeader("x-api-key", tatumApiKey)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5s connect timeout
                                .responseTimeout(Duration.ofSeconds(10)) // 10s response timeout
                ))
                .build();
    }


}