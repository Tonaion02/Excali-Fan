package com.example.restservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;





@Configuration
public class InterceptorConfigurator implements WebMvcConfigurer {

	@Override
	// T: This class is used to configure the interceptors
	// (in particular the patterns each interceptor is applied)
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new TokenValidatorInterceptor()).addPathPatterns(TokenValidatorInterceptor.patternToApply);
	}
}