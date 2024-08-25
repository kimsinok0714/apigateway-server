package com.mzc.apigateway_server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@SpringBootTest
public class WebClientTest {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Test
    public void testWebClientResponse() {

        WebClient webClient = webClientBuilder.build();

        Mono<String> response = webClient.get()
            .uri("http://127.0.0.1:8070/api/v1/aggregate/1/P005")
            .retrieve()
            .bodyToMono(String.class);

        response.subscribe(System.out::println);

       
    }
}
