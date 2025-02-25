package com.example.restservice;

import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;





public class TokenValidatorInterceptor implements HandlerInterceptor {

    // T: patterns to apply this interceptor
    public static final String patternToApply = "/api/**";

    @Override
    // T: This function is called among every https request that respect the patterns
    // specified above. This function check if there is a valid Access Token in 
    // the https request.
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("Interceptor: Request URI - " + request.getRequestURI());

        String loginToken = request.getHeader("Authorization");
        if(loginToken == null || loginToken.isEmpty() || !TokenValidatorEntraId.validateToken(loginToken)) {
            System.out.println("Invalid token");
            return false;
        } else {
            System.out.println("Valid token");
            return true;
        }
    }
}