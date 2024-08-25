package com.mzc.apigateway_server.filter;


import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.google.gson.Gson;
import com.mzc.apigateway_server.service.AccountService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.*;
import java.nio.charset.*;

@Component
@Slf4j
public class TokenCheckFilter implements WebFilter {
    
    @Autowired
    private AccountService accountService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        
        /*
        ServerHttpRequest request = exchange.getRequest();

        Object temp = request.getHeaders().get("token");
        Object temp2 = request.getHeaders().get("accountId");
        
        String accountId = "";
        String token = "";
        
        if (temp != null) {
            token = temp.toString().replace("[", "").replace("]", "");
        }

        if (temp2 != null) {
            accountId = temp2.toString().replace("[", "").replace("]", "");
        }

        log.info("token = {}", token);
        log.info("accountId = {}", accountId);

        boolean success = false;
        success = accountService.existsByAccountIdAndToken(accountId, token);

        log.info("user autuentication check result = {}", success);
       
        if (!success) {
            return errorResponse(exchange);
        }
        
        */

        return chain.filter(exchange);

    }


    /**
     * 에러 메시지 응답 전달
     * @param exchange
     * @return
     */

    private Mono<Void> errorResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();

        /*
         * Java 애플리케이션에서 JSON 데이터를 처리하기 위해 사용되는 라이브러리입니다. 
         * Google에서 개발한 이 라이브러리는 Java 객체를 JSON 형식으로 직렬화하거나 JSON 데이터를 Java 객체로 역직렬화하는 데 사용됩니다. 
         */
        Gson gson = new Gson();
       
        response.setStatusCode(HttpStatusCode.valueOf(HttpStatus.SC_UNAUTHORIZED));  // 응답 코드 : 401

        Map<String, String> paramMap = new HashMap<>();

        paramMap.put("code", "401");
        paramMap.put("message", "Unauthorized token");

        String json = gson.toJson(paramMap);

        // Response Log DB 저장 필요
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(HttpStatusCode.valueOf(HttpStatus.SC_UNAUTHORIZED));
        return response.writeWith(Mono.just(buffer));
    }

}
