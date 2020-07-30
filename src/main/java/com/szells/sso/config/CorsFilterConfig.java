package com.szells.sso.config;

import java.util.Collections;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

//@Configuration
public class CorsFilterConfig {
	/**
     * This special filter is needed so unauthorized request that are rejected by Spring security
     * still have CORS headers.
     * For some reason he {@code bean.setOrder} call is not enough, the configuration also needs
     * {@code security.filter-order=5} for the CORS filter to be in front of the Spring Security
     * filter.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        //based on https://github.com/spring-projects/spring-boot/issues/5834
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        // source.registerCorsConfiguration("/sso/cms/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }
}