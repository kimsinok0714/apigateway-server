package com.mzc.apigateway_server.filter;

import java.net.URI;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.mzc.apigateway_server.dto.DepartmentDto;
import com.mzc.apigateway_server.dto.ItemDto;

import reactor.core.publisher.Mono;

/*
 * Spring Cloud Gateway에서 필터를 사용해 서비스 요청을 조합할 수 있습니다.
 * GlobalFilter 인터페이스를 구현하여 모든 요청에 대해 필터를 적용할 수 있습니다.
 * Ordered 인터페이스를 구현하여 필터의 실행 순서를 지정합니다.
 */

//@Component
public class AggregationFilter implements GlobalFilter, Ordered {

    // 비동기 방식으로 HTTP 요청을 보낼 수 있는 스프링의 HTTP 클라이언트입니다.
    private final WebClient webClient;


    public AggregationFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }


    // filter 메소드는 모든 HTTP 요청에 대해 호출됩니다.

    /*
     * ServerWebExchange : HTTP 요청 및 응답의 모든 정보를 캡슐화하는 객체입니다.
     * GatewayFilterChain : 다음 필터로 요청을 전달하는 역할을 합니다.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        
        URI itemServiceUri = URI.create("http://localhost:8070/api/v1/items/P001");
        URI departmentServiceUri = URI.create("http://localhost:8070/api/v1/departments/1");

        /*
         * webClient.get() : GET 방식의 HTTP 요청을 생성합니다.
         * uri(service1Uri):  GET 요청을 보낼 대상 URI를 지정하는 부분입니다.
         * retrieve() : 요청을 실행하고 서버로 부터 응답을 가져오는 작업을 시작하는 메소드입니다.
         * bodyToMono() : 응답의 본문을 비동기적으로 처리합니다.
         */

        Mono<EntityModel<DepartmentDto>> response1 = webClient.get()
            .uri(departmentServiceUri)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<EntityModel<DepartmentDto>>() {});


        Mono<EntityModel<ItemDto>> response2 = webClient.get()
            .uri(itemServiceUri)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<EntityModel<ItemDto>>() {});


        /*
         * Mono.zip() : 여러 Mono를 결합하여 모든 Modno가 완료될 때까지 기다렸다가 결과를 조합합니다.
         * flatMap() :  두 서비스의 응답을 조합하여 최종 응답 문자열을 만듭니다. 
         * exchange.getResponse() : 조합된 응답을 클라이언트로 전달합니다.
         * Mono.just() : 데이터 버퍼를 사용하여 응답을 기록합니다.
         */
        return Mono.zip(response1, response2)
            .flatMap(tuple -> {
                String combinedResponse = "Service 1 Response: " + tuple.getT1() + " | Service 2 Response: " + tuple.getT2();
                exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(combinedResponse.getBytes())));
            });
    }


    @Override
    public int getOrder() {
        return -1;
    }


}
