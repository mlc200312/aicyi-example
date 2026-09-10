package io.github.aicyi.example.boot.token;

import io.github.aicyi.commons.core.token.AuthenticationTokenService;
import io.github.aicyi.commons.core.token.TokenPair;
import io.github.aicyi.commons.lang.exception.TokenExpiredException;
import io.github.aicyi.commons.lang.exception.TokenInvalidException;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AuthenticationTokenService} 接口契约的 Spring Boot 集成单元测试。
 *
 * <p>被测对象以 <b>接口类型</b> {@code AuthenticationTokenService<UserInfo>} 持有，实现由容器同款的
 * {@link JwtRefreshAuthenticationTokenService}（JWT 承载 AccessToken、Redis 承载 RefreshToken）提供，
 * 目的是校验 SPI 契约本身，而非某一实现细节；实现级的多设备名额裁剪、claim 白名单等由
 * {@code JwtRefreshAuthenticationTokenServiceTest} / {@code MultiRedisTokenServiceImplTest} 覆盖。
 *
 * <p>依赖真实运行环境：需 Nacos 配置中心与 Redis 可用（与其它 {@code @SpringBootTest} 用例一致），
 * Redis 连接由容器内的 {@link EnhancedRedisTemplateFactory} 提供。
 *
 * <p>按接口声明的 7 个方法逐一覆盖，并校验其契约边界：
 * <ul>
 *     <li>{@code createToken}：返回完整 {@link TokenPair}（双 Token 非空、有效期为正）；attributes 可为 null</li>
 *     <li>{@code refreshToken}：重签 AccessToken 且保留 RefreshToken；已撤销出 {@link TokenExpiredException}
 *         （40102），空白入参出 {@link TokenInvalidException}</li>
 *     <li>{@code getRefreshTokens}：返回主体全部在线 RefreshToken；未知主体返回空集合而非 null</li>
 *     <li>{@code revokeToken}：撤销后移出在线列表且会话失效；空白入参静默返回、不误伤在线 Token</li>
 *     <li>{@code validateAccessToken}：合法 true、非法收敛为 false 而非抛异常</li>
 *     <li>{@code parsePrincipal}：还原完整主体</li>
 *     <li>{@code getAttributes}：返回自定义属性并剔除内部 principal claim；无属性时返回空 Map</li>
 * </ul>
 *
 * <p>各用例使用互不相同的 userId，且统一使用独立 {@code keyPrefix}，与应用自身的 {@code aicyi:token}
 * 键空间及其它测试互不干扰，无需跨用例清理 Redis 状态。
 *
 * @author Mr.Min
 */
@SpringBootTest(classes = AicyiExampleApplication.class)
class AuthenticationTokenServiceTest {

    /**
     * 本测试类专用的 Token 键空间前缀，避免与自动配置装配的 {@code aicyi:token} 键空间交叉裁剪
     */
    private static final String KEY_PREFIX = "aicyi-ut-auth";

    /**
     * RefreshToken 有效期（7 天）；足够长以保证用例执行期间不会自然过期
     */
    private static final long REFRESH_TTL = 7L;

    /**
     * AccessToken 有效期（2 小时）
     */
    private static final long ACCESS_TTL = 2L;

    @Autowired
    private EnhancedRedisTemplateFactory factory;

    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        stringRedisTemplate = factory.getStringRedisTemplate();
    }

    // ------------------------------------------------------------------
    // createToken
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createToken - 返回完整 TokenPair：双 Token 非空、有效期为正、AccessToken 可校验、RefreshToken 在线")
    void testCreateTokenReturnsCompletePair() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(true, 3);

        UserInfo principal = principal(userId, "device-create");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("role", "ADMIN");

        TokenPair pair = service.createToken(principal, attributes);

        assertNotNull(pair);
        assertNotNull(pair.getAccessToken());
        assertNotNull(pair.getRefreshToken());
        assertFalse(pair.getAccessToken().isBlank());
        assertFalse(pair.getRefreshToken().isBlank());
        assertTrue(pair.getAccessTokenExpiresIn() > 0);
        assertTrue(pair.getRefreshTokenExpiresIn() > 0);

        // 契约联动：签发的 AccessToken 立即可校验，RefreshToken 立即出现在在线列表
        assertTrue(service.validateAccessToken(pair.getAccessToken()));
        assertTrue(service.getRefreshTokens(principal).contains(pair.getRefreshToken()));
    }

    @Test
    @DisplayName("createToken - attributes 为 null 时正常签发，不抛异常")
    void testCreateTokenWithNullAttributesSucceeds() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(false, 1);

        TokenPair pair = service.createToken(principal(userId, "device-null-attr"), null);

        assertNotNull(pair);
        assertTrue(service.validateAccessToken(pair.getAccessToken()));
    }

    // ------------------------------------------------------------------
    // refreshToken
    // ------------------------------------------------------------------

    @Test
    @DisplayName("refreshToken - 重签新的 AccessToken 并保留原 RefreshToken，新 AccessToken 可解析回主体")
    void testRefreshTokenIssuesNewAccessTokenKeepsRefreshToken() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(true, 3);

        UserInfo principal = principal(userId, "device-refresh");
        TokenPair origin = service.createToken(principal, null);

        TokenPair refreshed = service.refreshToken(origin.getRefreshToken());

        assertNotNull(refreshed.getAccessToken());
        // RefreshToken 保持不变，AccessToken 重新签发（jti 唯一，故与原值不同）
        assertEquals(origin.getRefreshToken(), refreshed.getRefreshToken());
        assertNotEquals(origin.getAccessToken(), refreshed.getAccessToken());
        assertTrue(service.validateAccessToken(refreshed.getAccessToken()));
        assertEquals(principal, service.parsePrincipal(refreshed.getAccessToken()));
    }

    @Test
    @DisplayName("refreshToken - 已撤销的 RefreshToken 刷新抛 TokenExpiredException（40102）")
    void testRefreshTokenAfterRevokeThrowsExpired() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(false, 1);

        TokenPair pair = service.createToken(principal(userId, "device-refresh-revoked"), null);
        service.revokeToken(pair.getRefreshToken());

        assertThrows(TokenExpiredException.class, () -> service.refreshToken(pair.getRefreshToken()));
    }

    @Test
    @DisplayName("refreshToken - 空白 RefreshToken 抛 TokenInvalidException")
    void testRefreshTokenBlankThrowsInvalid() {

        AuthenticationTokenService<UserInfo> service = buildService(false, 1);

        assertThrows(TokenInvalidException.class, () -> service.refreshToken(null));
        assertThrows(TokenInvalidException.class, () -> service.refreshToken(""));
    }

    // ------------------------------------------------------------------
    // getRefreshTokens
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getRefreshTokens - 返回同一主体多设备的全部在线 RefreshToken")
    void testGetRefreshTokensReturnsAllOnlineDevices() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(true, 3);

        Set<String> issued = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            // 3 个不同设备登录，名额上限 3，全部保留
            issued.add(service.createToken(principal(userId, "device-online-" + i), null).getRefreshToken());
        }

        Set<String> online = service.getRefreshTokens(principal(userId, "device-probe"));

        assertEquals(3, online.size());
        assertTrue(online.containsAll(issued));
    }

    @Test
    @DisplayName("getRefreshTokens - 未登录主体返回空集合而非 null")
    void testGetRefreshTokensEmptyForUnknownPrincipal() {

        AuthenticationTokenService<UserInfo> service = buildService(false, 1);

        Set<String> online = service.getRefreshTokens(principal(newUserId(), "device-none"));

        assertNotNull(online);
        assertTrue(online.isEmpty());
    }

    // ------------------------------------------------------------------
    // revokeToken
    // ------------------------------------------------------------------

    @Test
    @DisplayName("revokeToken - 撤销后从在线列表移除，且该 RefreshToken 会话失效")
    void testRevokeTokenRemovesFromOnlineList() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(true, 3);

        TokenPair pair = service.createToken(principal(userId, "device-revoke"), null);
        String refreshToken = pair.getRefreshToken();

        service.revokeToken(refreshToken);

        assertFalse(service.getRefreshTokens(principal(userId, "device-probe")).contains(refreshToken));
        assertThrows(TokenExpiredException.class, () -> service.refreshToken(refreshToken));
    }

    @Test
    @DisplayName("revokeToken - 空白入参静默返回，不误伤该主体已在线的 Token")
    void testRevokeTokenIdempotentForBlank() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(true, 3);

        UserInfo principal = principal(userId, "device-blank-revoke");
        TokenPair pair = service.createToken(principal, null);

        // 契约：null / 空串在触达底层存储前直接 return
        service.revokeToken(null);
        service.revokeToken("");

        assertTrue(service.getRefreshTokens(principal).contains(pair.getRefreshToken()));
    }

    // ------------------------------------------------------------------
    // validateAccessToken
    // ------------------------------------------------------------------

    @Test
    @DisplayName("validateAccessToken - 合法 AccessToken 返回 true")
    void testValidateAccessTokenTrueForValid() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(false, 1);

        TokenPair pair = service.createToken(principal(userId, "device-validate"), null);

        assertTrue(service.validateAccessToken(pair.getAccessToken()));
    }

    @Test
    @DisplayName("validateAccessToken - 非法 AccessToken 收敛为 false，不抛异常")
    void testValidateAccessTokenFalseForInvalid() {

        AuthenticationTokenService<UserInfo> service = buildService(false, 1);

        assertFalse(service.validateAccessToken("invalid-access-token"));
    }

    // ------------------------------------------------------------------
    // parsePrincipal
    // ------------------------------------------------------------------

    @Test
    @DisplayName("parsePrincipal - 无 claim 白名单时还原完整主体")
    void testParsePrincipalRoundTripsFullPrincipal() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(false, 1);

        UserInfo principal = principal(userId, "device-parse");
        TokenPair pair = service.createToken(principal, null);

        UserInfo parsed = service.parsePrincipal(pair.getAccessToken());

        assertEquals(principal, parsed);
        assertEquals(principal.getId(), parsed.getId());
        assertEquals(principal.getMobile(), parsed.getMobile());
    }

    // ------------------------------------------------------------------
    // getAttributes
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAttributes - 返回自定义属性且剔除内部 principal claim")
    void testGetAttributesReturnsCustomClaimsExcludingPrincipal() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(false, 1);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("role", "ADMIN");
        attributes.put("tenant", "aicyi");

        TokenPair pair = service.createToken(principal(userId, "device-attr"), attributes);

        Map<String, Object> result = service.getAttributes(pair.getAccessToken());

        // 标准 JWT claim（jti/iss/sub/iat/exp）与内部 principal claim 均被剔除，仅剩自定义属性
        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get("role"));
        assertEquals("aicyi", result.get("tenant"));
        assertFalse(result.containsKey("principal"));
    }

    @Test
    @DisplayName("getAttributes - 无自定义属性时返回空 Map 而非 null")
    void testGetAttributesEmptyWhenNoCustomAttributes() {

        long userId = newUserId();
        AuthenticationTokenService<UserInfo> service = buildService(false, 1);

        TokenPair pair = service.createToken(principal(userId, "device-empty-attr"), null);

        Map<String, Object> result = service.getAttributes(pair.getAccessToken());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------------
    // 测试夹具
    // ------------------------------------------------------------------

    /**
     * 以接口类型构建被测服务；实现固定为容器同款 {@link JwtRefreshAuthenticationTokenService}，
     * 每次生成独立 HS256 密钥与专用 keyPrefix，用例内自洽且互不干扰。
     *
     * @param multiTokenAllowed 是否开启多设备名额
     * @param multiTokenCount   名额上限（按设备计数）
     */
    private AuthenticationTokenService<UserInfo> buildService(boolean multiTokenAllowed, int multiTokenCount) {

        AuthenticationConfig config = AuthenticationConfig.builder()
                .secretKey(validSecret())
                .issuer("aicyi-example")
                .subject("aicyi-example-boot")
                .keyPrefix(KEY_PREFIX)
                .refreshTokenTtl(REFRESH_TTL)
                .refreshTokenTimeUnit(TimeUnit.DAYS)
                .accessTokenTtl(ACCESS_TTL)
                .accessTokenTimeUnit(TimeUnit.HOURS)
                .multiTokenAllowed(multiTokenAllowed)
                .multiTokenCount(multiTokenCount)
                .build();

        return new JwtRefreshAuthenticationTokenService<>(config, stringRedisTemplate, UserInfo.class);
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
