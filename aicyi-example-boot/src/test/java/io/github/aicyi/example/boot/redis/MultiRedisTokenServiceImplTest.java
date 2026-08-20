package io.github.aicyi.example.boot.redis;

import io.github.aicyi.commons.core.token.TokenCreateRequest;
import io.github.aicyi.commons.core.token.TokenService;
import io.github.aicyi.commons.security.token.jwt.JWTInfo;
import io.github.aicyi.commons.util.Maps;
import io.github.aicyi.commons.util.UUIDUtils;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.midware.redis.EnhancedRedisTemplateFactory;
import io.github.aicyi.midware.redis.token.MultiRedisTokenServiceImpl;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import io.github.aicyi.example.fixture.util.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 19:53
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class MultiRedisTokenServiceImplTest extends BaseLoggerTest {

    @Autowired
    private EnhancedRedisTemplateFactory factory;

    private JWTInfo jwtInfo;
    private TokenCreateRequest<JWTInfo> request;
    private TokenService<String, JWTInfo> tokenService;

    @Before
    @Override
    public void beforeTest() {
        jwtInfo = new JWTInfo();
        jwtInfo.setId("610780341698822144");
        jwtInfo.setUniqueName("张三");
        jwtInfo.setDeviceId(UUIDUtils.generateV7Id());

        request = new TokenCreateRequest<>();

        request.setPrincipal(jwtInfo);
        request.setAttributes(Maps.ofStr("phone", RandomGenerator.generatePhoneNum()).build());
        request.setTtl(1);
        request.setTimeUnit(TimeUnit.HOURS);

        long refreshTtl = 3;
        TimeUnit refreshTimeUnit = TimeUnit.HOURS;

        MultiRedisTokenServiceImpl<JWTInfo> multiRedisTokenService = new MultiRedisTokenServiceImpl<>(factory.getStringRedisTemplate(), JWTInfo.class, refreshTtl, refreshTimeUnit);
        multiRedisTokenService.setMultiTokenAllowed(true);
        multiRedisTokenService.setMultiTokenCount(3);

        tokenService = multiRedisTokenService;
    }

    @Test
    public void test() {
        String token = tokenService.create(request);
        Long ttl = tokenService.getRemainingTtl(token, TimeUnit.MINUTES);
        assert ttl > 0;

        boolean isValid = tokenService.isValid(token);
        assert isValid;

        String refresh = tokenService.refresh(token);
        JWTInfo principal = tokenService.parsePrincipal(refresh);
        assert principal.getId().equals(jwtInfo.getId());

        Map<String, Object> attributes = tokenService.parseAttributes(refresh);
        assert attributes.containsKey("phone");

        String phone = tokenService.getAttribute(refresh, "phone");
        assert StringUtils.isNotBlank(phone);

        Long refreshTtl = tokenService.getRemainingTtl(refresh, TimeUnit.MINUTES);
        assert refreshTtl > 0 && refreshTtl > ttl;

        Set<String> tokens = tokenService.getTokens(jwtInfo);
        assert tokens.contains(refresh);

        log(token, refresh, principal, phone, ttl, refreshTtl);
    }

    @Test
    public void test2() {
        // 模拟多设备登录

        MultiRedisTokenServiceImpl<JWTInfo> multiRedisTokenService = (MultiRedisTokenServiceImpl<JWTInfo>) tokenService;
        multiRedisTokenService.setMultiTokenAllowed(true);
        multiRedisTokenService.setMultiTokenCount(2);

        List<String> tokenList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {

            JWTInfo principal = request.getPrincipal();
            principal.setDeviceId("设备信息-" + i);

            String token = multiRedisTokenService.create(request);

            tokenList.add(token);
        }

        String first = tokenList.get(0);

        Set<String> tokens = multiRedisTokenService.getTokens(request.getPrincipal());

        assert tokens.size() == multiRedisTokenService.getMultiTokenCount() && !tokens.contains(first);

        List<JWTInfo> principals = new ArrayList<>();
        for (String token : tokens) {

            JWTInfo principal = multiRedisTokenService.parsePrincipal(token);

            principals.add(principal);
        }

        log(tokens, principals);
    }

    @Test
    public void test3() {

        tokenService.revokeAll(request.getPrincipal());
    }
}
