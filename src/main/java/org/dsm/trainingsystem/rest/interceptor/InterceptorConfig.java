
package org.dsm.trainingsystem.rest.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则，/**表示拦截所有请求
        // excludePathPatterns 用户排除拦截
/*        registry.addInterceptor(getTokenInterceptor()).addPathPatterns("/**")
                .excludePathPatterns("/trainingsystem/api/common/login",
                        "/trainingsystem/api/user/info",
                        "/trainingsystem/api/user/logout",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/**",
                        "/swagger-ui.html/**");
        super.addInterceptors(registry);*/
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    //PUT方法过滤器
    @Bean
    public FormContentFilter formContentFilter(){
        return new FormContentFilter();
    }

    @Bean
    public TokenInterceptor getTokenInterceptor()
    {
        return new TokenInterceptor();
    }
}
