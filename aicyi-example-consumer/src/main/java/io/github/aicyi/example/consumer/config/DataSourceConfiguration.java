package io.github.aicyi.example.consumer.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.aicyi.commons.lang.EnumType;
import io.github.aicyi.commons.lang.StringEnumType;
import io.github.aicyi.commons.core.logging.Logger;
import io.github.aicyi.commons.logging.LoggerFactory;
import io.github.aicyi.midware.db.commons.typehandler.GenericEnumTypeHandler;
import io.github.aicyi.midware.db.commons.typehandler.GenericStringEnumTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mr.Min
 * @description 数据源相关配置
 * @date 2020-05-11
 **/
@Configuration
public class DataSourceConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test?characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&verifyServerCertificate=false&useLegacyDatetimeCode=false&serverTimezone=UTC&allowMultiQueries=true");
        config.setUsername("root");
        config.setPassword("root");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 连接池优化参数
        config.setMaximumPoolSize(30);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        return new HikariDataSource(config);
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            // 注册所有实现了EnumType接口的枚举处理器
//            typeHandlerRegistry.register(GenderType.class, new GenericEnumTypeHandler(GenderType.class));
            // 注册所有枚举
            registerAllEnumTypeHandlers(typeHandlerRegistry,
                    "io.github.aicyi.commons.lang.type", "io.github.aicyi.example.domain.type");
        };
    }

    /**
     * 自动注册指定包下的所有实现了EnumType接口的枚举处理器
     */
    private void registerAllEnumTypeHandlers(TypeHandlerRegistry registry, String... packageNames) {
        Reflections reflections = new Reflections((Object[]) packageNames);

        Set<Class<? extends EnumType>> enumClasses = reflections.getSubTypesOf(EnumType.class);
        Set<Class<? extends StringEnumType>> stringEnumClasses = reflections.getSubTypesOf(StringEnumType.class);

        Set<Class<?>> enumClassSet = new HashSet<>();
        enumClassSet.addAll(enumClasses);
        enumClassSet.addAll(stringEnumClasses);

        for (Class<?> enumClass : enumClassSet) {
            if (enumClass.isEnum()) {
                @SuppressWarnings("unchecked")
                Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) enumClass;
                if (EnumType.class.isAssignableFrom(enumClass)) {
                    registry.register(enumType, new GenericEnumTypeHandler(enumType));
                }
                if (StringEnumType.class.isAssignableFrom(enumClass)) {
                    registry.register(enumType, new GenericStringEnumTypeHandler(enumType));
                }
                logger.info("Registered EnumTypeHandler for: {}", enumType.getName());
            }

        }
    }
}
