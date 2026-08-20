package io.github.aicyi.example.boot.redis;

import io.github.aicyi.commons.core.cache.Cache;
import io.github.aicyi.commons.core.cache.CacheConfig;
import io.github.aicyi.commons.core.cache.CacheLock;
import io.github.aicyi.commons.core.cache.DistributedCacheLock;
import io.github.aicyi.commons.core.lock.DistributedLockManager;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.midware.message.core.model.MessageTemplate;
import io.github.aicyi.midware.message.core.template.TemplateProvider;
import io.github.aicyi.midware.redis.EnhancedRedisTemplateFactory;
import io.github.aicyi.commons.util.serializer.CacheWrapperCodec;
import io.github.aicyi.midware.redis.cache.RedisCache;
import io.github.aicyi.midware.redis.cache.RedisCacheConfig;
import io.github.aicyi.example.fixture.util.BaseLoggerTest;
import org.apache.commons.collections4.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mr.Min
 * @description 业务描述
 * @date 11:13
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
public class RedisCacheTest extends BaseLoggerTest {

    @Autowired
    private EnhancedRedisTemplateFactory templateFactory;
    @Autowired
    private DistributedLockManager distributedLockManager;
    @Autowired
    private TemplateProvider templateProvider;
    @Autowired
    private Cache<String, String> cache;

    private RedisCache<MessageTemplate> redisCache;

    @Before
    @Override
    public void beforeTest() {
        cache.clear();

        StringRedisTemplate stringRedisTemplate = templateFactory.getStringRedisTemplate();

        CacheConfig cacheConfig = RedisCacheConfig.builder()
                .globalPrefix("aicyi.cache")
                .cacheName("message_template")
                .ttl(Duration.ofDays(1))
                .cacheNull(false)
                .build();

        CacheLock cacheLock = new DistributedCacheLock(distributedLockManager);

        redisCache = new RedisCache<>(
                stringRedisTemplate,
                cacheConfig,
                new CacheWrapperCodec<>(MessageTemplate.class),
                cacheLock
        );
    }

    @Test
    public void test() {
        cache.put("test_put", "tes put");
        Object testPut = cache.get("test_put");
        assert testPut.equals("tes put");

        boolean exists = cache.exists("test_put");
        assert exists;

        cache.evict("test_put");
        Object evict = cache.get("test_put");
        assert evict == null;

        boolean exists1 = cache.exists("test_put");
        assert !exists1;

        Object testPut01 = cache.get("test_put_01", key -> {
            System.out.println("load key: " + key);
            return "haha haha";
        });
        assert testPut01 == "haha haha";

        log(cache.stats().hitRate());
    }

    @Test
    public void test2() {
        HashMap<String, String> map = new HashMap<>();
        map.put("test_put_all_01", "test put all 01");
        map.put("test_put_all_02", "test put all 02");
        map.put("test_put_all_03", "test put all 03");

        cache.putAll(map);

        Map<String, String> testPutAll = cache.getAll(map.keySet());

        assert testPutAll.size() == 3;

        long cnt = cache.evictBatch(map.keySet());

        Map<String, String> evictAll = cache.getAll(map.keySet());

        assert MapUtils.isEmpty(evictAll);

        log(cnt, cache.stats().hitRate());
    }

    @Test
    public void test3() {

        MessageTemplate template = templateProvider.getTemplate("SMS_DELIVERY_NOTICE");

        redisCache.put("123", template);

        MessageTemplate messageTemplate = redisCache.get("123");

        log(messageTemplate);
    }

    @Test
    public void test4() {

        redisCache.put("123", null);

        MessageTemplate messageTemplate = redisCache.get("123");

        MessageTemplate messageTemplate1 = redisCache.get("123", key -> templateProvider.getTemplate("SMS_DELIVERY_NOTICE"));

        MessageTemplate messageTemplate2 = redisCache.get("1234", key -> null);

        log(messageTemplate, messageTemplate1, messageTemplate2);
    }
}
