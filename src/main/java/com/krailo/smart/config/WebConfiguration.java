package com.krailo.smart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.krailo.smart.config.LoginSuccessHandler.CHANGE_PASSWORD_URL;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    // тимчасовий пароль: усе, крім сторінки зміни пароля, сторінки входу, стилів і сторінки помилки, закрите
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MustChangePasswordInterceptor())
                .excludePathPatterns(CHANGE_PASSWORD_URL, "/login", "/css/**", "/favicon.ico", "/smartum.jpg", "/error");
    }
}
