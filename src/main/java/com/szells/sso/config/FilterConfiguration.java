package com.szells.sso.config;

import com.szells.sso.controller.RedirectFilter;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.DispatcherType;
import java.util.Collections;


public class FilterConfiguration {
    private final RedirectFilter redirectFilter;

   @Autowired
    public FilterConfiguration(RedirectFilter redirectFilter) {
        this.redirectFilter = redirectFilter;
    }

    @Bean
    public FilterRegistrationBean<RedirectFilter> dateLoggingFilterRegistration() {
        FilterRegistrationBean<RedirectFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(redirectFilter);
        filterRegistrationBean.setUrlPatterns(Collections.singletonList("/*"));
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        filterRegistrationBean.setOrder(Ordered.LOWEST_PRECEDENCE - 1);
        return filterRegistrationBean;
    }
}
