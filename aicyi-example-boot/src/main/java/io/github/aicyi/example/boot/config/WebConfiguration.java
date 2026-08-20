package io.github.aicyi.example.boot.config;

import io.github.aicyi.commons.security.token.jwt.IJWTInfo;
import io.github.aicyi.commons.core.token.AuthenticationTokenService;
import io.github.aicyi.example.domain.UserInfo;
import io.github.aicyi.midware.redis.EnhancedRedisTemplateFactory;
import io.github.aicyi.midware.redis.token.AuthenticationConfig;
import io.github.aicyi.midware.redis.token.JwtRefreshAuthenticationTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * @author Mr.Min
 * @description Web相关配置
 * <p>
 * 权限拦截与请求信息日志由启动类上的 {@code @EnableRestApi} 注解自动装配
 * @date 11:48
 **/
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final EnhancedRedisTemplateFactory templateFactory;

    public WebConfiguration(EnhancedRedisTemplateFactory templateFactory) {
        this.templateFactory = templateFactory;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //将所有/static/** 访问都映射到classpath:/static/ 目录下
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/webjars/");
        registry.addResourceHandler("/api-doc.html")
                .addResourceLocations("classpath:/api-doc.html");
    }

    @Bean
    public CorsFilter apiCrossFilter() {
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("*");
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    public AuthenticationTokenService<IJWTInfo> tokenService() {

        AuthenticationConfig config = AuthenticationConfig.builder()
                .secretKey("OczHbdKy3tzPx2PdYw5FwyQALsEZ36jd0Vrj3ZWZ3ic=")
                .issuer("aicyi")
                .subject("aicyi.com")
                .refreshTokenTtl(7)
                .refreshTokenTimeUnit(TimeUnit.DAYS)
                .accessTokenTtl(1)
                .accessTokenTimeUnit(TimeUnit.DAYS)
                .multiTokenAllowed(true)
                .multiTokenCount(2)
                .build();

        return new JwtRefreshAuthenticationTokenService<>(config, templateFactory.getStringRedisTemplate(), UserInfo.class);
    }
}
