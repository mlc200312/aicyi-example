package io.github.aicyi.example.mybaitsplus;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.po.TableField;
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
     * 全局列类型覆盖：列名(小写) -> Java 类型（全限定名或 JDK 内置类型简名），对所有表生效
     */
    private static final Map<String, String> GLOBAL_TYPE_OVERRIDES = loadGlobalTypeOverrides();

    /**
     * 类（表）级列类型覆盖：表名(小写) -> {列名(小写) -> Java 类型}，优先于全局配置
     */
    private static final Map<String, Map<String, String>> TABLE_TYPE_OVERRIDES = loadTableTypeOverrides();

    /**
     * TypeHandler 配置：表名(小写，"*"/"global" 为全局) -> {列名(小写) -> TypeHandler 类全限定名}
     */
    private static final Map<String, Map<String, String>> TYPE_HANDLERS = loadTypeHandlers();

    public static void main(String[] args) {
        generateCode();
    }

    private static void generateCode() {

        Map<String, Object> dataSource = section(CONFIG, "datasource");
        Map<String, Object> global = section(CONFIG, "global");
        Map<String, Object> pkg = section(CONFIG, "package");

        String path = Paths.get(System.getProperty("user.dir")) + getStrValue(global, "project");

        FastAutoGenerator.create(getStrValue(dataSource, "url"), getStrValue(dataSource, "username"), getStrValue(dataSource, "password"))
                .globalConfig(builder -> builder
                        .author(getStrValue(global, "author"))
                        .commentDate("yyyy-MM-dd")
                        .outputDir(path + "/src/main/java")
                )
                .dataSourceConfig(builder ->
                        builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {

                            String columnName = (String) ReflectionUtils.getFieldValue(metaInfo, "columnName");
                            String tableName = (String) ReflectionUtils.getFieldValue(metaInfo, "tableName");

                            // 1. 配置文件中的列类型覆盖：类（表）级配置优先，未命中再走全局配置
                            IColumnType overrideType = resolveColumnTypeOverride(tableName, columnName);
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
                        .parent(getStrValue(pkg, "parent"))
                        .moduleName(getStrValue(pkg, "moduleName"))
                        .entity(getStrValue(pkg, "entity"))
                        .mapper(getStrValue(pkg, "mapper"))
                        .service(getStrValue(pkg, "service"))
                        .serviceImpl(getStrValue(pkg, "serviceImpl"))
                        .pathInfo(Collections.singletonMap(OutputFile.xml, path + "/src/main/resources/mapper"))
                )
                .strategyConfig((scanner, builder) -> builder.addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        .addTablePrefix(getStrValue(global, "tablePrefix"))
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
                // 自定义注入：将配置的列 TypeHandler 写入字段 customMap，由实体模板渲染 @TableField(typeHandler = ...)
                .injectionConfig(builder -> builder.beforeOutputFile((tableInfo, objectMap) -> {
                    for (TableField field : tableInfo.getFields()) {
                        String typeHandler = resolveTypeHandler(tableInfo.getName(), field.getColumnName());
                        if (typeHandler != null) {
                            Map<String, Object> customMap = field.getCustomMap();
                            if (customMap == null) {
                                customMap = new HashMap<>();
                                field.setCustomMap(customMap);
                            }
                            customMap.put("typeHandler", typeHandler);
                        }
                    }
                }))
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
     * 解析配置文件中的列类型覆盖，类（表）级配置优先于全局配置。
     * JDK 内置类型简名（Boolean / Integer 等）复用 DbColumnType；
     * 全限定类名则构造自定义 IColumnType，模板渲染时会自动补 import。
     */
    private static IColumnType resolveColumnTypeOverride(String tableName, String columnName) {
        if (columnName == null) {
            return null;
        }
        String columnKey = columnName.toLowerCase();
        String javaType = null;

        // 1. 类（表）级配置优先
        if (tableName != null) {
            Map<String, String> tableOverrides = TABLE_TYPE_OVERRIDES.get(tableName.toLowerCase());
            if (tableOverrides != null) {
                javaType = tableOverrides.get(columnKey);
            }
        }

        // 2. 未命中则回退全局配置
        if (javaType == null || javaType.trim().isEmpty()) {
            javaType = GLOBAL_TYPE_OVERRIDES.get(columnKey);
        }
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
                return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            }

            @Override
            public String getPkg() {
                return fullClassName;
            }
        };
    }

    private static Map<String, String> loadGlobalTypeOverrides() {
        Map<String, String> overrides = new HashMap<>();
        Map<String, Object> raw = section(section(CONFIG, "column-type"), "overrides");
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                overrides.put(entry.getKey().toLowerCase(), String.valueOf(entry.getValue()));
            }
        }
        return overrides;
    }

    /**
     * 加载类（表）级列类型覆盖。
     * 表名键带前缀与去前缀两种形式均会注册，配置文件里写 t_user 或 user 都能命中。
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, String>> loadTableTypeOverrides() {
        Map<String, Map<String, String>> result = new HashMap<>();
        String tablePrefix = getStrValue(section(CONFIG, "global"), "tablePrefix").toLowerCase();
        Map<String, Object> tables = section(section(CONFIG, "column-type"), "tables");
        for (Map.Entry<String, Object> tableEntry : tables.entrySet()) {
            if (tableEntry.getKey() == null || !(tableEntry.getValue() instanceof Map)) {
                continue;
            }
            Map<String, String> columnOverrides = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) tableEntry.getValue()).entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    columnOverrides.put(entry.getKey().toLowerCase(), String.valueOf(entry.getValue()));
                }
            }
            String tableName = tableEntry.getKey().toLowerCase();
            result.put(tableName, columnOverrides);
            if (tableName.startsWith(tablePrefix)) {
                result.put(tableName.substring(tablePrefix.length()), columnOverrides);
            } else {
                result.put(tablePrefix + tableName, columnOverrides);
            }
        }
        return result;
    }

    /**
     * 解析列的 TypeHandler：类（表）级配置优先，其次全局（"*"/"global"）配置。
     */
    private static String resolveTypeHandler(String tableName, String columnName) {
        if (columnName == null) {
            return null;
        }
        String columnKey = columnName.toLowerCase();
        if (tableName != null) {
            Map<String, String> tableHandlers = TYPE_HANDLERS.get(tableName.toLowerCase());
            if (tableHandlers != null && tableHandlers.get(columnKey) != null) {
                return tableHandlers.get(columnKey);
            }
        }
        Map<String, String> globalHandlers = TYPE_HANDLERS.get("*");
        return globalHandlers == null ? null : globalHandlers.get(columnKey);
    }

    /**
     * 加载 TypeHandler 配置，与列类型覆盖相同的两级结构：
     * "*" 或 "global" 为全局配置；其余键为表名，带前缀与去前缀两种形式均会注册。
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, String>> loadTypeHandlers() {
        Map<String, Map<String, String>> result = new HashMap<>();
        String tablePrefix = getStrValue(section(CONFIG, "global"), "tablePrefix").toLowerCase();
        Map<String, Object> tables = section(section(CONFIG, "column-type"), "type-handlers");
        for (Map.Entry<String, Object> tableEntry : tables.entrySet()) {
            if (tableEntry.getKey() == null || !(tableEntry.getValue() instanceof Map)) {
                continue;
            }
            Map<String, String> columnHandlers = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) tableEntry.getValue()).entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    columnHandlers.put(entry.getKey().toLowerCase(), String.valueOf(entry.getValue()).trim());
                }
            }
            String tableName = tableEntry.getKey().toLowerCase();
            if ("*".equals(tableName) || "global".equals(tableName)) {
                result.put("*", columnHandlers);
                continue;
            }
            result.put(tableName, columnHandlers);
            if (tableName.startsWith(tablePrefix)) {
                result.put(tableName.substring(tablePrefix.length()), columnHandlers);
            } else {
                result.put(tablePrefix + tableName, columnHandlers);
            }
        }
        return result;
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

    private static String getStrValue(Map<String, Object> config, String key) {
        Object value = config == null ? null : config.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Config key '" + key + "' not found");
        }
        return String.valueOf(value);
    }
}
