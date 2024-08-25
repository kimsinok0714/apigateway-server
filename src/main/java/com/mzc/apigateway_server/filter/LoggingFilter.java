package com.mzc.apigateway_server.filter;


import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import reactor.core.scheduler.Schedulers;


@Slf4j
@Component
public class LoggingFilter implements WebFilter {   // Spring Framework에서 제공하는 인터셉터 기능을 제공하는 인터페이스 (전처리와 후처리)
    
    /*
     * Mono(Flux) : 비동기적으로 처리 데이터를 처리하는 데이터 스트림 객체
     * ServerWebExchange : Http 요청과 응답에 대한 액세스를 제공
     * WebFilterChain : 다음 필터에 데이터를 전달하기 위해서 사용
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();      
        // Spring WebFlux에서 사용되며, 비동기적으로 데이터를 읽고 쓰는 데 최적화되어 있습니다.
        DataBufferFactory dataBufferFactory = response.bufferFactory();

        // log the request body
        ServerHttpRequest  decoratedRequest = getDecoratedRequest(request);

        // log the response body
        ServerHttpResponseDecorator decoratedResponse = getDecoratedResponse(response, request, dataBufferFactory);

        return chain.filter(exchange.mutate().request(decoratedRequest).response(decoratedResponse).build());
        
    }
    
     // Spring WebFlux에서 HTTP 응답을 데코레이트하여, 응답 본문을 읽고 로깅하거나 수정한 후 클라이언트에게 반환하는 기능을 구현한다.

    private ServerHttpResponseDecorator getDecoratedResponse(ServerHttpResponse response, ServerHttpRequest request, DataBufferFactory dataBufferFactory) {
            return new ServerHttpResponseDecorator(response) {

                // 응답 본문을 쓰기 위해서 사용됩니다.                
                @Override
                public Mono<Void> writeWith(final Publisher<? extends DataBuffer> body) {  // 비동기적으로 데이터를 제공하는 스트림

                    if (body instanceof Flux) {

                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {  // 여러 DataBuffer를 하나로 합침

                            DefaultDataBuffer joinedBuffers = new DefaultDataBufferFactory().join(dataBuffers);
                            byte[] content = new byte[joinedBuffers.readableByteCount()];
                            joinedBuffers.read(content);
                            String responseBody = new String(content, StandardCharsets.UTF_8);//MODIFY RESPONSE and Return the Modified response
                            log.info("request.id: {}, method: {}, url: {}, \nresponse body :{}", request.getId(), request.getMethodValue(), request.getURI(), responseBody);

                            return dataBufferFactory.wrap(responseBody.getBytes());
                        })
                        .switchIfEmpty(Flux.defer(() -> {

                            System.out.println("If empty");
                            return Flux.just();
                        }))
                        ).onErrorResume(err -> {
                            log.error("error while decorating Response: {}", err.getMessage());
                            return Mono.empty();
                        });

                    } else {
                        System.out.println("Not Flux");
                    }
                    return super.writeWith(body);
                }
            };
        }
            

    private ServerHttpRequest getDecoratedRequest(ServerHttpRequest request) {
        
        // Decorator Pattern
        // ServerHttpRequestDecorator : ServerHttpRequest 객체를 데코레이트하고 있다.
        // ServerHttpRequestDecorator : ServerHttpRequest 객체에 새로운 기능을 추가하고 있다. (TTP 요청의 본문을 로깅하는 기능)

        return new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                log.info("request.id: {}, method: {}, url: {}", request.getId(), request.getMethodValue(), request.getURI());
                
                return super.getBody().publishOn(Schedulers.boundedElastic()).doOnNext(dataBuffer -> {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                        String body = baos.toString(StandardCharsets.UTF_8);
                        log.info("request.id: {}, request body :{}", request.getId(), body);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }

                });
            }
            
        };

    }
    
    
}
