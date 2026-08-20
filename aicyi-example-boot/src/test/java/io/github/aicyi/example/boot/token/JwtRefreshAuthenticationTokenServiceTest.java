package io.github.aicyi.example.boot.token;

import io.github.aicyi.commons.core.mapper.BeanMapper;
import io.github.aicyi.commons.core.token.TokenPair;
import io.github.aicyi.commons.security.SecretKeyUtils;
import io.github.aicyi.commons.core.token.AuthenticationTokenService;
import io.github.aicyi.commons.util.Maps;
import io.github.aicyi.commons.util.UUIDUtils;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.domain.UserBean;
import io.github.aicyi.example.domain.UserInfo;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.midware.redis.EnhancedRedisTemplateFactory;
import io.github.aicyi.midware.redis.token.AuthenticationConfig;
import io.github.aicyi.midware.redis.token.JwtRefreshAuthenticationTokenService;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import io.github.aicyi.example.fixture.util.DataSource;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 17:29
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class JwtRefreshAuthenticationTokenServiceTest extends BaseLoggerTest {

    @Autowired
    private EnhancedRedisTemplateFactory factory;

    @Autowired
    private BeanMapper beanMapper;

    private AuthenticationTokenService<UserInfo> authenticationTokenService;

    @Before
    @Override
    public void beforeTest() {

        StringRedisTemplate stringRedisTemplate = factory.getStringRedisTemplate();

        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

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

        authenticationTokenService = new JwtRefreshAuthenticationTokenService<>(config, stringRedisTemplate, UserInfo.class);
    }

    @Test
    @Override
    public void test() {

        UserBean userBean = DataSource.getUser();

        User user = beanMapper.map(userBean, User.class);

        UserInfo userInfo = UserInfo.of(user, UUIDUtils.generateV7Id());

        Map<String, Object> attributes = Maps.ofStr("test", "test").build();

        TokenPair token = authenticationTokenService.createToken(userInfo, attributes);

        TokenPair refreshToken = authenticationTokenService.refreshToken(token.getRefreshToken());

        boolean validated = authenticationTokenService.validateAccessToken(token.getAccessToken());

        assert validated;

        UserInfo principal = authenticationTokenService.parsePrincipal(token.getAccessToken());

        assert principal != null;

        attributes = authenticationTokenService.getAttributes(token.getAccessToken());

        authenticationTokenService.revokeToken(token.getRefreshToken());

        try {
            authenticationTokenService.refreshToken(token.getRefreshToken());
            assert false;
        } catch (Exception e) {
            assert true;
        }

        log(token, refreshToken, principal, attributes);
    }

    @Test
    public void test2() {
        // 模拟多设备登录

        UserBean userBean = DataSource.getUser();

        User user = beanMapper.map(userBean, User.class);

        UserInfo userInfo = UserInfo.of(user, UUIDUtils.generateV7Id());

        List<String> tokenList = new ArrayList<>();

        List<String> deviceIdList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {

            TokenPair token = authenticationTokenService.createToken(userInfo, Maps.ofStr("deviceId", "设备：" + i).build());

            Map<String, Object> attributes = authenticationTokenService.getAttributes(token.getAccessToken());

            tokenList.add(token.getRefreshToken());

            deviceIdList.add(attributes.get("deviceId").toString());
        }

        Set<String> refreshTokens = authenticationTokenService.getRefreshTokens(userInfo);

        List<String> deviceIdList2 = new ArrayList<>();

        for (String refreshToken : refreshTokens) {

            TokenPair tokenPair = authenticationTokenService.refreshToken(refreshToken);

            Map<String, Object> attributes = authenticationTokenService.getAttributes(tokenPair.getAccessToken());

            deviceIdList2.add(attributes.get("deviceId").toString());
        }

        assert refreshTokens.size() == 2;

        log(tokenList, deviceIdList, refreshTokens, deviceIdList2);
    }
}
