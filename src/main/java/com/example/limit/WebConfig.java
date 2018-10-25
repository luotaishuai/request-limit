package com.example.limit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author anonymity
 * @create 2018-10-24 16:54
 **/
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Bean
    public HandlerInterceptor getUrlInterceptor(){
        return new UrlInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getUrlInterceptor()).addPathPatterns("/hello/**/*");
        super.addInterceptors(registry);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                .allowedOrigins("*")
                //设置允许跨域请求方式,或为allowedMethods("*")
                .allowedMethods("GET", "POST", "PUT", "OPTIONS", "DELETE")
                //是否允许证书 2.0不再默认开启
                .allowCredentials(true)
                //允许所有header
                .allowedHeaders("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
