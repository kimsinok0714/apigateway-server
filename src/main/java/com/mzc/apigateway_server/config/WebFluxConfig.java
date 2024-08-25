package com.mzc.apigateway_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@Configuration
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class WebFluxConfig {
    
    // 추가적인 설정이 필요하지 않음
}