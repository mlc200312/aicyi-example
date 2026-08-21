package io.github.aicyi.example.mybaitsplus;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import io.github.aicyi.commons.util.reflect.ReflectionUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MyBatis‑Plus 代码生成器
 * <p>
 * 生成参数（数据库连接、包名、字段类型覆盖等）统一从 src/test/resources/generator.yml 读取，
 * 增删字段类型映射只需修改配置文件。运行 main 方法即可生成代码。
 */
public class MyBatisPlusGenerator {

    /**
     * 生成器配置文件（classpath 下，即 src/test/resources）
     */
    private static final String CONFIG_FILE = "generator.yml";

    /**
     * 自定义实体模板：在官方默认模板基础上，为全限定的自定义字段类型自动补 import
     */
    private static final String CUSTOM_ENTITY_TEMPLATE = "/templates/entity-custom.java";

    private static final Map<String, Object> CONFIG = loadConfig();

    /**
     * 列名(小写) -> Java 类型（全限定名或 JDK 内置类型简名）
     */
    private static final Map<String, String> COLUMN_TYPE_OVERRIDES = loadColumnTypeOverrides();

    public static void main(String[] args) {
        generateCode();
    }

    private static void generateCode() {

        Map<String, Object> dataSource = section(CONFIG, "datasource");
        Map<String, Object> global = section(CONFIG, "global");
        Map<String, Object> pkg = section(CONFIG, "package");

        String path = Paths.get(System.getProperty("user.dir")) + str(global, "project", "/aicyi-example-mybatisplus");

        FastAutoGenerator.create(str(dataSource, "url"), str(dataSource, "username"), str(dataSource, "password"))
                .globalConfig(builder -> builder
                        .author(str(global, "author", "Leno"))
                        .outputDir(path + "/src/main/java")
                        .commentDate("yyyy-MM-dd")
                )
                .dataSourceConfig(builder ->
                        builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {

                            String columnName = (String) ReflectionUtils.getFieldValue(metaInfo, "columnName");

                            // 1. 配置文件中的列类型覆盖：支持业务枚举等任意自定义类
                            IColumnType overrideType = resolveColumnTypeOverride(columnName);
                            if (overrideType != null) {
                                return overrideType;
                            }

                            // 2. 默认规则：smallint 映射为 Integer
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.SMALLINT) {
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        })
                )
                .packageConfig(builder -> builder
                        .parent(str(pkg, "parent", "io.github.aicyi.example"))
                        .moduleName(str(pkg, "moduleName", "mybatisplus"))
                        .entity(str(pkg, "entity", "entity"))
                        .mapper(str(pkg, "mapper", "mapper"))
                        .service(str(pkg, "service", "service"))
                        .serviceImpl(str(pkg, "serviceImpl", "service.impl"))
                        .pathInfo(Collections.singletonMap(OutputFile.xml, path + "/src/main/resources/mapper"))
                )
                .strategyConfig((scanner, builder) -> builder.addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        .addTablePrefix(str(global, "tablePrefix", "t_"))
                        .entityBuilder()
                        .enableFileOverride()
                        .enableLombok()
                        .naming(NamingStrategy.underline_to_camel)
                        .columnNaming(NamingStrategy.underline_to_camel)
                        // 公共字段自动填充，与 aicyi-midware MybatisPlusMetaObjectHandler 约定对齐
                        .addTableFills(
                                new Column("create_time", FieldFill.INSERT),
                                new Column("update_time", FieldFill.INSERT_UPDATE),
                                new Column("deleted", FieldFill.INSERT),
                                new Column("version", FieldFill.INSERT))
                        // 乐观锁字段，生成 @Version（配合 OptimisticLockerInnerInterceptor）
                        .versionColumnName("version")
                        // 逻辑删除字段，生成 @TableLogic
                        .logicDeleteColumnName("deleted")
                        .controllerBuilder()
                        .enableRestStyle()
                        .build()
                )
                // 使用自定义实体模板，为自定义字段类型（业务枚举等）自动补 import
                .templateConfig(builder -> builder.entity(CUSTOM_ENTITY_TEMPLATE))
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }

    /**
     * 解析配置文件中的列类型覆盖。
     * JDK 内置类型简名（Boolean / Integer 等）复用 DbColumnType；
     * 全限定类名则构造自定义 IColumnType，模板渲染时会自动补 import。
     */
    private static IColumnType resolveColumnTypeOverride(String columnName) {
        if (columnName == null) {
            return null;
        }
        String javaType = COLUMN_TYPE_OVERRIDES.get(columnName.toLowerCase());
        if (javaType == null || javaType.trim().isEmpty()) {
            return null;
        }
        javaType = javaType.trim();
        if (!javaType.contains(".")) {
            for (DbColumnType columnType : DbColumnType.values()) {
                if (columnType.getType().equalsIgnoreCase(javaType)) {
                    return columnType;
                }
            }
            javaType = "java.lang." + javaType;
        }
        return customColumnType(javaType);
    }

    private static IColumnType customColumnType(final String fullClassName) {
        return new IColumnType() {
            @Override
            public String getType() {
                return fullClassName;
            }

            @Override
            public String getPkg() {
                return null;
            }
        };
    }

    private static Map<String, String> loadColumnTypeOverrides() {
        Map<String, String> overrides = new HashMap<>();
        Map<String, Object> raw = section(section(CONFIG, "column-type"), "overrides");
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                overrides.put(entry.getKey().toLowerCase(), String.valueOf(entry.getValue()));
            }
        }
        return overrides;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadConfig() {
        try (InputStream in = MyBatisPlusGenerator.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException("未找到生成器配置文件: src/test/resources/" + CONFIG_FILE);
            }
            Object loaded = new Yaml().load(in);
            if (!(loaded instanceof Map)) {
                throw new IllegalStateException("生成器配置文件为空或格式不正确: " + CONFIG_FILE);
            }
            return (Map<String, Object>) loaded;
        } catch (IOException e) {
            throw new IllegalStateException("读取生成器配置文件失败: " + CONFIG_FILE, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Map<String, Object> config, String key) {
        Object value = config == null ? null : config.get(key);
        return value instanceof Map ? (Map<String, Object>) value : Collections.<String, Object>emptyMap();
    }

    private static String str(Map<String, Object> config, String key) {
        return str(config, key, null);
    }

    private static String str(Map<String, Object> config, String key, String defaultValue) {
        Object value = config == null ? null : config.get(key);
        return value != null ? String.valueOf(value) : defaultValue;
    }
}
