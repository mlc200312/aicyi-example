package io.github.aicyi.example.boot.redis;

import io.github.aicyi.commons.core.token.TokenCreateRequest;
import io.github.aicyi.commons.core.token.TokenService;
import io.github.aicyi.commons.util.map.Maps;
import io.github.aicyi.commons.util.id.UUIDUtils;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.domain.UserInfo;
import io.github.aicyi.midware.redis.template.EnhancedRedisTemplateFactory;
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

    private UserInfo jwtInfo;
    private TokenCreateRequest<UserInfo> request;
    private TokenService<String, UserInfo> tokenService;

    @Before
    @Override
    public void beforeTest() {
        jwtInfo = new UserInfo();
        jwtInfo.setUserId(610780341698822144L);
        jwtInfo.setUsername("张三");
        jwtInfo.setDeviceId(UUIDUtils.generateV7Id());

        request = new TokenCreateRequest<>();

        request.setPrincipal(jwtInfo);
        request.setAttributes(Maps.ofStr("phone", RandomGenerator.generatePhoneNum()).build());
        request.setTtl(1);
        request.setTimeUnit(TimeUnit.HOURS);

        long refreshTtl = 3;
        TimeUnit refreshTimeUnit = TimeUnit.HOURS;

        MultiRedisTokenServiceImpl<UserInfo> multiRedisTokenService = new MultiRedisTokenServiceImpl<>(factory.getStringRedisTemplate(), UserInfo.class, refreshTtl, refreshTimeUnit);
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
        UserInfo principal = tokenService.parsePrincipal(refresh);
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

        MultiRedisTokenServiceImpl<UserInfo> multiRedisTokenService = (MultiRedisTokenServiceImpl<UserInfo>) tokenService;
        multiRedisTokenService.setMultiTokenAllowed(true);
        multiRedisTokenService.setMultiTokenCount(2);

        List<String> tokenList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {

            UserInfo principal = request.getPrincipal();
            principal.setDeviceId("设备信息-" + i);

            String token = multiRedisTokenService.create(request);

            tokenList.add(token);
        }

        String first = tokenList.get(0);

        Set<String> tokens = multiRedisTokenService.getTokens(request.getPrincipal());

        assert tokens.size() == multiRedisTokenService.getMultiTokenCount() && !tokens.contains(first);

        List<UserInfo> principals = new ArrayList<>();
        for (String token : tokens) {

            UserInfo principal = multiRedisTokenService.parsePrincipal(token);

            principals.add(principal);
        }

        log(tokens, principals);
    }

    @Test
    public void test3() {

        tokenService.revokeAll(request.getPrincipal());
    }
}
