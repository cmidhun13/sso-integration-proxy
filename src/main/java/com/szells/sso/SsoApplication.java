package com.szells.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.szells.sso.filter.SsoZuulFilter;

//@EnableZuulProxy
@ServletComponentScan

@SpringBootApplication
public class SsoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsoApplication.class, args);
	}

	@Bean
	public SsoZuulFilter ssoZuulFilter() {
		return new SsoZuulFilter();
	}
}
