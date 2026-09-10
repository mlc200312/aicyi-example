package io.github.aicyi.example.boot.token;

import io.github.aicyi.commons.core.token.TokenCreateRequest;
import io.github.aicyi.commons.lang.exception.TokenExpiredException;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.domain.bo.UserInfo;
import io.github.aicyi.midware.redis.template.EnhancedRedisTemplateFactory;
import io.github.aicyi.midware.redis.token.MultiRedisTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
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
 * {@link MultiRedisTokenServiceImpl} 的 Spring Boot 集成单元测试。
 *
 * <p>依赖真实运行环境：需 Nacos 配置中心与 Redis 可用（与其它 {@code @SpringBootTest} 用例一致），
 * Redis 连接由容器内的 {@link EnhancedRedisTemplateFactory} 提供。
 *
 * <p>覆盖范围：
 * <ul>
 *     <li>名额配置：构造器入参回读、非正数名额快速失败、名额字段不可变（无 setter）</li>
 *     <li>按设备计数的名额裁剪：同设备覆盖、超额保留最近名额、关闭多端时恒为单 Token</li>
 *     <li>{@code deviceId} 缺失时降级为按 Token 计数；{@code deviceId} 含分隔符时仍能正确还原 Token</li>
 *     <li>幽灵成员回收：会话已失效的索引成员被过滤并就地 ZREM，索引不会无界增长</li>
 *     <li>Token 生命周期：签发 / 校验 / 解析 / 属性 / 剩余有效期 / 刷新 / 单个撤销 / 全量踢下线（含幂等）</li>
 *     <li>{@code keyPrefix} 键空间隔离：多应用共享同一 Redis 时不互相裁剪、不互踢下线</li>
 *     <li>异常契约：过期与无效分别出 {@code TokenExpiredException}（40102）/ 收敛为 false，入参非法出
 *         {@code IllegalArgumentException}</li>
 * </ul>
 *
 * <p>各用例使用互不相同的 userId，且统一使用独立 {@code keyPrefix}，与应用自身的
 * {@code aicyi:token} 键空间及其它测试互不干扰，无需跨用例清理 Redis 状态。
 *
 * @author Mr.Min
 */
@SpringBootTest(classes = AicyiExampleApplication.class)
class MultiRedisTokenServiceImplTest {

    /**
     * 本测试类专用的 Token 键空间前缀，避免与自动配置装配的 {@code aicyi:token} 键空间交叉裁剪
     */
    private static final String KEY_PREFIX = "aicyi-ut-multi";

    /**
     * 会话 key 中缀，与 {@code RedisTokenServiceImpl} 的键空间布局一致：{@code {keyPrefix}:session:{token}}
     */
    private static final String SESSION_KEY_INFIX = ":session:";

    /**
     * 主体索引 key 中缀：{@code {keyPrefix}:principal:{principalId}}（ZSet，score 为签发时刻）
     */
    private static final String PRINCIPAL_KEY_INFIX = ":principal:";

    /**
     * RefreshToken 有效期（小时）；足够长以保证用例执行期间不会自然过期
     */
    private static final long REFRESH_TTL = 1L;

    @Autowired
    private EnhancedRedisTemplateFactory factory;

    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        stringRedisTemplate = factory.getStringRedisTemplate();
    }

    // ------------------------------------------------------------------
    // 名额配置
    // ------------------------------------------------------------------

    @Test
    @DisplayName("构造器 - 名额开关与上限按入参回读，缺省构造为单 Token")
    void testConstructorsExposeQuotaConfig() {

        MultiRedisTokenServiceImpl<UserInfo> single = new MultiRedisTokenServiceImpl<>(
                stringRedisTemplate, UserInfo.class, REFRESH_TTL, TimeUnit.HOURS);

        assertFalse(single.isMultiTokenAllowed());
        assertEquals(1, single.getMultiTokenCount());

        MultiRedisTokenServiceImpl<UserInfo> multi = service(true, 3);

        assertTrue(multi.isMultiTokenAllowed());
        assertEquals(3, multi.getMultiTokenCount());
    }

    @Test
    @DisplayName("构造器 - 名额为 0 或负数时快速失败，避免裁剪脚本清空全部成员")
    void testConstructorRejectsNonPositiveQuota() {

        IllegalArgumentException zero = assertThrows(IllegalArgumentException.class,
                () -> service(true, 0));
        assertTrue(zero.getMessage().contains("multiTokenCount"));

        assertThrows(IllegalArgumentException.class, () -> service(true, -1));
    }

    @Test
    @DisplayName("不可变性 - 名额字段为 final 且不提供 setter，杜绝运行期篡改并发策略")
    void testQuotaFieldsAreImmutable() throws NoSuchFieldException {

        assertThrows(NoSuchMethodException.class,
                () -> MultiRedisTokenServiceImpl.class.getMethod("setMultiTokenAllowed", boolean.class));
        assertThrows(NoSuchMethodException.class,
                () -> MultiRedisTokenServiceImpl.class.getMethod("setMultiTokenCount", int.class));

        Field allowed = MultiRedisTokenServiceImpl.class.getDeclaredField("multiTokenAllowed");
        Field count = MultiRedisTokenServiceImpl.class.getDeclaredField("multiTokenCount");

        assertTrue(Modifier.isFinal(allowed.getModifiers()));
        assertTrue(Modifier.isFinal(count.getModifiers()));
    }

    // ------------------------------------------------------------------
    // 按设备计数的名额裁剪
    // ------------------------------------------------------------------

    @Test
    @DisplayName("关闭多端 - 名额恒为 1，新设备登录后旧设备被踢下线")
    void testMultiTokenNotAllowedKeepsSingleToken() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(false, 5);

        String first = service.create(request(principal(userId, "device-single-0")));
        String second = service.create(request(principal(userId, "device-single-1")));

        // multiTokenAllowed=false 时 maxTokens() 恒为 1，构造器传入的 5 不生效
        Set<String> tokens = service.getTokens(principal(userId, "device-probe"));

        assertEquals(1, tokens.size());
        assertTrue(tokens.contains(second));
        assertFalse(tokens.contains(first));
        assertFalse(service.isValid(first));
        assertTrue(service.isValid(second));
    }

    @Test
    @DisplayName("同设备重复登录 - 覆盖旧 Token 仅保留 1 个，且不挤占其它设备名额")
    void testSameDeviceRepeatedLoginCollapsesToOne() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 3);

        List<String> sameDevice = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            sameDevice.add(service.create(request(principal(userId, "device-same"))));
        }

        // 同设备覆盖后仅最新 Token 存活，最早的两个已被裁剪并失效
        assertEquals(1, service.getTokens(principal(userId, "device-same")).size());
        assertFalse(service.isValid(sameDevice.get(0)));
        assertFalse(service.isValid(sameDevice.get(1)));
        assertTrue(service.isValid(sameDevice.get(2)));

        // 覆盖不占用其它设备名额：另一设备登录后总数为 2（上限 3）
        String otherDevice = service.create(request(principal(userId, "device-other")));

        Set<String> tokens = service.getTokens(principal(userId, "device-probe"));

        assertEquals(2, tokens.size());
        assertTrue(tokens.contains(sameDevice.get(2)));
        assertTrue(tokens.contains(otherDevice));
    }

    @Test
    @DisplayName("名额超限 - 保留最近的名额数个 Token，最旧设备被踢下线且会话失效")
    void testQuotaOverflowEvictsOldestDevices() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 2);

        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tokens.add(service.create(request(principal(userId, "device-quota-" + i))));
        }

        Set<String> alive = service.getTokens(principal(userId, "device-probe"));

        assertEquals(2, alive.size());
        assertFalse(alive.contains(tokens.get(0)));
        assertFalse(alive.contains(tokens.get(1)));
        assertTrue(alive.contains(tokens.get(2)));
        assertTrue(alive.contains(tokens.get(3)));

        // 被裁剪成员的会话由 evictMembers 失效，旧 Token 不能继续用于鉴权或刷新
        assertFalse(service.isValid(tokens.get(0)));
        assertFalse(service.isValid(tokens.get(1)));
        assertThrows(TokenExpiredException.class, () -> service.refresh(tokens.get(0)));

        // 索引 ZSet 与存活集合一致，不存在残留成员
        assertEquals(2, zCard(userId));
    }

    @Test
    @DisplayName("deviceId 缺失 - 降级为按 Token 计数，索引成员不含设备前缀")
    void testMissingDeviceIdDegradesToPerTokenQuota() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 2);

        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            // deviceId 为 null：脚本跳过同设备覆盖，名额按 Token 计数
            tokens.add(service.create(request(principal(userId, null))));
        }

        Set<String> alive = service.getTokens(principal(userId, null));

        assertEquals(2, alive.size());
        assertFalse(alive.contains(tokens.get(0)));
        assertTrue(alive.contains(tokens.get(1)));
        assertTrue(alive.contains(tokens.get(2)));

        // 索引成员即纯 Token，不含 "|" 设备前缀
        for (String member : zRangeMembers(userId)) {
            assertFalse(member.contains("|"));
        }
    }

    @Test
    @DisplayName("deviceId 含分隔符 - 同设备仍能正确覆盖，还原出的 Token 可直接鉴权")
    void testDeviceIdContainingSeparatorStillResolvesToken() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 2);

        String first = service.create(request(principal(userId, "dev|ice-x")));
        String second = service.create(request(principal(userId, "dev|ice-x")));

        // tokenOf 取最后一个分隔符之后的内容，deviceId 自身含分隔符时仍还原出纯 Token
        Set<String> tokens = service.getTokens(principal(userId, "dev|ice-x"));

        assertEquals(1, tokens.size());
        assertTrue(tokens.contains(second));
        assertFalse(tokens.contains(first));
        assertTrue(service.isValid(second));
        assertEquals(userId, service.parsePrincipal(second).getUserId());

        // 前缀不同的设备不被误判为同设备，各自占用一个名额
        String otherDevice = service.create(request(principal(userId, "dev|ice-y")));

        assertEquals(2, service.getTokens(principal(userId, "device-probe")).size());
        assertTrue(service.isValid(otherDevice));
    }

    // ------------------------------------------------------------------
    // 幽灵成员回收
    // ------------------------------------------------------------------

    @Test
    @DisplayName("幽灵成员 - 会话已失效的索引成员被过滤并就地回收，索引不无界增长")
    void testGetTokensRecyclesGhostMembers() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 3);

        String first = service.create(request(principal(userId, "device-ghost-0")));
        String ghost = service.create(request(principal(userId, "device-ghost-1")));
        String third = service.create(request(principal(userId, "device-ghost-2")));

        assertEquals(3, zCard(userId));

        // 模拟会话自然过期：索引 TTL 已被后续登录刷新，成员却残留（幽灵成员）
        stringRedisTemplate.delete(sessionKey(ghost));

        Set<String> alive = service.getTokens(principal(userId, "device-probe"));

        assertEquals(2, alive.size());
        assertTrue(alive.contains(first));
        assertTrue(alive.contains(third));
        assertFalse(alive.contains(ghost));

        // 回收为就地 ZREM：索引成员数同步下降，而非仅在返回值中过滤
        assertEquals(2, zCard(userId));
    }

    // ------------------------------------------------------------------
    // 生命周期
    // ------------------------------------------------------------------

    @Test
    @DisplayName("create/parse - 主体与属性完整往返，剩余有效期为正")
    void testCreateAndParseRoundTrip() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 2);

        UserInfo principal = principal(userId, "device-round-trip");
        TokenCreateRequest<UserInfo> request = request(principal);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("role", "ADMIN");
        attributes.put("tenant", "aicyi");
        request.setAttributes(attributes);

        String token = service.create(request);

        assertNotNull(token);
        assertTrue(service.isValid(token));
        assertEquals(principal, service.parsePrincipal(token));

        Map<String, Object> parsed = service.parseAttributes(token);
        assertEquals("ADMIN", parsed.get("role"));
        assertEquals("aicyi", parsed.get("tenant"));

        String role = service.getAttribute(token, "role");
        assertEquals("ADMIN", role);
        assertNull(service.getAttribute(token, "not-exist-attribute"));

        assertTrue(service.getRemainingTtl(token, TimeUnit.MINUTES) > 0);
        assertTrue(service.getTokens(principal).contains(token));
    }

    @Test
    @DisplayName("refresh - 签发新 Token 并吊销旧 Token，同设备名额不增加")
    void testRefreshIssuesNewTokenAndRevokesOld() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 2);

        UserInfo principal = principal(userId, "device-refresh");
        String oldToken = service.create(request(principal));

        String newToken = service.refresh(oldToken);

        assertNotNull(newToken);
        assertFalse(newToken.equals(oldToken));
        assertTrue(service.isValid(newToken));
        assertFalse(service.isValid(oldToken));

        // 刷新沿用原会话的 principal 与 deviceId，故为同设备覆盖，名额不增加
        Set<String> tokens = service.getTokens(principal);
        assertEquals(1, tokens.size());
        assertTrue(tokens.contains(newToken));

        // 新 Token 继承原会话属性，且有效期按 refreshTtl 重新计算
        assertEquals(principal, service.parsePrincipal(newToken));
        assertTrue(service.getRemainingTtl(newToken, TimeUnit.MINUTES) > 0);

        assertThrows(TokenExpiredException.class, () -> service.refresh(oldToken));
    }

    @Test
    @DisplayName("revoke - 撤销单个 Token 并移出索引，重复撤销幂等")
    void testRevokeSingleTokenRemovesFromIndex() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 3);

        UserInfo principal = principal(userId, "device-revoke-0");
        String revoked = service.create(request(principal));
        String retained = service.create(request(principal(userId, "device-revoke-1")));

        assertEquals(2, zCard(userId));

        service.revoke(revoked);

        assertFalse(service.isValid(revoked));
        assertFalse(stringRedisTemplate.hasKey(sessionKey(revoked)));

        Set<String> tokens = service.getTokens(principal);
        assertEquals(1, tokens.size());
        assertTrue(tokens.contains(retained));
        assertEquals(1, zCard(userId));

        // 幂等：会话已不存在时静默返回，不抛异常
        service.revoke(revoked);
        assertEquals(1, zCard(userId));
    }

    @Test
    @DisplayName("revokeAll - 踢下线清空该主体全部会话与索引，重复调用幂等")
    void testRevokeAllClearsSessionsAndIndex() {

        long userId = newUserId();
        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 3);

        UserInfo principal = principal(userId, "device-kick-0");
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tokens.add(service.create(request(principal(userId, "device-kick-" + i))));
        }

        assertEquals(3, zCard(userId));

        service.revokeAll(principal);

        assertTrue(service.getTokens(principal).isEmpty());
        assertFalse(stringRedisTemplate.hasKey(indexKey(userId)));
        for (String token : tokens) {
            assertFalse(service.isValid(token));
            assertFalse(stringRedisTemplate.hasKey(sessionKey(token)));
        }

        // 幂等：无有效 Token 时静默返回
        service.revokeAll(principal);
        assertTrue(service.getTokens(principal).isEmpty());
    }

    // ------------------------------------------------------------------
    // 键空间隔离
    // ------------------------------------------------------------------

    @Test
    @DisplayName("keyPrefix - 不同前缀的键空间互不裁剪、互不踢下线")
    void testKeyPrefixIsolatesKeySpaces() {

        long userId = newUserId();

        MultiRedisTokenServiceImpl<UserInfo> appA = prefixedService("aicyi-ut-prefix-a", true, 1);
        MultiRedisTokenServiceImpl<UserInfo> appB = prefixedService("aicyi-ut-prefix-b", true, 1);

        UserInfo principal = principal(userId, "device-prefix");

        String tokenA = appA.create(request(principal));
        String tokenB = appB.create(request(principal));

        // 名额均为 1，但键空间隔离使两侧互不裁剪（共享 Redis 的多应用不会互踢下线）
        assertTrue(appA.getTokens(principal).contains(tokenA));
        assertTrue(appB.getTokens(principal).contains(tokenB));
        assertTrue(appA.isValid(tokenA));
        assertTrue(appB.isValid(tokenB));

        appA.revokeAll(principal);

        assertFalse(appA.isValid(tokenA));
        assertTrue(appB.isValid(tokenB));
        assertTrue(appB.getTokens(principal).contains(tokenB));
    }

    // ------------------------------------------------------------------
    // 异常契约
    // ------------------------------------------------------------------

    @Test
    @DisplayName("异常契约 - 无效 Token 收敛为 false，过期出 40102，入参非法出 IllegalArgumentException")
    void testTokenExceptionContracts() {

        MultiRedisTokenServiceImpl<UserInfo> service = service(true, 2);

        long userId = newUserId();
        UserInfo principal = principal(userId, "device-contract");

        String token = service.create(request(principal));
        service.revoke(token);

        // isValid 是唯一不抛 Token 异常的读取入口：null / 空白 / 不存在 / 已撤销一律 false
        assertFalse(service.isValid(null));
        assertFalse(service.isValid("  "));
        assertFalse(service.isValid("not-exist-token"));
        assertFalse(service.isValid(token));

        // 会话不存在按"已过期"出码，前端据此触发重新登录而非重试刷新
        assertThrows(TokenExpiredException.class, () -> service.parsePrincipal("not-exist-token"));
        assertThrows(TokenExpiredException.class, () -> service.getRemainingTtl(token, TimeUnit.MINUTES));
        assertThrows(TokenExpiredException.class, () -> service.parseAttributes(token));

        // 入参非法属调用方使用错误，快速失败
        assertThrows(IllegalArgumentException.class, () -> service.create(null));
        assertThrows(IllegalArgumentException.class, () -> service.create(new TokenCreateRequest<>()));
        assertThrows(IllegalArgumentException.class, () -> service.getTokens(null));
        assertThrows(IllegalArgumentException.class, () -> service.revokeAll(null));
    }

    // ------------------------------------------------------------------
    // 测试夹具
    // ------------------------------------------------------------------

    /**
     * 构建被测服务，统一使用本测试类专用 {@link #KEY_PREFIX}
     *
     * @param multiTokenAllowed 是否开启多设备名额
     * @param multiTokenCount   名额上限（按设备计数）
     */
    private MultiRedisTokenServiceImpl<UserInfo> service(boolean multiTokenAllowed, int multiTokenCount) {
        return prefixedService(KEY_PREFIX, multiTokenAllowed, multiTokenCount);
    }

    private MultiRedisTokenServiceImpl<UserInfo> prefixedService(String keyPrefix,
                                                                 boolean multiTokenAllowed,
                                                                 int multiTokenCount) {

        return new MultiRedisTokenServiceImpl<>(
                stringRedisTemplate,
                UserInfo.class,
                REFRESH_TTL,
                TimeUnit.HOURS,
                keyPrefix,
                multiTokenAllowed,
                multiTokenCount
        );
    }

    private TokenCreateRequest<UserInfo> request(UserInfo principal) {

        TokenCreateRequest<UserInfo> request = new TokenCreateRequest<>();
        request.setPrincipal(principal);
        request.setTtl(REFRESH_TTL);
        request.setTimeUnit(TimeUnit.HOURS);
        return request;
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

    private String indexKey(long userId) {
        return KEY_PREFIX + PRINCIPAL_KEY_INFIX + userId;
    }

    private String sessionKey(String token) {
        return KEY_PREFIX + SESSION_KEY_INFIX + token;
    }

    /**
     * 直接读取索引 ZSet 成员数，用于断言"就地回收"而非仅在返回值中过滤
     */
    private long zCard(long userId) {
        Long size = stringRedisTemplate.opsForZSet().zCard(indexKey(userId));
        return size == null ? 0L : size;
    }

    private Set<String> zRangeMembers(long userId) {
        Set<String> members = stringRedisTemplate.opsForZSet().range(indexKey(userId), 0, -1);
        return members == null ? Set.of() : members;
    }
}
