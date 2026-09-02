package io.github.aicyi.example.test.util;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MapStruct 对象映射测试（编译期生成，取代原 Orika 实现）
 *
 * @author Mr.Min
 * @date 2026-09-02
 */
@DisplayName("MapStruct 对象映射测试")
public class MapStructMapperTest {

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

    @Mapper
    public interface SourceTargetMapper {

        SourceTargetMapper INSTANCE = Mappers.getMapper(SourceTargetMapper.class);

        Target toTarget(Source source);

        void updateTarget(Source source, @MappingTarget Target target);

        List<Target> toTargetList(List<Source> sources);
    }

    @Test
    @DisplayName("Mappers.getMapper 返回全局单例")
    public void testGetInstance() {
        SourceTargetMapper mapper = SourceTargetMapper.INSTANCE;

        assertNotNull(mapper);
        assertSame(SourceTargetMapper.INSTANCE, mapper);
    }

    @Test
    @DisplayName("map 同名属性映射")
    public void testMap() {
        Source source = new Source();
        source.setId(1L);
        source.setName("Tom");
        source.setAge(18);

        Target target = SourceTargetMapper.INSTANCE.toTarget(source);

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
        SourceTargetMapper.INSTANCE.updateTarget(source, target);

        assertEquals("Tom", target.getName());
    }

    @Test
    @DisplayName("mapList 集合映射")
    public void testMapList() {
        Source s1 = new Source();
        s1.setName("a");
        Source s2 = new Source();
        s2.setName("b");

        List<Target> targets = SourceTargetMapper.INSTANCE.toTargetList(Arrays.asList(s1, s2));

        assertEquals(2, targets.size());
        assertEquals("a", targets.get(0).getName());
        assertEquals("b", targets.get(1).getName());
    }

    @Test
    @DisplayName("map null源返回null")
    public void testMapNull() {
        assertNull(SourceTargetMapper.INSTANCE.toTarget(null));
    }
}
