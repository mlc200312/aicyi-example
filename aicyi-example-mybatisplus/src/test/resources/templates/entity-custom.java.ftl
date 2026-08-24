package ${package.Entity};

<#list table.importPackages as pkg>
import ${pkg};
</#list>
<#-- 自定义字段 Java 类型（业务枚举等）：为全限定类型自动补充 import -->
<#list table.fields as field>
    <#if field.propertyType?contains(".") && !field.propertyType?starts_with("java.")>
        <#assign duplicated=false>
        <#list table.fields as other>
            <#if other?index < field?index && other.propertyType == field.propertyType>
                <#assign duplicated=true>
            </#if>
        </#list>
        <#if !duplicated>
import ${field.propertyType};
        </#if>
    </#if>
</#list>
<#-- 字段自定义 TypeHandler：为 TypeHandler 类自动补充 import -->
<#list table.fields as field>
    <#if field.customMap?? && field.customMap.typeHandler??>
        <#assign duplicated=false>
        <#list table.fields as other>
            <#if other?index < field?index && other.customMap?? && other.customMap.typeHandler! == field.customMap.typeHandler>
                <#assign duplicated=true>
            </#if>
        </#list>
        <#if !duplicated>
import ${field.customMap.typeHandler};
        </#if>
    </#if>
</#list>
<#if springdoc!false>
import io.swagger.v3.oas.annotations.media.Schema;
<#elseif swagger>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if entityLombokModel>
import lombok.Getter;
import lombok.Setter;
    <#if chainModel>
import lombok.experimental.Accessors;
    </#if>
</#if>

/**
 * <p>
 * ${table.comment!}
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if entityLombokModel>
@Getter
@Setter
    <#if chainModel>
@Accessors(chain = true)
    </#if>
</#if>
<#-- 存在自定义 TypeHandler 的字段时，开启 autoResultMap，保证查询结果也走 TypeHandler -->
<#assign hasTypeHandler=false>
<#list table.fields as field>
    <#if field.customMap?? && field.customMap.typeHandler??>
        <#assign hasTypeHandler=true>
    </#if>
</#list>
<#if table.convert>
    <#if hasTypeHandler>
@TableName(value = "${schemaName!}${table.name}", autoResultMap = true)
    <#else>
@TableName("${schemaName!}${table.name}")
    </#if>
</#if>
<#if springdoc!false>
@Schema(name = "${entity}", description = "${table.comment!}")
<#elseif swagger>
@ApiModel(value = "${entity}对象", description = "${table.comment!}")
</#if>
<#if superEntityClass??>
public class ${entity} extends ${superEntityClass}<#if activeRecord><${entity}></#if> {
<#elseif activeRecord>
public class ${entity} extends Model<${entity}> {
<#elseif entitySerialVersionUID>
public class ${entity} implements Serializable {
<#else>
public class ${entity} {
</#if>
<#if entitySerialVersionUID>

    private static final long serialVersionUID = 1L;
</#if>
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.keyFlag>
        <#assign keyPropertyName="${field.propertyName}"/>
    </#if>

    <#if field.comment!?length gt 0>
        <#if springdoc!false>
    @Schema(description = "${field.comment}")
        <#elseif swagger>
    @ApiModelProperty("${field.comment}")
        <#else>
    /**
     * ${field.comment}
     */
        </#if>
    </#if>
    <#-- 普通字段：存在自定义 TypeHandler 时追加 typeHandler 属性 -->
    <#assign typeHandlerAttr="">
    <#if field.customMap?? && field.customMap.typeHandler??>
        <#assign typeHandlerAttr=", typeHandler = ${field.customMap.typeHandler?keep_after_last('.')}.class">
    </#if>
    <#if field.keyFlag>
        <#-- 主键 -->
        <#if field.keyIdentityFlag>
    @TableId(value = "${field.annotationColumnName}", type = IdType.AUTO)
        <#elseif idType??>
    @TableId(value = "${field.annotationColumnName}", type = IdType.${idType})
        <#elseif field.convert>
    @TableId("${field.annotationColumnName}")
        </#if>
    <#elseif field.fill??>
    <#-- -----   存在字段填充设置   ----->
        <#if field.convert>
    @TableField(value = "${field.annotationColumnName}", fill = FieldFill.${field.fill}${typeHandlerAttr})
        <#elseif typeHandlerAttr?has_content>
    @TableField(fill = FieldFill.${field.fill}${typeHandlerAttr})
        <#else>
    @TableField(fill = FieldFill.${field.fill})
        </#if>
    <#elseif field.convert>
        <#if typeHandlerAttr?has_content>
    @TableField(value = "${field.annotationColumnName}"${typeHandlerAttr})
        <#else>
    @TableField("${field.annotationColumnName}")
        </#if>
    <#elseif typeHandlerAttr?has_content>
    @TableField(typeHandler = ${field.customMap.typeHandler?keep_after_last('.')}.class)
    </#if>
    <#-- 乐观锁注解 -->
    <#if field.versionField>
    @Version
    </#if>
    <#-- 逻辑删除注解 -->
    <#if field.logicDeleteField>
    @TableLogic
    </#if>
    private ${field.propertyType} ${field.propertyName};
</#list>
<#------------  END 字段循环遍历  ---------->
<#if !entityLombokModel>
    <#list table.fields as field>
        <#if field.propertyType == "boolean">
            <#assign getprefix="is"/>
        <#else>
            <#assign getprefix="get"/>
        </#if>

    public ${field.propertyType} ${getprefix}${field.capitalName}() {
        return ${field.propertyName};
    }

    <#if chainModel>
    public ${entity} set${field.capitalName}(${field.propertyType} ${field.propertyName}) {
    <#else>
    public void set${field.capitalName}(${field.propertyType} ${field.propertyName}) {
    </#if>
        this.${field.propertyName} = ${field.propertyName};
        <#if chainModel>
        return this;
        </#if>
    }
    </#list>
</#if>
<#if entityColumnConstant>
    <#list table.fields as field>

    public static final String ${field.name?upper_case} = "${field.name}";
    </#list>
</#if>
<#if activeRecord>

    @Override
    public Serializable pkVal() {
    <#if keyPropertyName??>
        return this.${keyPropertyName};
    <#else>
        return null;
    </#if>
    }
</#if>
<#if !entityLombokModel>

    @Override
    public String toString() {
        return "${entity}{" +
    <#list table.fields as field>
        <#if field_index==0>
            "${field.propertyName} = " + ${field.propertyName} +
        <#else>
            ", ${field.propertyName} = " + ${field.propertyName} +
        </#if>
    </#list>
        "}";
    }
</#if>
}
