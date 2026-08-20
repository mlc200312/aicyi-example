package io.github.aicyi.example.test.security;

import io.github.aicyi.commons.lang.exception.TokenException;
import io.github.aicyi.commons.lang.exception.TokenExpiredException;
import io.github.aicyi.commons.lang.exception.TokenParseException;
import io.github.aicyi.commons.security.token.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JwtTokenProvider} 单元测试（HS256 签发 / 验签 / 解析）
 */
@DisplayName("JwtTokenProvider - JWT 令牌签发与解析")
class JwtTokenProviderTest {

    /**
     * HS256 要求密钥至少 32 字节
     */
    private static final String SECRET = "aicyi-jwt-test-secret-key-0123456789";
    private static final String ISSUER = "aicyi-test";
    private static final String SUBJECT = "aicyi-user";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, ISSUER, SUBJECT);

    private Map<String, Object> attributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1001);
        attributes.put("username", "tester");
        return attributes;
    }

    @Test
    @DisplayName("create 指定有效期：Claims 中 jti/iss/sub 与自定义属性均可解析")
    void testCreateAndParse() {
        String token = provider.create("token-001", attributes(), 1L, TimeUnit.HOURS);

        Claims claims = provider.parseClaims(token);

        assertEquals("token-001", claims.getId());
        assertEquals(ISSUER, claims.getIssuer());
        assertEquals(SUBJECT, claims.getSubject());
        assertEquals(1001, ((Number) claims.get("userId")).intValue());
        assertEquals("tester", claims.get("username"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("create 未指定有效期：使用默认 2 小时，避免永久 token")
    void testCreateWithDefaultExpiration() {
        String token = provider.create("token-002", attributes());

        Claims claims = provider.parseClaims(token);
        long ttlMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        assertTrue(ttlMillis > TimeUnit.HOURS.toMillis(2) - TimeUnit.MINUTES.toMillis(1),
                "default ttl should be about 2 hours, actual: " + ttlMillis);
        assertTrue(ttlMillis <= TimeUnit.HOURS.toMillis(2));
    }

    @Test
    @DisplayName("getAttributes 仅返回自定义属性，过滤全部标准 Claim")
    void testGetAttributesExcludesStandardClaims() {
        String token = provider.create("token-003", attributes(), 1L, TimeUnit.HOURS);

        Map<String, Object> result = provider.getAttributes(token);

        assertEquals(2, result.size());
        assertEquals("tester", result.get("username"));
        assertFalse(result.containsKey(Claims.ID));
        assertFalse(result.containsKey(Claims.ISSUER));
        assertFalse(result.containsKey(Claims.EXPIRATION));
    }

    @Test
    @DisplayName("getAttribute / getTokenId / getExpiration 返回对应值")
    void testSingleAccessors() {
        String token = provider.create("token-004", attributes(), 1L, TimeUnit.HOURS);

        assertEquals("token-004", provider.getTokenId(token));
        assertEquals("tester", provider.getAttribute(token, "username"));
        assertNotNull(provider.getExpiration(token));
    }

    @Test
    @DisplayName("getRemainingTtl 返回剩余有效期且不超过总有效期")
    void testGetRemainingTtl() {
        String token = provider.create("token-005", attributes(), 60L, TimeUnit.SECONDS);

        long remaining = provider.getRemainingTtl(token, TimeUnit.SECONDS);

        assertTrue(remaining > 0 && remaining <= 60, "remaining: " + remaining);
    }

    @Test
    @DisplayName("isValid：合法 token 为 true，非法 / null token 为 false")
    void testIsValid() {
        String token = provider.create("token-006", attributes(), 1L, TimeUnit.HOURS);

        assertTrue(provider.isValid(token));
        assertFalse(provider.isValid("not.a.jwt"));
        assertFalse(provider.isValid(null));
    }

    @Test
    @DisplayName("过期 token 解析抛 TokenExpiredException")
    void testExpiredToken() throws InterruptedException {
        // exp 精度为秒，1ms 有效期 + 睡眠确保跨越过期点
        String token = provider.create("token-007", attributes(), 1L, TimeUnit.MILLISECONDS);
        Thread.sleep(1200);

        assertThrows(TokenExpiredException.class, () -> provider.parseClaims(token));
        assertFalse(provider.isValid(token));
    }

    @Test
    @DisplayName("其他密钥签发的 token 验签失败，抛 TokenException")
    void testWrongSecret() {
        JwtTokenProvider other = new JwtTokenProvider(
                "another-secret-key-for-test-9876543210", ISSUER, SUBJECT);
        String token = other.create("token-008", attributes(), 1L, TimeUnit.HOURS);

        assertThrows(TokenException.class, () -> provider.parseClaims(token));
        assertFalse(provider.isValid(token));
    }

    @Test
    @DisplayName("非法格式 token 解析抛 TokenParseException")
    void testMalformedToken() {
        assertThrows(TokenParseException.class, () -> provider.parseClaims("not.a.jwt"));
    }

    @Test
    @DisplayName("构造参数校验：空 issuer / subject 抛 IllegalArgumentException")
    void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider(SECRET, " ", SUBJECT));
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider(SECRET, ISSUER, ""));
    }

    @Test
    @DisplayName("attributes 为 null 时 create 不报错且 getAttributes 返回空 Map")
    void testNullAttributes() {
        String token = provider.create("token-009", null, 1L, TimeUnit.HOURS);

        assertTrue(provider.isValid(token));
        assertTrue(provider.getAttributes(token).isEmpty());
        assertTrue(provider.getAttributes("not.a.jwt").isEmpty());
    }

    @Test
    @DisplayName("过期时间 getExpiration 与签发时指定的 TTL 一致")
    void testExpirationMatchesTtl() {
        Date before = new Date();
        String token = provider.create("token-010", attributes(), 30L, TimeUnit.MINUTES);
        Date after = new Date();

        Date expiration = provider.getExpiration(token);
        long expectedMin = before.getTime() + TimeUnit.MINUTES.toMillis(30) - 1000;
        long expectedMax = after.getTime() + TimeUnit.MINUTES.toMillis(30) + 1000;

        assertTrue(expiration.getTime() >= expectedMin && expiration.getTime() <= expectedMax);
    }
}
