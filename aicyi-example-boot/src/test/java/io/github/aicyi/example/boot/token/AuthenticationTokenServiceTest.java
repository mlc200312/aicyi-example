package io.github.aicyi.example.boot.token;

import io.github.aicyi.commons.core.mapper.BeanMapper;
import io.github.aicyi.commons.core.token.AuthenticationTokenService;
import io.github.aicyi.commons.core.token.TokenPair;
import io.github.aicyi.commons.security.SecretKeyUtils;
import io.github.aicyi.commons.util.UUIDUtils;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.domain.UserBean;
import io.github.aicyi.example.domain.UserInfo;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.midware.redis.EnhancedRedisTemplateFactory;
import io.github.aicyi.midware.redis.token.AuthenticationConfig;
import io.github.aicyi.midware.redis.token.JwtRefreshAuthenticationTokenService;
import io.github.aicyi.example.fixture.util.DataSource;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthenticationTokenService 测试类
 *
 * @author Mr.Min
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
class AuthenticationTokenServiceTest {

    @Autowired
    private EnhancedRedisTemplateFactory factory;

    @Autowired
    private BeanMapper beanMapper;

    private UserInfo principal;

    private AuthenticationTokenService<UserInfo> tokenService;

    @BeforeEach
    void setUp() {

        UserBean userBean = DataSource.getUser();

        User user = beanMapper.map(userBean, User.class);

        principal = UserInfo.of(user, UUIDUtils.generateV7Id());

        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        StringRedisTemplate stringRedisTemplate = factory.getStringRedisTemplate();

        AuthenticationConfig config = AuthenticationConfig.builder()
                .secretKey(SecretKeyUtils.asString(secretKey))
                .issuer("issuer")
                .subject("subject")
                .refreshTokenTtl(7)
                .refreshTokenTimeUnit(TimeUnit.DAYS)
                .accessTokenTtl(1)
                .accessTokenTimeUnit(TimeUnit.DAYS)
                .multiTokenAllowed(true)
                .multiTokenCount(2)
                .build();

        tokenService = new JwtRefreshAuthenticationTokenService<>(config, stringRedisTemplate, UserInfo.class);
    }

    @Test
    @DisplayName("createToken - 创建Token成功")
    void testCreateToken() {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1001L);
        attributes.put("role", "ADMIN");

        TokenPair tokenPair = tokenService.createToken(principal, attributes);

        // then
        assertNotNull(tokenPair);
    }

    @Test
    @DisplayName("refreshToken - 刷新Token成功")
    void testRefreshToken() {
        // given
        TokenPair tokenPair = tokenService.createToken(principal, null);
        String refreshToken = tokenPair.getRefreshToken();

        // when
        TokenPair result = tokenService.refreshToken(refreshToken);

        // then
        assertNotNull(result);
    }

    @Test
    @DisplayName("getRefreshTokens - 获取在线RefreshToken")
    void testGetRefreshTokens() {
        // given
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            TokenPair tokenPair = tokenService.createToken(principal, null);

            tokens.add(tokenPair.getRefreshToken());
        }

        // when
        Set<String> result = tokenService.getRefreshTokens(principal);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("revokeToken - 撤销Token")
    void testRevokeToken() {
        // given
        TokenPair tokenPair = tokenService.createToken(principal, null);
        String refreshToken = tokenPair.getRefreshToken();

        // when
        tokenService.revokeToken(refreshToken);

        // then
        Set<String> refreshTokens = tokenService.getRefreshTokens(principal);

        assertFalse(refreshTokens.contains(refreshTokens));
    }

    @Test
    @DisplayName("validateAccessToken - Token有效")
    void testValidateAccessTokenTrue() {
        // given
        TokenPair tokenPair = tokenService.createToken(principal, null);
        String accessToken = tokenPair.getAccessToken();

        // when
        boolean result = tokenService.validateAccessToken(accessToken);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("validateAccessToken - Token无效")
    void testValidateAccessTokenFalse() {
        // given
        String accessToken = "invalid-access-token";

        // when
        boolean result = tokenService.validateAccessToken(accessToken);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("parsePrincipal - 解析Principal")
    void testParsePrincipal() {
        // given
        TokenPair tokenPair = tokenService.createToken(principal, null);
        String accessToken = tokenPair.getAccessToken();

        // when
        UserInfo result = tokenService.parsePrincipal(accessToken);

        // then
        assertEquals(principal, result);
    }

    @Test
    @DisplayName("getAttributes - 获取自定义属性")
    void testGetAttributes() {
        // given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1001L);
        attributes.put("username", "admin");
        attributes.put("role", "ADMIN");

        TokenPair tokenPair = tokenService.createToken(principal, attributes);
        String accessToken = tokenPair.getAccessToken();

        // when
        Map<String, Object> result = tokenService.getAttributes(accessToken);
        String userId = result.get("userId").toString();

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1001L, Long.valueOf(userId));
        assertEquals("admin", result.get("username"));
        assertEquals("ADMIN", result.get("role"));
    }
}