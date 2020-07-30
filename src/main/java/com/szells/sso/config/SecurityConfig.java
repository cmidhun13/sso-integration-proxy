package com.szells.sso.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Value ("${app.user}")
	private String APP_USER;

	@Value ("${app.pwd}")
	private String APP_PWD;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
			.withUser(APP_USER).password(passwordEncoder().encode(APP_PWD)).roles("USER");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
        .authorizeRequests()
        .antMatchers("/*").permitAll()
        .antMatchers("/logout/sso/url").denyAll()
				//.antMatchers("/studio/api/1/services/api/1/security/validate-session.json").authenticated()
            .anyRequest().hasAnyRole("USER", "ADMIN")
            .and()
            .httpBasic();
        http.csrf().disable();
	}
	
	@Override 
	public void configure(WebSecurity web) throws Exception {
		web.ignoring()
		.antMatchers("/**"); 
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
}