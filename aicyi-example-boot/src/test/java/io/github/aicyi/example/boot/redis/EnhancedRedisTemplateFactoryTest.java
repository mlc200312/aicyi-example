
package io.github.aicyi.example.boot.redis;

import io.github.aicyi.commons.core.codec.JsonCodec;
import io.github.aicyi.commons.util.jackson.JacksonJsonCodec;
import io.github.aicyi.example.boot.AicyiExampleApplication;
import io.github.aicyi.example.domain.StudentBean;
import io.github.aicyi.example.domain.UserBean;
import io.github.aicyi.midware.redis.EnhancedRedisTemplateFactory;
import io.github.aicyi.midware.redis.JsonCodecRedisSerializer;
import io.github.aicyi.midware.redis.SerializerType;
import io.github.aicyi.example.fixture.util.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Type;
import java.util.List;

/**
 * EnhancedRedisTemplateFactory 测试类
 * <p>
 * 测试覆盖：
 * 1. StringRedisTemplate 缓存复用
 * 2. JSON RedisTemplate 缓存复用
 * 3. Type Template 创建
 * 4. Generic JSON Template
 * 5. SerializerType 路由
 * 6. Serializer 配置正确性
 * 7. ConnectionFactory 注入正确性
 * 8. Cache clear 行为
 *
 * @author Mr.Min
 */
@ExtendWith(MockitoExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AicyiExampleApplication.class)
class EnhancedRedisTemplateFactoryTest {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private JsonCodec jsonCodec;

    private EnhancedRedisTemplateFactory factory;

    private StringRedisTemplate stringRedisTemplate;

    private UserBean user;

    @BeforeEach
    void setUp() {
        jsonCodec = JacksonJsonCodec.DEFAULT;
        factory = new EnhancedRedisTemplateFactory(redisConnectionFactory, jsonCodec);
        stringRedisTemplate = factory.getStringRedisTemplate();
        user = DataSource.getUser();
    }

    // =========================================================
    // STRING TEMPLATE
    // =========================================================

    @Test
    @DisplayName("StringRedisTemplate 应该缓存复用")
    void shouldReuseStringRedisTemplate() {

        StringRedisTemplate template1 = factory.getStringRedisTemplate();
        StringRedisTemplate template2 = factory.getStringRedisTemplate();

        Assertions.assertSame(template1, template2);
    }

    @Test
    @DisplayName("StringRedisTemplate ConnectionFactory 应正确注入")
    void shouldInjectConnectionFactoryForStringTemplate() {

        StringRedisTemplate template = factory.getStringRedisTemplate();

        Assertions.assertSame(redisConnectionFactory, template.getConnectionFactory());
    }

    // =========================================================
    // JSON TEMPLATE（CLASS）
    // =========================================================

    @Test
    @DisplayName("JSON RedisTemplate(Class) 应该缓存复用")
    void shouldReuseJsonRedisTemplateByClass() {

        RedisTemplate<String, UserBean> template1 = factory.getJsonRedisTemplate(UserBean.class);

        RedisTemplate<String, UserBean> template2 = factory.getJsonRedisTemplate(UserBean.class);

        Assertions.assertSame(template1, template2);
    }

    @Test
    @DisplayName("不同 Class 应创建不同 RedisTemplate")
    void shouldCreateDifferentTemplateForDifferentClass() {

        RedisTemplate<String, UserBean> userTemplate = factory.getJsonRedisTemplate(UserBean.class);

        RedisTemplate<String, StudentBean> studentTemplate = factory.getJsonRedisTemplate(StudentBean.class);

        Assertions.assertNotSame(userTemplate, studentTemplate);
    }

    @Test
    @DisplayName("JSON RedisTemplate 应正确设置 serializer")
    void shouldConfigureJsonSerializerCorrectly() {

        RedisTemplate<String, UserBean> template = factory.getJsonRedisTemplate(UserBean.class);

        Assertions.assertNotNull(template.getValueSerializer());
        Assertions.assertNotNull(template.getHashValueSerializer());

        Assertions.assertSame(
                template.getValueSerializer(),
                template.getHashValueSerializer()
        );
    }

    @Test
    @DisplayName("JSON RedisTemplate KeySerializer 应为 StringSerializer")
    void shouldUseStringSerializerForKey() {

        RedisTemplate<String, UserBean> template = factory.getJsonRedisTemplate(UserBean.class);

        RedisSerializer<?> keySerializer = template.getKeySerializer();

        Assertions.assertNotNull(keySerializer);
        Assertions.assertEquals(
                RedisSerializer.string().getClass(),
                keySerializer.getClass()
        );
    }

    @Test
    @DisplayName("JSON RedisTemplate ConnectionFactory 应正确注入")
    void shouldInjectConnectionFactoryForJsonTemplate() {

        RedisTemplate<String, UserBean> template = factory.getJsonRedisTemplate(UserBean.class);

        Assertions.assertSame(redisConnectionFactory, template.getConnectionFactory());
    }

    // =========================================================
    // JSON TEMPLATE（TYPE）
    // =========================================================

    @Test
    @DisplayName("Type RedisTemplate 应正常创建")
    void shouldCreateJsonRedisTemplateByType() {

        Type type = new com.fasterxml.jackson.core.type.TypeReference<List<StudentBean>>() {
        }.getType();

        RedisTemplate<String, List<StudentBean>> template = factory.getJsonRedisTemplate(type);

        Assertions.assertNotNull(template);
        Assertions.assertNotNull(template.getValueSerializer());
    }

    @Test
    @DisplayName("Type RedisTemplate 应缓存复用")
    void shouldNotReuseJsonRedisTemplateByType() {

        Type type = new com.fasterxml.jackson.core.type.TypeReference<List<UserBean>>() {
        }.getType();

        RedisTemplate<String, List<UserBean>> template1 = factory.getJsonRedisTemplate(type);

        RedisTemplate<String, List<UserBean>> template2 = factory.getJsonRedisTemplate(type);

        Assertions.assertSame(template1, template2);
    }

    // =========================================================
    // GENERIC JSON TEMPLATE
    // =========================================================

    @Test
    @DisplayName("Generic JSON Template 应缓存复用")
    void shouldReuseGenericJsonRedisTemplate() {

        RedisTemplate<String, Object> template1 = factory.getGenericJsonRedisTemplate();

        RedisTemplate<String, Object> template2 = factory.getGenericJsonRedisTemplate();

        Assertions.assertSame(template1, template2);
    }

    @Test
    @DisplayName("Generic JSON Template 应使用 GenericJackson2JsonRedisSerializer")
    void shouldUseGenericJacksonSerializer() {

        RedisTemplate<String, Object> template = factory.getGenericJsonRedisTemplate();

        Assertions.assertInstanceOf(
                JsonCodecRedisSerializer.class,
                template.getValueSerializer()
        );
    }

    // =========================================================
    // SERIALIZER TYPE
    // =========================================================

    @Test
    @DisplayName("JDK Template 应使用 JDK Serializer")
    void shouldUseJdkSerializer() {

        RedisTemplate<String, Object> template = factory.getRedisTemplate(SerializerType.JDK);

        Assertions.assertInstanceOf(
                JdkSerializationRedisSerializer.class,
                template.getValueSerializer()
        );
    }

    @Test
    @DisplayName("JSON Template 应使用 Generic JSON Serializer")
    void shouldUseJsonSerializer() {

        RedisTemplate<String, Object> template = factory.getRedisTemplate(SerializerType.JSON);

        Assertions.assertInstanceOf(
                JsonCodecRedisSerializer.class,
                template.getValueSerializer()
        );
    }

    @Test
    @DisplayName("相同 SerializerType 应缓存复用")
    void shouldReuseTemplateBySerializerType() {

        RedisTemplate<String, Object> template1 = factory.getRedisTemplate(SerializerType.JSON);

        RedisTemplate<String, Object> template2 = factory.getRedisTemplate(SerializerType.JSON);

        Assertions.assertSame(template1, template2);
    }

    // =========================================================
    // CACHE
    // =========================================================

    @Test
    @DisplayName("clearCache 后应该重新创建 Template")
    void shouldCreateNewTemplateAfterClearCache() {

        RedisTemplate<String, UserBean> template1 = factory.getJsonRedisTemplate(UserBean.class);

        factory.clearCache();

        RedisTemplate<String, UserBean> template2 = factory.getJsonRedisTemplate(UserBean.class);

        Assertions.assertNotSame(template1, template2);
    }

    // =========================================================
    // GETTER
    // =========================================================

    @Test
    @DisplayName("应该返回正确的 RedisConnectionFactory")
    void shouldReturnCorrectRedisConnectionFactory() {

        Assertions.assertSame(
                redisConnectionFactory,
                factory.getRedisConnectionFactory()
        );
    }

    @Test
    @DisplayName("应该返回正确的 JsonCodec")
    void shouldReturnCorrectJsonCodec() {

        Assertions.assertSame(jsonCodec, factory.getJsonCodec());
    }

    @Test
    @DisplayName("应该正确缓存并获取")
    void getJsonRedisTemplate() {
        RedisTemplate<String, UserBean> template = factory.getJsonRedisTemplate(UserBean.class);

        BoundValueOperations<String, UserBean> valueOps = template.boundValueOps("testJson");

        valueOps.set(user);

        UserBean userBean = valueOps.get();

        String string = stringRedisTemplate.opsForValue().get("testJson");

        Assertions.assertEquals(userBean, user);

        System.out.println(string);
    }

    @Test
    @DisplayName("getJsonRedisTemplate")
    void getJsonRedisTemplate2() {
        Type type = new com.fasterxml.jackson.core.type.TypeReference<List<StudentBean>>() {
        }.getType();
        RedisTemplate<String, List<StudentBean>> template = factory.getJsonRedisTemplate(type);

        BoundValueOperations<String, List<StudentBean>> valueOps = template.boundValueOps("testJsonList");

        List<StudentBean> studentList = DataSource.getStudentList();

        valueOps.set(studentList);

        List<StudentBean> studentBeanList = valueOps.get();

        String string = stringRedisTemplate.opsForValue().get("testJsonList");

        Assertions.assertEquals(studentList, studentBeanList);

        System.out.println(string);
    }

    @Test
    @DisplayName("getGenericJsonRedisTemplate")
    void getGenericJsonRedisTemplate() {

        RedisTemplate<String, Object> template = factory.getGenericJsonRedisTemplate();

        BoundValueOperations<String, Object> valueOps = template.boundValueOps("testGenericJson");

        valueOps.set(user);

        Object object = valueOps.get();

        String string = stringRedisTemplate.opsForValue().get("testGenericJson");

        Assertions.assertNotNull(object);
        Assertions.assertNotEquals(object, user);

        System.out.println(string);
    }

    @Test
    @DisplayName("getRedisTemplate")
    void getRedisTemplateTest() {

        RedisTemplate<String, Object> template = factory.getRedisTemplate(SerializerType.JSON);

        BoundValueOperations<String, Object> valueOps = template.boundValueOps("testJsonObject");

        valueOps.set(user);

        Object object = valueOps.get();

        String string = stringRedisTemplate.opsForValue().get("testJsonObject");

        Assertions.assertNotNull(object);
        Assertions.assertNotEquals(object, user);

        System.out.println(string);
    }

    @Test
    @DisplayName("getRedisTemplate")
    void getRedisTemplateTest2() {

        RedisTemplate<String, Object> template = factory.getRedisTemplate(SerializerType.JDK);

        BoundValueOperations<String, Object> valueOps = template.boundValueOps("testJdk");

        valueOps.set(user);

        Object object = valueOps.get();

        String string = stringRedisTemplate.opsForValue().get("testJdk");

        Assertions.assertNotNull(object);
        Assertions.assertEquals(object, user);

        System.out.println(string);
    }
}

