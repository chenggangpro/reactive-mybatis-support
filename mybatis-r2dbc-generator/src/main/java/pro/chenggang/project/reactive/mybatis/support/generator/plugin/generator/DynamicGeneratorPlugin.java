/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package pro.chenggang.project.reactive.mybatis.support.generator.plugin.generator;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.runtime.dynamic.sql.elements.AbstractMethodGenerator;
import org.mybatis.generator.runtime.dynamic.sql.elements.FragmentGenerator;
import org.mybatis.generator.runtime.dynamic.sql.elements.MethodAndImports;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorProperties;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorPropertiesHolder;
import pro.chenggang.project.reactive.mybatis.support.generator.support.GeneratedModelCustomizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Dynamic generate extension plugin.
 */
public class DynamicGeneratorPlugin extends PluginAdapter {

    private final GeneratedModelCustomizer generatedModelCustomizer = new GeneratedModelCustomizer();
    private boolean autoGenerateModel = true;
    private boolean hasDynamicSqlInClasspath = false;

    @Override
    public boolean validate(List<String> warnings) {
        try {
            Class<?> aClass = Class.forName("org.mybatis.dynamic.sql.SqlBuilder");
            hasDynamicSqlInClasspath = true;
        } catch (ClassNotFoundException e) {
            //ignore org.mybatis.dynamic.sql.SqlBuilder Class Not Found
        }
        this.autoGenerateModel = Boolean.parseBoolean(properties.getProperty("autoGenerateModel", "true"));
        return hasDynamicSqlInClasspath;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        if ((introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3)) {
            generatedModelCustomizer.customizeLombokGeneratedIfConfigured(interfaze, introspectedTable);
            interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
            interfaze.addAnnotation("@Mapper");
        }
        String tableRemarks = StringUtils.defaultIfBlank(introspectedTable.getRemarks(), "auto generated");
        interfaze.addJavaDocLine("/**");
        interfaze.addJavaDocLine(" * " + tableRemarks + " mapper");
        interfaze.addJavaDocLine(" * ");
        interfaze.addJavaDocLine(" * @author AutoGenerated");
        interfaze.addJavaDocLine(" */");
        boolean hasCommonSelectMapperClass = false;
        try {
            Class<?> aClass = Class.forName("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonSelectMapper");
            hasCommonSelectMapperClass = true;
        } catch (ClassNotFoundException e) {
            //ignore CommonSelectMapper Class Not Found
        }
        if (hasCommonSelectMapperClass) {
            FullyQualifiedJavaType commonSelectMapperType = new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonSelectMapper");
            interfaze.addSuperInterface(commonSelectMapperType);
            interfaze.addImportedType(commonSelectMapperType);
        }
        // remove original mybatis3's type
        interfaze.getImportedTypes().removeIf(item -> StringUtils.equals(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.update.UpdateDSL<org.mybatis.dynamic.sql.update.UpdateModel>"));
        interfaze.getImportedTypes().removeIf(item -> StringUtils.equals(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils"));
        interfaze.getImportedTypes().removeIf(item -> StringUtils.equals(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper"));
        interfaze.getImportedTypes().removeIf(item -> StringUtils.equals(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper"));
        interfaze.getImportedTypes().removeIf(item -> StringUtils.equals(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper"));
        interfaze.getImportedTypes().removeIf(item -> StringUtils.equals(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper"));
        interfaze.getSuperInterfaceTypes().removeIf(item -> StringUtils.equals(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper"));
        interfaze.getSuperInterfaceTypes().removeIf(item -> StringUtils.equals(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper"));
        interfaze.getSuperInterfaceTypes().removeIf(item -> StringUtils.startsWith(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper"));
        interfaze.getSuperInterfaceTypes().removeIf(item -> StringUtils.equals(item.getFullyQualifiedName(), "org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper"));
        // add r2dbc mybatis3's type
        interfaze.addSuperInterface(new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonCountMapper"));
        interfaze.addSuperInterface(new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonDeleteMapper"));
        FullyQualifiedJavaType commonInsertMapperJavaType = new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonInsertMapper");
        commonInsertMapperJavaType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        interfaze.addSuperInterface(commonInsertMapperJavaType);
        interfaze.addSuperInterface(new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonUpdateMapper"));
        interfaze.addImportedType(new FullyQualifiedJavaType("reactor.core.publisher.Mono"));
        interfaze.addImportedType(new FullyQualifiedJavaType("reactor.core.publisher.Flux"));
        interfaze.addImportedType(new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.ReactiveMyBatis3Utils"));
        interfaze.addImportedType(new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonCountMapper"));
        interfaze.addImportedType(new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonDeleteMapper"));
        interfaze.addImportedType(new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonInsertMapper"));
        interfaze.addImportedType(new FullyQualifiedJavaType("pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonUpdateMapper"));
        // override basic insert method with generated key
        GeneratorProperties generatorProperties = GeneratorPropertiesHolder.getInstance().getGeneratorProperties();
        if (generatorProperties.isGenerateReturnedKey()) {
            Optional<IntrospectedColumn> optionalIntrospectedColumn = introspectedTable.getPrimaryKeyColumns().stream()
                    .filter(IntrospectedColumn::isAutoIncrement)
                    .findFirst();
            if (optionalIntrospectedColumn.isPresent()) {
                this.generateInsertWithGeneratedKey(interfaze, introspectedTable);
            }
        }
        return true;
    }

    /**
     * override basic insert method with generated key
     *
     * @param interfaze
     * @param introspectedTable
     */
    public void generateInsertWithGeneratedKey(Interface interfaze, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        Set<FullyQualifiedJavaType> imports = new HashSet<>();
        FullyQualifiedJavaType adapter = new FullyQualifiedJavaType("org.mybatis.dynamic.sql.util.SqlProviderAdapter");
        FullyQualifiedJavaType annotation = new FullyQualifiedJavaType("org.apache.ibatis.annotations.InsertProvider");
        imports.add(new FullyQualifiedJavaType("org.mybatis.dynamic.sql.insert.render.InsertStatementProvider"));
        imports.add(adapter);
        imports.add(annotation);
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType("org.mybatis.dynamic.sql.insert.render.InsertStatementProvider");
        imports.add(recordType);
        parameterType.addTypeArgument(recordType);
        Method method = new Method("insert");
        method.setAbstract(true);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        method.addParameter(new Parameter(parameterType, "insertStatement"));
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);
        method.addAnnotation("@InsertProvider(type=SqlProviderAdapter.class, method=\"insert\")");
        MethodAndImports.Builder builder = MethodAndImports.withMethod(method)
                .withImports(imports);
        introspectedTable.getPrimaryKeyColumns().stream()
                .filter(IntrospectedColumn::isAutoIncrement)
                .findFirst()
                .ifPresent(introspectedColumn -> {
                    FullyQualifiedJavaType importOptionType = new FullyQualifiedJavaType("org.apache.ibatis.annotations.Options");
                    if (!interfaze.getImportedTypes().contains(importOptionType)) {
                        interfaze.addImportedType(importOptionType);
                    }
                    //@Options(useGeneratedKeys = true,keyProperty = "row.id",keyColumn = "column_name")
                    String optionsAnnotation = "@Options(useGeneratedKeys = true,keyProperty = \"row." + introspectedColumn.getJavaProperty() + "\",keyColumn = \"" + introspectedColumn.getActualColumnName() + "\")";
                    method.addAnnotation(optionsAnnotation);
                });
        MethodAndImports methodAndImports = builder.build();
        interfaze.addImportedTypes(methodAndImports.getImports());
        interfaze.getMethods().add(0, methodAndImports.getMethod());
    }

    @Override
    public boolean clientBasicSelectManyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Flux<" + introspectedTable.getBaseRecordType() + ">"));
        return true;
    }

    @Override
    public boolean clientBasicSelectOneMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<" + introspectedTable.getBaseRecordType() + ">"));
        return true;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientGeneralDeleteMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        replaceMyBatis3UtilsLine(method);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        return true;
    }

    @Override
    public boolean clientGeneralCountMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        replaceMyBatis3UtilsLine(method);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Long>"));
        return true;
    }

    @Override
    public boolean clientGeneralSelectDistinctMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        replaceMyBatis3UtilsLine(method);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Flux<" + introspectedTable.getBaseRecordType() + ">"));
        return true;
    }

    @Override
    public boolean clientGeneralSelectMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        replaceMyBatis3UtilsLine(method);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Flux<" + introspectedTable.getBaseRecordType() + ">"));
        return true;
    }

    @Override
    public boolean clientSelectOneMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        replaceMyBatis3UtilsLine(method);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<" + introspectedTable.getBaseRecordType() + ">"));
        return true;
    }

    @Override
    public boolean clientGeneralUpdateMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        replaceMyBatis3UtilsLine(method);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        replaceMyBatis3UtilsLine(method);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        return true;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        replaceMyBatis3UtilsLine(method);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        return true;
    }

    @Override
    public boolean clientInsertMultipleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        replaceMyBatis3UtilsLine(method);
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        return true;
    }

    /**
     * replace MyBatis3Utils --> ReactiveMyBatis3Utils
     *
     * @param method
     */
    private void replaceMyBatis3UtilsLine(Method method) {
        List<String> bodyLines = method.getBodyLines();
        Optional<String> optionalReplacedValue = bodyLines.stream()
                .findFirst()
                .map(value -> StringUtils.replace(value, "MyBatis3Utils", "ReactiveMyBatis3Utils"));
        if (optionalReplacedValue.isPresent()) {
            bodyLines.remove(0);
            bodyLines.add(0, optionalReplacedValue.get());
        }
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        interfaze.addStaticImport("org.mybatis.dynamic.sql.SqlBuilder.isEqualTo");
        FullyQualifiedJavaType whereApplierType = new FullyQualifiedJavaType("org.mybatis.dynamic.sql.where.WhereApplier");
        interfaze.addImportedType(whereApplierType);
        method.setName("updateSelective");
        method.addParameter(new Parameter(whereApplierType, "whereApplier"));
        FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String resultMapId = recordType.getShortNameWithoutTypeArguments() + "Result";
        String tableFieldName =
                JavaBeansUtil.getValidPropertyName(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
        FragmentGenerator fragmentGenerator = new FragmentGenerator.Builder()
                .withIntrospectedTable(introspectedTable)
                .withResultMapId(resultMapId)
                .withTableFieldName(tableFieldName)
                .build();
        method.getBodyLines().clear();
        method.addBodyLine("return update(c ->");
        method.addBodyLines(fragmentGenerator.getSetEqualWhenPresentLines(introspectedTable.getNonPrimaryKeyColumns(),
                "    c", "    ", false));
        method.addBodyLine("    .applyWhere(whereApplier)");
        method.addBodyLine(");");
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        return true;
    }

    @Override
    public boolean clientUpdateSelectiveColumnsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        method.setStatic(false);
        method.setDefault(true);
        List<String> bodyLines = method.getBodyLines();
        String tableFieldName = JavaBeansUtil.getValidPropertyName(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
        bodyLines.clear();
        List<String> lines = this.getUpdateByPrimaryKeyBodyLineWithAllNonPrimaryKeyColumns(tableFieldName, introspectedTable);
        bodyLines.addAll(lines);
        method.getParameters().removeIf(parameter -> StringUtils.equals(parameter.getType().getFullyQualifiedName(), "org.mybatis.dynamic.sql.update.UpdateDSL<org.mybatis.dynamic.sql.update.UpdateModel>"));
        interfaze.getImportedTypes().removeIf(parameter -> StringUtils.equals(parameter.getFullyQualifiedName(), "org.mybatis.dynamic.sql.update.UpdateDSL"));
        method.setName("updateAllByPrimaryKey");
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        return true;
    }

    @Override
    public boolean clientUpdateAllColumnsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // update all equalTo include id
        method.setStatic(false);
        method.setDefault(true);
        List<String> bodyLines = method.getBodyLines();
        String tableFieldName = JavaBeansUtil.getValidPropertyName(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
        bodyLines.clear();
        List<String> lines = this.getUpdateSelectiveByPrimaryKeyBodyLineWithAllNonPrimaryKeyColumns(tableFieldName, introspectedTable);
        bodyLines.addAll(lines);
        method.getParameters().removeIf(parameter -> StringUtils.equals(parameter.getType().getFullyQualifiedName(), "org.mybatis.dynamic.sql.update.UpdateDSL<org.mybatis.dynamic.sql.update.UpdateModel>"));
        interfaze.getImportedTypes().removeIf(parameter -> StringUtils.equals(parameter.getFullyQualifiedName(), "org.mybatis.dynamic.sql.update.UpdateDSL"));
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        method.setName("updateSelectiveByPrimaryKey");
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // update all equalTo include id
        method.setStatic(false);
        method.setDefault(true);
        List<String> bodyLines = method.getBodyLines();
        String tableFieldName = JavaBeansUtil.getValidPropertyName(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
        bodyLines.clear();
        List<String> lines = this.getUpdateWhereApplierBodyLineWithAllNonPrimaryKeyColumns(tableFieldName, introspectedTable);
        bodyLines.addAll(lines);
        FullyQualifiedJavaType whereApplierType = new FullyQualifiedJavaType("org.mybatis.dynamic.sql.where.WhereApplier");
        method.getParameters().removeIf(parameter -> StringUtils.equals(parameter.getType().getFullyQualifiedName(), "org.mybatis.dynamic.sql.update.UpdateDSL<org.mybatis.dynamic.sql.update.UpdateModel>"));
        interfaze.getImportedTypes().removeIf(parameter -> StringUtils.equals(parameter.getFullyQualifiedName(), "org.mybatis.dynamic.sql.update.UpdateDSL"));
        interfaze.addImportedType(whereApplierType);
        method.setName("updateAll");
        method.addParameter(new Parameter(whereApplierType, "whereApplier"));
        method.setReturnType(new FullyQualifiedJavaType("reactor.core.publisher.Mono<java.lang.Integer>"));
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        List<VisitableElement> elements = document.getRootElement().getElements();
        XmlElement xmlElement = (XmlElement) elements.get(0);
        List<VisitableElement> elementElements = xmlElement.getElements();
        elementElements.removeIf(visitableElement -> visitableElement instanceof TextElement);
        String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
        XmlElement newXmlElement = new XmlElement(xmlElement);
        List<Attribute> attributeList = newXmlElement.getAttributes()
                .stream()
                .map(attribute -> {
                    if ("id".equals(attribute.getName())) {
                        return new Attribute(attribute.getName(), "TableResultMap");
                    }
                    return new Attribute(attribute.getName(), attribute.getValue());
                })
                .collect(Collectors.toList());
        newXmlElement.getAttributes().clear();
        attributeList.forEach(newXmlElement::addAttribute);
        List<String> columnNameWithTable = new ArrayList<>();
        List<XmlElement> subXmlElements = newXmlElement.getElements()
                .stream()
                .map(element -> {
                    XmlElement subXmlElement = new XmlElement((XmlElement) element);
                    List<Attribute> attributes = subXmlElement.getAttributes()
                            .stream()
                            .map(attribute -> {
                                if ("column".equals(attribute.getName())) {
                                    String columnWithTableName = tableName + "_" + attribute.getValue();
                                    columnNameWithTable.add(tableName + "." + attribute.getValue() + " AS " + columnWithTableName);
                                    return new Attribute(attribute.getName(), columnWithTableName);
                                }
                                return new Attribute(attribute.getName(), attribute.getValue());
                            })
                            .collect(Collectors.toList());
                    subXmlElement.getAttributes().clear();
                    attributes.forEach(subXmlElement::addAttribute);
                    return subXmlElement;
                })
                .collect(Collectors.toList());
        newXmlElement.getElements().clear();
        subXmlElements.forEach(newXmlElement::addElement);
        elements.add(newXmlElement);
        XmlElement columnSqlXmlElement = new XmlElement("sql");
        columnSqlXmlElement.addAttribute(new Attribute("id", "columnNameWithTable"));
        String columnNameWithTableSql = String.join(", \n    ", columnNameWithTable);
        TextElement columnSqlContentElement = new TextElement(columnNameWithTableSql);
        columnSqlXmlElement.addElement(columnSqlContentElement);
        elements.add(columnSqlXmlElement);
        return true;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerApplyWhereMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        return false;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        return false;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!autoGenerateModel) {
            return false;
        }
        generatedModelCustomizer.customizeLombok(topLevelClass, introspectedTable);
        generatedModelCustomizer.customizeModelConstant(context, topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!autoGenerateModel) {
            return false;
        }
        generatedModelCustomizer.customizeLombok(topLevelClass, introspectedTable);
        generatedModelCustomizer.customizeModelConstant(context, topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!autoGenerateModel) {
            return false;
        }
        generatedModelCustomizer.customizeLombok(topLevelClass, introspectedTable);
        generatedModelCustomizer.customizeModelConstant(context, topLevelClass, introspectedTable);
        return false;
    }

    @Override
    public boolean dynamicSqlSupportGenerated(TopLevelClass supportClass, IntrospectedTable introspectedTable) {
        String tableRemarks = StringUtils.defaultIfBlank(introspectedTable.getRemarks(), "auto generated");
        supportClass.addJavaDocLine("/**");
        supportClass.addJavaDocLine(" * " + tableRemarks + " dynamic mapper");
        supportClass.addJavaDocLine(" * ");
        supportClass.addJavaDocLine(" * @author autoGenerated");
        supportClass.addJavaDocLine(" */");
        return true;
    }

    private List<String> getUpdateWhereApplierBodyLineWithAllNonPrimaryKeyColumns(String tableFieldName, IntrospectedTable introspectedTable) {
        List<String> lines = new ArrayList<>();
        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns());
        Iterator<IntrospectedColumn> iter = columns.iterator();
        boolean first = true;
        String firstLinePrefix = "    c";
        String subsequentLinePrefix = "    ";
        lines.add("return update(c ->");
        while (iter.hasNext()) {
            IntrospectedColumn column = iter.next();
            String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
            String methodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(), column.getFullyQualifiedJavaType());
            String start;
            if (first) {
                start = firstLinePrefix;
                first = false;
            } else {
                start = subsequentLinePrefix;
            }
            String line = null;
            if (column.isNullable()) {
                line = start + ".set(" + fieldName
                        + ").equalTo(row::" + methodName
                        + ")";
            } else {
                line = start + ".set(" + fieldName
                        + ").equalToWhenPresent(row::" + methodName
                        + ")";
            }
            lines.add(line);
        }
        lines.add(subsequentLinePrefix + ".applyWhere(whereApplier)");
        lines.add(");");
        return lines;
    }

    private List<String> getUpdateByPrimaryKeyBodyLineWithAllNonPrimaryKeyColumns(String tableFieldName, IntrospectedTable introspectedTable) {
        List<String> lines = new ArrayList<>();
        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns());
        Iterator<IntrospectedColumn> iter = columns.iterator();
        boolean first = true;
        String firstLinePrefix = "    c";
        String subsequentLinePrefix = "    ";
        lines.add("return update(c ->");
        while (iter.hasNext()) {
            IntrospectedColumn column = iter.next();
            String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
            String methodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(), column.getFullyQualifiedJavaType());
            String start;
            if (first) {
                start = firstLinePrefix;
                first = false;
            } else {
                start = subsequentLinePrefix;
            }
            String line = null;
            if (column.isNullable()) {
                line = start + ".set(" + fieldName
                        + ").equalTo(row::" + methodName
                        + ")";
            } else {
                line = start + ".set(" + fieldName
                        + ").equalToWhenPresent(row::" + methodName
                        + ")";
            }
            lines.add(line);
        }
        first = true;
        for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
            String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
            String methodName = JavaBeansUtil.getGetterMethodName(
                    column.getJavaProperty(), column.getFullyQualifiedJavaType());
            if (first) {
                lines.add(subsequentLinePrefix + ".where(" + fieldName
                        + ", isEqualTo(row::" + methodName
                        + "))");
                first = false;
            } else {
                lines.add(subsequentLinePrefix + ".and(" + fieldName
                        + ", isEqualTo(row::" + methodName
                        + "))");
            }
        }
        lines.add(");");
        return lines;
    }

    private List<String> getUpdateSelectiveByPrimaryKeyBodyLineWithAllNonPrimaryKeyColumns(String tableFieldName, IntrospectedTable introspectedTable) {
        List<String> lines = new ArrayList<>();
        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns());
        Iterator<IntrospectedColumn> iter = columns.iterator();
        boolean first = true;
        String firstLinePrefix = "    c";
        String subsequentLinePrefix = "    ";
        lines.add("return update(c ->");
        while (iter.hasNext()) {
            IntrospectedColumn column = iter.next();
            String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
            String methodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(), column.getFullyQualifiedJavaType());
            String start;
            if (first) {
                start = firstLinePrefix;
                first = false;
            } else {
                start = subsequentLinePrefix;
            }
            String line = start + ".set(" + fieldName
                    + ").equalToWhenPresent(row::" + methodName
                    + ")";
            lines.add(line);
        }
        first = true;
        for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
            String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
            String methodName = JavaBeansUtil.getGetterMethodName(
                    column.getJavaProperty(), column.getFullyQualifiedJavaType());
            if (first) {
                lines.add(subsequentLinePrefix + ".where(" + fieldName
                        + ", isEqualTo(row::" + methodName
                        + "))");
                first = false;
            } else {
                lines.add(subsequentLinePrefix + ".and(" + fieldName
                        + ", isEqualTo(row::" + methodName
                        + "))");
            }
        }
        lines.add(");");
        return lines;
    }
}
