package io.github.aicyi.example.boot.token;

import io.github.aicyi.commons.core.token.TokenPair;
import io.github.aicyi.commons.lang.exception.TokenExpiredException;
import io.github.aicyi.commons.security.SecretKeyUtils;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.domain.bo.UserInfo;
import io.github.aicyi.midware.redis.template.EnhancedRedisTemplateFactory;
import io.github.aicyi.midware.redis.token.AuthenticationConfig;
import io.github.aicyi.midware.redis.token.JwtRefreshAuthenticationTokenService;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JwtRefreshAuthenticationTokenService} 的 Spring Boot 集成单元测试。
 *
 * <p>依赖真实运行环境：需 Nacos 配置中心与 Redis 可用（与其它 {@code @SpringBootTest} 用例一致），
 * Redis 连接由容器内的 {@link EnhancedRedisTemplateFactory} 提供。
 *
 * <p>覆盖范围：
 * <ul>
 *     <li>双 Token 全生命周期：签发 / 校验 / 解析 / 属性 / 刷新 / 撤销</li>
 *     <li>多设备名额：按 {@code principal.deviceId} 计数（同设备覆盖、超额保留最近名额）</li>
 *     <li>AccessToken claim 字段白名单：仅下发最小字段集，手机号等敏感字段不进入 JWT</li>
 *     <li>{@link AuthenticationConfig} 配置期快速失败：弱密钥 / 空签发者 / 名额越界</li>
 * </ul>
 *
 * <p>各用例使用互不相同的 userId，索引 key 天然隔离，无需跨用例清理 Redis 状态。
 *
 * @author Mr.Min
 */
@SpringBootTest(classes = AicyiExampleApplication.class)
class JwtRefreshAuthenticationTokenServiceTest {

    @Autowired
    private EnhancedRedisTemplateFactory factory;

    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        stringRedisTemplate = factory.getStringRedisTemplate();
    }

    // ------------------------------------------------------------------
    // 生命周期
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createToken/validate/parse/refresh - 双 Token 全生命周期")
    void testFullLifecycle() {

        long userId = newUserId();
        JwtRefreshAuthenticationTokenService<UserInfo> service = buildService(true, 2, null);

        UserInfo principal = principal(userId, "device-life");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("role", "ADMIN");

        TokenPair tokenPair = service.createToken(principal, attributes);

        assertNotNull(tokenPair);
        assertNotNull(tokenPair.getAccessToken());
        assertNotNull(tokenPair.getRefreshToken());
        assertTrue(tokenPair.getAccessTokenExpiresIn() > 0);
        assertTrue(tokenPair.getRefreshTokenExpiresIn() > 0);

        // AccessToken 可校验、可解析回 principal
        assertTrue(service.validateAccessToken(tokenPair.getAccessToken()));
        assertEquals(principal, service.parsePrincipal(tokenPair.getAccessToken()));

        // RefreshToken 在线列表可见
        assertTrue(service.getRefreshTokens(principal).contains(tokenPair.getRefreshToken()));

        // 刷新后签发新的 AccessToken，RefreshToken 保持不变
        TokenPair refreshed = service.refreshToken(tokenPair.getRefreshToken());
        assertNotNull(refreshed.getAccessToken());
        assertEquals(tokenPair.getRefreshToken(), refreshed.getRefreshToken());
        assertTrue(service.validateAccessToken(refreshed.getAccessToken()));
    }

    @Test
    @DisplayName("getAttributes - 返回自定义属性且不泄露 principal claim")
    void testGetAttributesExcludesPrincipalClaim() {

        long userId = newUserId();
        JwtRefreshAuthenticationTokenService<UserInfo> service = buildService(false, 1, null);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("role", "ADMIN");
        attributes.put("tenant", "aicyi");

        TokenPair tokenPair = service.createToken(principal(userId, "device-attr"), attributes);

        Map<String, Object> result = service.getAttributes(tokenPair.getAccessToken());

        assertEquals("ADMIN", result.get("role"));
        assertEquals("aicyi", result.get("tenant"));
        // principal 承载于内部 claim，不得混入业务属性对外返回
        assertFalse(result.containsKey("principal"));
    }

    @Test
    @DisplayName("validateAccessToken - 非法 Token 返回 false 而非抛异常")
    void testValidateAccessTokenInvalid() {

        JwtRefreshAuthenticationTokenService<UserInfo> service = buildService(false, 1, null);

        assertFalse(service.validateAccessToken("invalid-access-token"));
    }

    @Test
    @DisplayName("revokeToken - 撤销后刷新抛 TokenExpiredException 且在线列表移除")
    void testRevokeThenRefreshThrows() {

        long userId = newUserId();
        JwtRefreshAuthenticationTokenService<UserInfo> service = buildService(false, 1, null);

        UserInfo principal = principal(userId, "device-revoke");
        TokenPair tokenPair = service.createToken(principal, null);

        service.revokeToken(tokenPair.getRefreshToken());

        assertFalse(service.getRefreshTokens(principal).contains(tokenPair.getRefreshToken()));
        assertThrows(TokenExpiredException.class, () -> service.refreshToken(tokenPair.getRefreshToken()));
    }

    @Test
    @DisplayName("getRefreshTokens - 未登录用户返回空集合")
    void testGetRefreshTokensEmptyForUnknownUser() {

        JwtRefreshAuthenticationTokenService<UserInfo> service = buildService(false, 1, null);

        Set<String> tokens = service.getRefreshTokens(principal(newUserId(), "device-none"));

        assertNotNull(tokens);
        assertTrue(tokens.isEmpty());
    }

    // ------------------------------------------------------------------
    // 多设备名额（按 principal.deviceId 计数）
    // ------------------------------------------------------------------

    @Test
    @DisplayName("多设备名额 - 同一设备重复登录覆盖旧 Token，仅保留 1 个")
    void testSameDeviceRepeatedLoginCollapsesToOne() {

        long userId = newUserId();
        JwtRefreshAuthenticationTokenService<UserInfo> service = buildService(true, 3, null);

        for (int i = 0; i < 3; i++) {
            // 同一 userId、同一 deviceId：名额按设备计数，重复登录覆盖而非累加
            service.createToken(principal(userId, "device-same"), null);
        }

        Set<String> tokens = service.getRefreshTokens(principal(userId, "device-same"));

        assertEquals(1, tokens.size());
    }

    @Test
    @DisplayName("多设备名额 - 设备数超限时保留最近的名额数，最旧设备被踢下线")
    void testDeviceQuotaKeepsMostRecent() {

        long userId = newUserId();
        JwtRefreshAuthenticationTokenService<UserInfo> service = buildService(true, 2, null);

        List<String> refreshTokens = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            // 4 个不同设备登录，名额上限 2，应保留最近的 device-2 / device-3
            TokenPair tokenPair = service.createToken(principal(userId, "device-" + i), null);
            refreshTokens.add(tokenPair.getRefreshToken());
        }

        Set<String> alive = service.getRefreshTokens(principal(userId, "device-probe"));

        assertEquals(2, alive.size());
        assertFalse(alive.contains(refreshTokens.get(0)));
        assertFalse(alive.contains(refreshTokens.get(1)));
        assertTrue(alive.contains(refreshTokens.get(2)));
        assertTrue(alive.contains(refreshTokens.get(3)));
    }

    // ------------------------------------------------------------------
    // AccessToken claim 字段白名单
    // ------------------------------------------------------------------

    @Test
    @DisplayName("claim 白名单 - AccessToken 仅承载白名单字段，手机号/昵称不外泄")
    void testClaimWhitelistFiltersSensitiveFields() {

        long userId = newUserId();
        Set<String> claimFields = new LinkedHashSet<>(Arrays.asList("userId", "username", "deviceId"));
        JwtRefreshAuthenticationTokenService<UserInfo> service = buildService(false, 1, claimFields);

        UserInfo principal = principal(userId, "device-claim");
        TokenPair tokenPair = service.createToken(principal, null);

        UserInfo parsed = service.parsePrincipal(tokenPair.getAccessToken());

        // 白名单字段保留，getId() 依赖 userId 仍可正常鉴权
        assertEquals(userId, parsed.getUserId());
        assertEquals("device-claim", parsed.getDeviceId());
        assertNotNull(parsed.getUsername());
        assertEquals(principal.getId(), parsed.getId());

        // 非白名单的敏感字段不进入 JWT payload
        assertNull(parsed.getMobile());
        assertNull(parsed.getNickname());
    }

    @Test
    @DisplayName("无 claim 白名单 - 兼容既有行为，AccessToken 承载完整 principal")
    void testNoWhitelistRetainsFullPrincipal() {

        long userId = newUserId();
        JwtRefreshAuthenticationTokenService<UserInfo> service = buildService(false, 1, null);

        UserInfo principal = principal(userId, "device-full");
        TokenPair tokenPair = service.createToken(principal, null);

        UserInfo parsed = service.parsePrincipal(tokenPair.getAccessToken());

        assertEquals(principal, parsed);
        assertEquals(principal.getMobile(), parsed.getMobile());
    }

    // ------------------------------------------------------------------
    // AuthenticationConfig 配置期快速失败
    // ------------------------------------------------------------------

    @Test
    @DisplayName("配置校验 - HS256 弱密钥（<32 字节）被拒绝")
    void testConfigRejectsWeakSecretKey() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> baseConfigBuilder().secretKey("short-key").build());

        assertTrue(ex.getMessage().contains("secretKey"));
    }

    @Test
    @DisplayName("配置校验 - 空签发者被拒绝")
    void testConfigRejectsBlankIssuer() {

        assertThrows(IllegalArgumentException.class,
                () -> baseConfigBuilder().secretKey(validSecret()).issuer("  ").build());
    }

    @Test
    @DisplayName("配置校验 - multiTokenCount 越界（0 / 101）被拒绝")
    void testConfigRejectsOutOfRangeMultiTokenCount() {

        assertThrows(IllegalArgumentException.class,
                () -> baseConfigBuilder().secretKey(validSecret()).multiTokenCount(0).build());
        assertThrows(IllegalArgumentException.class,
                () -> baseConfigBuilder().secretKey(validSecret()).multiTokenCount(101).build());
    }

    // ------------------------------------------------------------------
    // 测试夹具
    // ------------------------------------------------------------------

    /**
     * 构建被测服务；每次生成独立 HS256 密钥，用例内自洽即可
     *
     * @param multiTokenAllowed 是否开启多设备名额
     * @param multiTokenCount   名额上限（按设备计数）
     * @param claimFields       AccessToken claim 白名单，null 表示不过滤（全量写入）
     */
    private JwtRefreshAuthenticationTokenService<UserInfo> buildService(boolean multiTokenAllowed,
                                                                        int multiTokenCount,
                                                                        Set<String> claimFields) {

        AuthenticationConfig.Builder builder = baseConfigBuilder()
                .secretKey(validSecret())
                .multiTokenAllowed(multiTokenAllowed)
                .multiTokenCount(multiTokenCount);

        if (claimFields != null) {
            builder.principalClaimFields(claimFields);
        }

        return new JwtRefreshAuthenticationTokenService<>(builder.build(), stringRedisTemplate, UserInfo.class);
    }

    private AuthenticationConfig.Builder baseConfigBuilder() {

        return AuthenticationConfig.builder()
                .issuer("aicyi-example")
                .subject("aicyi-example-boot")
                .refreshTokenTtl(7)
                .refreshTokenTimeUnit(TimeUnit.DAYS)
                .accessTokenTtl(2)
                .accessTokenTimeUnit(TimeUnit.HOURS);
    }

    private String validSecret() {
        // HS256 密钥（32 字节）经 Base64 编码为 44 字符，满足 AuthenticationConfig 的最小长度约束
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        return SecretKeyUtils.asString(secretKey);
    }

    /**
     * 每个用例使用独立 userId，使 Redis 主体索引 key 天然隔离
     */
    private long newUserId() {
        return ThreadLocalRandom.current().nextLong(1_000_000_000L, Long.MAX_VALUE);
    }

    private UserInfo principal(long userId, String deviceId) {

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUsername("tester-" + userId);
        userInfo.setNickname("测试用户");
        userInfo.setMobile("13800001111");
        userInfo.setDeviceId(deviceId);
        return userInfo;
    }
}
