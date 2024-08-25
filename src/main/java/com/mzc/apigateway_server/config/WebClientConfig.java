package com.mzc.apigateway_server.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebClientConfig {

    @Bean
    public HttpMessageConverter<Object> createMappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        return converter;
    }

    @Bean
    public WebClient webClient() {
        // ObjectMapper에 HATEOAS 지원을 추가
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jackson2HalModule());

        // WebClient에 필요한 코드 설정
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(clientCodecConfigurer -> {
                // application/hal+json을 지원하는 디코더 및 인코더 설정
                clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(
                    new Jackson2JsonDecoder(mapper, MediaType.parseMediaType("application/hal+json")));
                clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(
                    new Jackson2JsonEncoder(mapper, MediaType.parseMediaType("application/hal+json")));

                // 일반적인 application/json을 처리하는 디코더 및 인코더 설정
                clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(
                    new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
                clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(
                    new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
            })
            .build();

        return WebClient.builder()
            .exchangeStrategies(strategies)
            .build();
        }
        
}

