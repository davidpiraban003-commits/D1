package com.david.tienda.config;

import com.david.tienda.filter.JwtValidationFilter;
import com.david.tienda.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtValidationFilter> jwtFilterRegistration(JwtService jwtService,
                                                                             ObjectMapper objectMapper) {
        FilterRegistrationBean<JwtValidationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtValidationFilter(jwtService, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
