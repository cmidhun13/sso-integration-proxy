package com.szells.sso.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;

@Configuration
public class WebConfig {

	public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
            	registry
            	.addMapping("/**")
    			.allowedMethods("HEAD", "GET", "POST", "PUT", "OPTIONS")
    			.allowedOrigins("*")

    			.allowedHeaders("*");
            }
        };

    }
	
}

