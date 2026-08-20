package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.core.mapper.BeanMapper;
import io.github.aicyi.commons.util.MapperUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MapperUtils} 测试类（Orika实现）
 */
@DisplayName("MapperUtils 对象映射测试")
public class MapperUtilsTest {

    @Getter
    @Setter
    public static class Source {
        private Long id;
        private String name;
        private int age;
    }

    @Getter
    @Setter
    public static class Target {
        private Long id;
        private String name;
        private int age;
    }

    @Test
    @DisplayName("getInstance 返回全局单例")
    public void testGetInstance() {
        BeanMapper mapper = MapperUtils.getInstance();

        assertNotNull(mapper);
        assertSame(MapperUtils.getInstance(), mapper);
    }

    @Test
    @DisplayName("map 同名属性映射")
    public void testMap() {
        Source source = new Source();
        source.setId(1L);
        source.setName("Tom");
        source.setAge(18);

        Target target = MapperUtils.getInstance().map(source, Target.class);

        assertNotNull(target);
        assertEquals(1L, target.getId());
        assertEquals("Tom", target.getName());
        assertEquals(18, target.getAge());
    }

    @Test
    @DisplayName("map 映射到已有对象")
    public void testMapToExistingTarget() {
        Source source = new Source();
        source.setName("Tom");

        Target target = new Target();
        MapperUtils.getInstance().map(source, target);

        assertEquals("Tom", target.getName());
    }

    @Test
    @DisplayName("mapList 集合映射")
    public void testMapList() {
        Source s1 = new Source();
        s1.setName("a");
        Source s2 = new Source();
        s2.setName("b");

        List<Target> targets = MapperUtils.getInstance()
                .mapList(Arrays.asList(s1, s2), Target.class);

        assertEquals(2, targets.size());
        assertEquals("a", targets.get(0).getName());
        assertEquals("b", targets.get(1).getName());
    }

    @Test
    @DisplayName("map null源返回null")
    public void testMapNull() {
        assertNull(MapperUtils.getInstance().map(null, Target.class));
    }
}
