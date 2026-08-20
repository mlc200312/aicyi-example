package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.BeanCglibCopier;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BeanCglibCopier} 测试类
 */
@DisplayName("BeanCglibCopier Cglib拷贝测试")
public class BeanCglibCopierTest {

    @Test
    @DisplayName("同名同类型属性拷贝")
    public void testCopy() {
        Source source = new Source();
        source.setName("Tom");
        source.setAge(18);

        Target target = BeanCglibCopier.copy(source, Target.class);

        assertNotNull(target);
        assertEquals("Tom", target.getName());
        assertEquals(18, target.getAge());
    }

    @Test
    @DisplayName("重复拷贝走缓存且结果一致")
    public void testCopyCached() {
        Source source = new Source();
        source.setName("Tom");
        source.setAge(18);

        Target first = BeanCglibCopier.copy(source, Target.class);
        Target second = BeanCglibCopier.copy(source, Target.class);

        assertNotSame(first, second);
        assertEquals(first.getName(), second.getName());
        assertEquals(first.getAge(), second.getAge());
    }

    @Test
    @DisplayName("source/target为null抛IllegalArgumentException")
    public void testNullArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> BeanCglibCopier.copy(null, Target.class));

        assertThrows(IllegalArgumentException.class,
                () -> BeanCglibCopier.copy(new Source(), null));
    }

    @Test
    @DisplayName("目标无无参构造时抛IllegalArgumentException")
    public void testTargetWithoutNoArgConstructor() {
        assertThrows(IllegalArgumentException.class,
                () -> BeanCglibCopier.copy(new Source(), NoDefaultConstructor.class));
    }

    @Getter
    @Setter
    public static class Source {
        private String name;
        private int age;
        private String extra = "only-source";
    }

    @Getter
    @Setter
    public static class Target {
        private String name;
        private int age;
    }

    @Getter
    @Setter
    public static class NoDefaultConstructor {
        private final String name;

        public NoDefaultConstructor(String name) {
            this.name = name;
        }
    }
}
