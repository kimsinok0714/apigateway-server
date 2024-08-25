package com.mzc.apigateway_server.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jackson2HalModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}


/*
 *  [HAL+JSON] 형식을 처리
 * 
 *  1. 의존성 설정 : build.gradle
 *     dependencies {
 *        implementation 'org.springframework.boot:spring-boot-starter-hateoas'
 *        implementation 'org.springframework.hateoas:spring-hateoas'
 *     }
 * 
 *   2. Feign 클라이언트 설정 
 *      - HAL을 처리하기 위해 ObjectMapper를 사용자 정의하는 FeignClientConfiguration 클래스를 만듭니다.
 * 
 *   3. 이 설정을 Feign 클라이언트에서 사용한다.
 * 
 *      @FeignClient(name = "APIGATEWAY-SERVER", url = "${api.gateway.url}", configuration = FeignClientConfiguration.class) 
 *        public interface DepartmentFeignClient {
 *           @GetMapping("/api/v1/departments/{departmentId}")
 *            ResponseEntity<String> retrieveDepartmentById(@PathVariable("departmentId") Long departmentId);
 *      }
 *
 *   4. Spring HATEOAS 지원 활성화
 * 
 *      @SpringBootApplication
 *      @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
 *      public class Application {
 *         public static void main(String[] args) {
 *            SpringApplication.run(Application.class, args);
 *         }
 *      } 
 */