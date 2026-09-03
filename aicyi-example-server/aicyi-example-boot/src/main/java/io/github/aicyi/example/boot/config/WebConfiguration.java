package io.github.aicyi.example.boot.config;

import io.github.aicyi.commons.core.token.IJWTInfo;
import io.github.aicyi.commons.core.token.AuthenticationTokenService;
import io.github.aicyi.example.domain.bo.UserInfo;
import io.github.aicyi.midware.redis.template.EnhancedRedisTemplateFactory;
import io.github.aicyi.midware.redis.token.AuthenticationConfig;
import io.github.aicyi.midware.redis.token.JwtRefreshAuthenticationTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * @author Mr.Min
 * @description Web相关配置
 * <p>
 * 权限拦截与请求信息日志由启动类上的 {@code @EnableMidwareWeb} 注解自动装配。
 * <p>
 * 静态资源不再自定义 {@code addResourceHandlers}：自研接口文档已迁至 {@code classpath:/static/apidoc/}，
 * 跳板页迁至 {@code classpath:/static/api-doc.html}，均由 Spring Boot 默认静态资源映射暴露；
 * 原先的 {@code /webjars/** -> classpath:/webjars/} 映射会覆盖 Boot 默认的
 * {@code /webjars/** -> classpath:/META-INF/resources/webjars/}，使 springdoc 自带的
 * {@code org.webjars:swagger-ui} 资源无法按 webjars 路径访问（资源解析异常被全局处理器转成
 * HTTP 200 + JSON 错误体，前端表现为「加载成功但内容错乱」），故整体移除。
 * @date 11:48
 **/
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final EnhancedRedisTemplateFactory templateFactory;

    public WebConfiguration(EnhancedRedisTemplateFactory templateFactory) {
        this.templateFactory = templateFactory;
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
