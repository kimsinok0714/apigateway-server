package com.mzc.apigateway_server.controller;



import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.apigateway_server.dto.DepartmentDto;
import com.mzc.apigateway_server.dto.ItemDto;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.*;



@RestController
@RequestMapping(value = "api/v1")
@Slf4j
public class AggregateController {

    // HTTP 요청을 수행하기 위한 논 블록킹 방식의 반응형 클라이언트입니다.
    private final WebClient webClient;

    public AggregateController(WebClient webClient) {
        this.webClient = webClient;        
    }
     
    
    @GetMapping(value = "/aggregate/{departmentId}/{itemId}")
    public Mono<ResponseEntity<String>> aggregate(@PathVariable("departmentId") Long departmentId, @PathVariable("itemId") String itemId, ServerWebExchange exchange) throws Exception {
        
        Mono<EntityModel<DepartmentDto>> response1 = webClient.get()
            .uri("http://127.0.0.1:8070/api/v1/departments/" + departmentId)
            .header("Accept", "application/hal+json")            
            .retrieve()
            .onStatus(status -> status.isError(), clientResponse -> {
                // 오류 처리 로직 추가
                return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new RuntimeException("Error response: " + errorBody)));
            })
            .bodyToMono(new ParameterizedTypeReference<EntityModel<DepartmentDto>>() {});


        Mono<EntityModel<ItemDto>> response2 = webClient.get()
            .uri("http://127.0.0.1:8070/api/v1/items/" + itemId)
            .header("Accept", "application/hal+json")    // application/json
            .retrieve()
            .onStatus(status -> status.isError(), clientResponse -> {
                // 오류 처리 로직 추가
                return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new RuntimeException("Error response: " + errorBody)));
            })
            .bodyToMono(new ParameterizedTypeReference<EntityModel<ItemDto>>() {});
           
        log.info("response1 = {}", response1);  
        log.info("response2 = {}", response2);

                    
        return Mono.zip(response1, response2)
            .map(tuple -> {
                EntityModel<DepartmentDto> department = tuple.getT1();
                EntityModel<ItemDto> item = tuple.getT2();

                String combinedResponse = "";
                try {
                    combinedResponse = createCombinedResponse(department, item);
                } catch (Exception e) {                  
                    e.printStackTrace();                    
                }
                
                // ResponseEntity로 응답을 작성
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(combinedResponse);
            });
            
    }



    private String createCombinedResponse(EntityModel<DepartmentDto> department, EntityModel<ItemDto> item) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("department", department);
        responseMap.put("item", item);
        return objectMapper.writeValueAsString(responseMap);
    }

    
    
}
