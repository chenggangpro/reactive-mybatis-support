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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler;

import io.r2dbc.spi.Row;
import org.apache.ibatis.annotations.AutomapConstructor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.executor.result.ResultMapException;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.util.MapUtil;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.exception.R2dbcResultException;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.TypeHandleContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * The type Default reactive result handler.
 * <p>
 * {@link org.apache.ibatis.executor.resultset.DefaultResultSetHandler}
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class DefaultReactiveResultHandler implements ReactiveResultHandler {

    private final LongAdder totalCount = new LongAdder();

    private final R2dbcMybatisConfiguration r2dbcMybatisConfiguration;
    private final MappedStatement mappedStatement;
    private final ObjectFactory objectFactory;
    private final ReflectorFactory reflectorFactory;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final Map<CacheKey, List<DefaultReactiveResultHandler.PendingRelation>> pendingRelations = new HashMap<>();
    // Cached Automappings
    private final Map<String, List<DefaultReactiveResultHandler.UnMappedColumnAutoMapping>> autoMappingsCache = new HashMap<>();
    private final Map<String, List<String>> constructorAutoMappingColumns = new HashMap<>();
    // nested resultmaps
    private final Map<CacheKey, Object> nestedResultObjects = new HashMap<>();
    private final Map<String, Object> ancestorObjects = new HashMap<>();
    private final TypeHandler<?> delegatedTypeHandler;
    private final List<Object> resultHolder = new ArrayList<>();
    // temporary marking flag that indicate using constructor mapping (use field to reduce memory usage)
    private boolean useConstructorMappings;

    /**
     * Instantiates a new Default reactive result handler.
     *
     * @param r2dbcMybatisConfiguration the R2dbc mybatis configuration
     * @param mappedStatement           the mapped statement
     */
    public DefaultReactiveResultHandler(R2dbcMybatisConfiguration r2dbcMybatisConfiguration, MappedStatement mappedStatement) {
        this.mappedStatement = mappedStatement;
        this.r2dbcMybatisConfiguration = r2dbcMybatisConfiguration;
        this.objectFactory = r2dbcMybatisConfiguration.getObjectFactory();
        this.reflectorFactory = r2dbcMybatisConfiguration.getReflectorFactory();
        this.typeHandlerRegistry = r2dbcMybatisConfiguration.getTypeHandlerRegistry();
        this.delegatedTypeHandler = this.initDelegateTypeHandler();
    }

    @Override
    public Integer getResultRowTotalCount() {
        return totalCount.intValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T handleResult(RowResultWrapper rowResultWrapper) {
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();
        int resultMapCount = resultMaps.size();
        if (resultMapCount < 1) {
            throw new ExecutorException("A query was run and no Result Maps were found for the Mapped Statement '" + mappedStatement.getId()
                    + "'.  It's likely that neither a Result Type nor a Result Map was specified.");
        }
        ResultMap resultMap = resultMaps.get(0);
        if (!resultMap.hasNestedResultMaps()) {
            try {
                ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rowResultWrapper, resultMap, null);
                Object rowValue = getRowValueForSimpleResultMap(rowResultWrapper, discriminatedResultMap, null);
                totalCount.increment();
                return (T) (rowValue == null ? DEFERRED : rowValue);
            } catch (SQLException e) {
                throw new R2dbcResultException(e);
            }
        }
        try {
            Object rowValue = handleRowValuesForNestedResultMap(rowResultWrapper, resultMap);
            totalCount.increment();
            return (T) (rowValue == null ? DEFERRED : rowValue);
        } catch (SQLException e) {
            throw new R2dbcResultException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getRemainedResults() {
        return (List<T>) this.resultHolder;
    }

    @Override
    public void cleanup() {
        pendingRelations.clear();
        autoMappingsCache.clear();
        constructorAutoMappingColumns.clear();
        nestedResultObjects.clear();
        ancestorObjects.clear();
        resultHolder.clear();
    }

    /**
     * get row value for simple result map
     *
     * @param rowResultWrapper the RowResultWrapper
     * @param resultMap        the ResultMap
     * @param columnPrefix     the columnPrefix
     * @return data
     * @throws SQLException SQLException
     */
    private Object getRowValueForSimpleResultMap(RowResultWrapper rowResultWrapper, ResultMap resultMap, String columnPrefix) throws SQLException {
        Object rowValue = createResultObject(rowResultWrapper, resultMap, columnPrefix);
        if (rowValue != null && !hasTypeHandlerForResultObject(resultMap.getType())) {
            final MetaObject metaObject = r2dbcMybatisConfiguration.newMetaObject(rowValue);
            boolean foundValues = this.useConstructorMappings;
            if (shouldApplyAutomaticMappings(resultMap, false)) {
                foundValues = applyAutomaticMappings(rowResultWrapper, resultMap, metaObject, columnPrefix) || foundValues;
            }
            foundValues = applyPropertyMappings(rowResultWrapper, resultMap, metaObject, columnPrefix) || foundValues;
            rowValue = foundValues || r2dbcMybatisConfiguration.isReturnInstanceForEmptyRow() ? rowValue : null;
        }
        return rowValue;
    }

    /**
     * handle row values for nested resultMap
     *
     * @param rowResultWrapper the RowResultWrapper
     * @param resultMap        the ResultMap
     * @throws SQLException the SQLException
     */
    private Object handleRowValuesForNestedResultMap(RowResultWrapper rowResultWrapper, ResultMap resultMap) throws SQLException {
        final DefaultResultHandler resultHandler = new DefaultResultHandler(objectFactory);
        final DefaultResultContext<Object> resultContext = new DefaultResultContext<>();
        final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rowResultWrapper, resultMap, null);
        final CacheKey rowKey = createRowKey(discriminatedResultMap, rowResultWrapper, null);
        Object partialObject = nestedResultObjects.get(rowKey);
        Object rowValue = getRowValueForNestedResultMap(rowResultWrapper, discriminatedResultMap, rowKey, null, partialObject);
        if (partialObject == null) {
            storeObject(resultHandler, resultContext, rowValue, null, rowResultWrapper);
        }
        List<Object> resultList = resultHandler.getResultList();
        if(resultList == null || resultList.isEmpty()){
            return DEFERRED;
        }
        // if result is not ordered , then hold all results for nested result mapping
        if(!mappedStatement.isResultOrdered()){
            this.resultHolder.addAll(resultList);
            return DEFERRED;
        }
        // result is ordered,then hold before next nested result mapping

        // result holder has value then return hold results and clear hold results
        if(!this.resultHolder.isEmpty()){
            Object resultRowValue = this.resultHolder.get(0);
            this.resultHolder.clear();
            this.resultHolder.addAll(resultList);
            return resultRowValue;
        }
        // result holder is empty then hold result
        this.resultHolder.addAll(resultList);
        return DEFERRED;
    }

    private boolean applyNestedResultMappings(RowResultWrapper rowResultWrapper, ResultMap resultMap, MetaObject metaObject, String parentPrefix, CacheKey parentRowKey, boolean newObject) {
        boolean foundValues = false;
        for (ResultMapping resultMapping : resultMap.getPropertyResultMappings()) {
            final String nestedResultMapId = resultMapping.getNestedResultMapId();
            if (nestedResultMapId != null && resultMapping.getResultSet() == null) {
                try {
                    final String columnPrefix = getColumnPrefix(parentPrefix, resultMapping);
                    final ResultMap nestedResultMap = getNestedResultMap(rowResultWrapper, nestedResultMapId, columnPrefix);
                    if (resultMapping.getColumnPrefix() == null) {
                        // try to fill circular reference only when columnPrefix
                        // is not specified for the nested result map (issue #215)
                        Object ancestorObject = ancestorObjects.get(nestedResultMapId);
                        if (ancestorObject != null) {
                            if (newObject) {
                                linkObjects(metaObject, resultMapping, ancestorObject); // issue #385
                            }
                            continue;
                        }
                    }
                    final CacheKey rowKey = createRowKey(nestedResultMap, rowResultWrapper, columnPrefix);
                    final CacheKey combinedKey = combineKeys(rowKey, parentRowKey);
                    Object rowValue = nestedResultObjects.get(combinedKey);
                    boolean knownValue = rowValue != null;
                    instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject); // mandatory
                    if (anyNotNullColumnHasValue(resultMapping, columnPrefix, rowResultWrapper)) {
                        rowValue = getRowValueForNestedResultMap(rowResultWrapper, nestedResultMap, combinedKey, columnPrefix, rowValue);
                        if (rowValue != null && !knownValue) {
                            linkObjects(metaObject, resultMapping, rowValue);
                            foundValues = true;
                        }
                    }
                } catch (SQLException e) {
                    throw new ExecutorException("Error getting nested result map values for '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
                }
            }
        }
        return foundValues;
    }

    private Object getRowValueForNestedResultMap(RowResultWrapper rowResultWrapper, ResultMap resultMap, CacheKey combinedKey, String columnPrefix, Object partialObject) throws SQLException {
        final String resultMapId = resultMap.getId();
        Object rowValue = partialObject;
        if (rowValue != null) {
            final MetaObject metaObject = r2dbcMybatisConfiguration.newMetaObject(rowValue);
            putAncestor(rowValue, resultMapId);
            applyNestedResultMappings(rowResultWrapper, resultMap, metaObject, columnPrefix, combinedKey, false);
            ancestorObjects.remove(resultMapId);
        } else {
            rowValue = createResultObject(rowResultWrapper, resultMap, columnPrefix);
            if (rowValue != null && !hasTypeHandlerForResultObject(resultMap.getType())) {
                final MetaObject metaObject = r2dbcMybatisConfiguration.newMetaObject(rowValue);
                boolean foundValues = this.useConstructorMappings;
                if (shouldApplyAutomaticMappings(resultMap, true)) {
                    foundValues = applyAutomaticMappings(rowResultWrapper, resultMap, metaObject, columnPrefix) || foundValues;
                }
                foundValues = applyPropertyMappings(rowResultWrapper, resultMap, metaObject, columnPrefix) || foundValues;
                putAncestor(rowValue, resultMapId);
                foundValues = applyNestedResultMappings(rowResultWrapper, resultMap, metaObject, columnPrefix, combinedKey, true) || foundValues;
                ancestorObjects.remove(resultMapId);
                rowValue = foundValues || r2dbcMybatisConfiguration.isReturnInstanceForEmptyRow() ? rowValue : null;
            }
            if (combinedKey != CacheKey.NULL_CACHE_KEY) {
                nestedResultObjects.put(combinedKey, rowValue);
            }
        }
        return rowValue;
    }

    private boolean applyPropertyMappings(RowResultWrapper rowResultWrapper, ResultMap resultMap, MetaObject metaObject, String columnPrefix)
            throws SQLException {
        final List<String> mappedColumnNames = rowResultWrapper.getMappedColumnNames(resultMap, columnPrefix);
        boolean foundValues = false;
        final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
        for (ResultMapping propertyMapping : propertyMappings) {
            String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
            if (propertyMapping.getNestedResultMapId() != null) {
                // the user added a column attribute to a nested result map, ignore it
                column = null;
            }
            if (propertyMapping.isCompositeResult()
                    || (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH)))
                    || propertyMapping.getResultSet() != null) {
                Object value = getPropertyMappingValue(rowResultWrapper, metaObject, propertyMapping, columnPrefix);
                // issue #541 make property optional
                final String property = propertyMapping.getProperty();
                if (property == null) {
                    continue;
                } else if (value == DEFERRED) {
                    foundValues = true;
                    continue;
                }
                if (value != null) {
                    foundValues = true;
                }
                if (value != null || (r2dbcMybatisConfiguration.isCallSettersOnNulls() && !metaObject.getSetterType(property).isPrimitive())) {
                    // gcode issue #377, call setter on nulls (value is not 'found')
                    metaObject.setValue(property, value);
                }
            }
        }
        return foundValues;
    }

    private Object getPropertyMappingValue(RowResultWrapper rowResultWrapper, MetaObject metaResultObject, ResultMapping propertyMapping, String columnPrefix)
            throws SQLException {
        if (propertyMapping.getNestedQueryId() != null) {
            throw new UnsupportedOperationException("Not supported Nested query ");
        } else {
            final TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
            final String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
            ((TypeHandleContext) this.delegatedTypeHandler).contextWith(propertyMapping.getJavaType(),typeHandler, rowResultWrapper);
            return this.delegatedTypeHandler.getResult(null, column);
        }
    }

    private List<DefaultReactiveResultHandler.UnMappedColumnAutoMapping> createAutomaticMappings(RowResultWrapper rowResultWrapper, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        final String mapKey = resultMap.getId() + ":" + columnPrefix;
        List<DefaultReactiveResultHandler.UnMappedColumnAutoMapping> autoMapping = autoMappingsCache.get(mapKey);
        if (autoMapping == null) {
            autoMapping = new ArrayList<>();
            final List<String> unmappedColumnNames = rowResultWrapper.getUnmappedColumnNames(resultMap, columnPrefix);
            // Remove the entry to release the memory
            List<String> mappedInConstructorAutoMapping = constructorAutoMappingColumns.remove(mapKey);
            if (mappedInConstructorAutoMapping != null) {
                unmappedColumnNames.removeAll(mappedInConstructorAutoMapping);
            }
            for (String columnName : unmappedColumnNames) {
                String propertyName = columnName;
                if (columnPrefix != null && !columnPrefix.isEmpty()) {
                    // When columnPrefix is specified,
                    // ignore columns without the prefix.
                    if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                        propertyName = columnName.substring(columnPrefix.length());
                    } else {
                        continue;
                    }
                }
                final String property = metaObject.findProperty(propertyName, r2dbcMybatisConfiguration.isMapUnderscoreToCamelCase());
                if (property != null && metaObject.hasSetter(property)) {
                    if (resultMap.getMappedProperties().contains(property)) {
                        continue;
                    }
                    final Class<?> propertyType = metaObject.getSetterType(property);
                    if (typeHandlerRegistry.hasTypeHandler(propertyType)) {
                        final TypeHandler<?> typeHandler = rowResultWrapper.getTypeHandler(propertyType, columnName);
                        autoMapping.add(new DefaultReactiveResultHandler.UnMappedColumnAutoMapping(columnName, property, propertyType, typeHandler, propertyType.isPrimitive()));
                    } else {
                        r2dbcMybatisConfiguration.getAutoMappingUnknownColumnBehavior()
                                .doAction(mappedStatement, columnName, property, propertyType);
                    }
                } else {
                    r2dbcMybatisConfiguration.getAutoMappingUnknownColumnBehavior()
                            .doAction(mappedStatement, columnName, (property != null) ? property : propertyName, null);
                }
            }
            autoMappingsCache.put(mapKey, autoMapping);
        }
        return autoMapping;
    }

    private boolean applyAutomaticMappings(RowResultWrapper rowResultWrapper, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        List<DefaultReactiveResultHandler.UnMappedColumnAutoMapping> autoMapping = createAutomaticMappings(rowResultWrapper, resultMap, metaObject, columnPrefix);
        boolean foundValues = false;
        if (!autoMapping.isEmpty()) {
            for (DefaultReactiveResultHandler.UnMappedColumnAutoMapping mapping : autoMapping) {
                TypeHandler<?> typeHandler = mapping.typeHandler;
                ((TypeHandleContext) this.delegatedTypeHandler).contextWith(mapping.propertyType,typeHandler, rowResultWrapper);
                final Object value = this.delegatedTypeHandler.getResult(null, mapping.column);
                if (value != null) {
                    foundValues = true;
                }
                if (value != null || (r2dbcMybatisConfiguration.isCallSettersOnNulls() && !mapping.primitive)) {
                    // gcode issue #377, call setter on nulls (value is not 'found')
                    metaObject.setValue(mapping.property, value);
                }
            }
        }
        return foundValues;
    }

    private Object createResultObject(RowResultWrapper rowResultWrapper, ResultMap resultMap, String columnPrefix) throws SQLException {
        this.useConstructorMappings = false; // reset previous mapping result
        final List<Class<?>> constructorArgTypes = new ArrayList<>();
        final List<Object> constructorArgs = new ArrayList<>();
        Object resultObject = createResultObject(rowResultWrapper, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
        this.useConstructorMappings = resultObject != null && !constructorArgTypes.isEmpty(); // set current mapping result
        return resultObject;
    }

    private Object createResultObject(RowResultWrapper rowResultWrapper, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix)
            throws SQLException {
        final Class<?> resultType = resultMap.getType();
        final MetaClass metaType = MetaClass.forClass(resultType, reflectorFactory);
        final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
        if (hasTypeHandlerForResultObject(resultType)) {
            return createPrimitiveResultObject(rowResultWrapper, resultMap, columnPrefix);
        } else if (!constructorMappings.isEmpty()) {
            return createParameterizedResultObject(rowResultWrapper, resultType, constructorMappings, constructorArgTypes, constructorArgs, columnPrefix);
        } else if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
            return objectFactory.create(resultType);
        } else if (shouldApplyAutomaticMappings(resultMap, false)) {
            return createByConstructorSignature(rowResultWrapper, resultMap, columnPrefix, resultType, constructorArgTypes, constructorArgs);
        }
        throw new ExecutorException("Do not know how to create an instance of " + resultType);
    }

    private Object createParameterizedResultObject(RowResultWrapper rowResultWrapper, Class<?> resultType, List<ResultMapping> constructorMappings,
                                                   List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) {
        boolean foundValues = false;
        for (ResultMapping constructorMapping : constructorMappings) {
            final Class<?> parameterType = constructorMapping.getJavaType();
            final String column = constructorMapping.getColumn();
            final Object value;
            try {
                if (constructorMapping.getNestedQueryId() != null) {
                    throw new UnsupportedOperationException("Unsupported constructor with nested query :" + constructorMapping.getNestedQueryId());
                } else if (constructorMapping.getNestedResultMapId() != null) {
                    String constructorColumnPrefix = getColumnPrefix(columnPrefix, constructorMapping);
                    final ResultMap resultMap = resolveDiscriminatedResultMap(rowResultWrapper,
                            r2dbcMybatisConfiguration.getResultMap(constructorMapping.getNestedResultMapId()), constructorColumnPrefix);
                    value = getRowValueForSimpleResultMap(rowResultWrapper, resultMap, constructorColumnPrefix);
                } else {
                    final TypeHandler<?> typeHandler = constructorMapping.getTypeHandler();
                    ((TypeHandleContext) this.delegatedTypeHandler).contextWith(constructorMapping.getJavaType(),typeHandler, rowResultWrapper);
                    value = this.delegatedTypeHandler.getResult(null, prependPrefix(column, columnPrefix));
                }
            } catch (ResultMapException | SQLException e) {
                throw new ExecutorException("Could not process result for mapping: " + constructorMapping, e);
            }
            constructorArgTypes.add(parameterType);
            constructorArgs.add(value);
            foundValues = value != null || foundValues;
        }
        return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
    }

    private Object createByConstructorSignature(RowResultWrapper rowResultWrapper, ResultMap resultMap, String columnPrefix, Class<?> resultType,
                                                List<Class<?>> constructorArgTypes, List<Object> constructorArgs) throws SQLException {
        return applyConstructorAutomapping(rowResultWrapper, resultMap, columnPrefix, resultType, constructorArgTypes, constructorArgs,
                findConstructorForAutomapping(resultType).orElseThrow(() -> new ExecutorException(
                        "No constructor found in " + resultType.getName() + " matching " + rowResultWrapper.getClassNames())));
    }

    private Optional<Constructor<?>> findConstructorForAutomapping(final Class<?> resultType) {
        Constructor<?>[] constructors = resultType.getDeclaredConstructors();
        if (constructors.length == 1) {
            return Optional.of(constructors[0]);
        }
        Optional<Constructor<?>> annotated = Arrays.stream(constructors)
                .filter(x -> x.isAnnotationPresent(AutomapConstructor.class))
                .reduce((x, y) -> {
                    throw new ExecutorException("@AutomapConstructor should be used in only one constructor.");
                });
        if (annotated.isPresent()) {
            return annotated;
        } else if (r2dbcMybatisConfiguration.isArgNameBasedConstructorAutoMapping()) {
            // Finding-best-match type implementation is possible,
            // but using @AutomapConstructor seems sufficient.
            throw new ExecutorException(MessageFormat.format(
                    "'argNameBasedConstructorAutoMapping' is enabled and the class ''{0}'' has multiple constructors, so @AutomapConstructor must be added to one of the constructors.",
                    resultType.getName()));
        } else {
            return Arrays.stream(constructors).filter(this::findUsableConstructorByArgTypes).findAny();
        }
    }

    private boolean findUsableConstructorByArgTypes(final Constructor<?> constructor) {
        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!typeHandlerRegistry.hasTypeHandler(parameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private Object applyConstructorAutomapping(RowResultWrapper rowResultWrapper, ResultMap resultMap, String columnPrefix, Class<?> resultType, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, Constructor<?> constructor) throws SQLException {
        boolean foundValues = false;
        if (r2dbcMybatisConfiguration.isArgNameBasedConstructorAutoMapping()) {
            foundValues = applyArgNameBasedConstructorAutoMapping(rowResultWrapper, resultMap, columnPrefix, constructorArgTypes, constructorArgs,
                    constructor, foundValues);
        } else {
            foundValues = applyColumnOrderBasedConstructorAutomapping(rowResultWrapper, constructorArgTypes, constructorArgs, constructor,
                    foundValues);
        }
        return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
    }

    private boolean applyColumnOrderBasedConstructorAutomapping(RowResultWrapper rowResultWrapper, List<Class<?>> constructorArgTypes,
                                                                List<Object> constructorArgs, Constructor<?> constructor, boolean foundValues) throws SQLException {
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            Class<?> parameterType = constructor.getParameterTypes()[i];
            String columnName = rowResultWrapper.getColumnNames().get(i);
            final TypeHandler<?> typeHandler = rowResultWrapper.getTypeHandler(parameterType, columnName);
            ((TypeHandleContext) this.delegatedTypeHandler).contextWith(parameterType,typeHandler, rowResultWrapper);
            Object value = delegatedTypeHandler.getResult(null, columnName);
            constructorArgTypes.add(parameterType);
            constructorArgs.add(value);
            foundValues = value != null || foundValues;
        }
        return foundValues;
    }

    private boolean applyArgNameBasedConstructorAutoMapping(RowResultWrapper rowResultWrapper, ResultMap resultMap, String columnPrefix,
                                                            List<Class<?>> constructorArgTypes, List<Object> constructorArgs, Constructor<?> constructor, boolean foundValues)
            throws SQLException {
        List<String> missingArgs = null;
        Parameter[] params = constructor.getParameters();
        for (Parameter param : params) {
            boolean columnNotFound = true;
            Param paramAnno = param.getAnnotation(Param.class);
            String paramName = paramAnno == null ? param.getName() : paramAnno.value();
            for (String columnName : rowResultWrapper.getColumnNames()) {
                if (columnMatchesParam(columnName, paramName, columnPrefix)) {
                    Class<?> paramType = param.getType();
                    TypeHandler<?> typeHandler = rowResultWrapper.getTypeHandler(paramType, columnName);
                    ((TypeHandleContext) this.delegatedTypeHandler).contextWith(paramType,typeHandler, rowResultWrapper);
                    Object value = this.delegatedTypeHandler.getResult(null, columnName);
                    constructorArgTypes.add(paramType);
                    constructorArgs.add(value);
                    final String mapKey = resultMap.getId() + ":" + columnPrefix;
                    if (!autoMappingsCache.containsKey(mapKey)) {
                        MapUtil.computeIfAbsent(constructorAutoMappingColumns, mapKey, k -> new ArrayList<>()).add(columnName);
                    }
                    columnNotFound = false;
                    foundValues = value != null || foundValues;
                }
            }
            if (columnNotFound) {
                if (missingArgs == null) {
                    missingArgs = new ArrayList<>();
                }
                missingArgs.add(paramName);
            }
        }
        if (foundValues && constructorArgs.size() < params.length) {
            throw new ExecutorException(MessageFormat.format("Constructor auto-mapping of ''{1}'' failed "
                            + "because ''{0}'' were not found in the result set; "
                            + "Available columns are ''{2}'' and mapUnderscoreToCamelCase is ''{3}''.",
                    missingArgs, constructor, rowResultWrapper.getColumnNames(), r2dbcMybatisConfiguration.isMapUnderscoreToCamelCase()));
        }
        return foundValues;
    }

    private boolean columnMatchesParam(String columnName, String paramName, String columnPrefix) {
        if (columnPrefix != null) {
            if (!columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                return false;
            }
            columnName = columnName.substring(columnPrefix.length());
        }
        return paramName
                .equalsIgnoreCase(r2dbcMybatisConfiguration.isMapUnderscoreToCamelCase() ? columnName.replace("_", "") : columnName);
    }

    /**
     * create Primitive ResultObject
     *
     * @param rowResultWrapper
     * @param resultMap
     * @param columnPrefix
     * @return
     * @throws SQLException
     */
    private Object createPrimitiveResultObject(RowResultWrapper rowResultWrapper, ResultMap resultMap, String columnPrefix) throws SQLException {
        final Class<?> resultType = resultMap.getType();
        final String columnName;
        if (!resultMap.getResultMappings().isEmpty()) {
            final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
            final ResultMapping mapping = resultMappingList.get(0);
            columnName = prependPrefix(mapping.getColumn(), columnPrefix);
        } else {
            columnName = rowResultWrapper.getColumnNames().get(0);
        }
        final TypeHandler<?> typeHandler = rowResultWrapper.getTypeHandler(resultType, columnName);
        ((TypeHandleContext) this.delegatedTypeHandler).contextWith(resultType,typeHandler, rowResultWrapper);
        return delegatedTypeHandler.getResult(null, columnName);
    }

    /**
     * resolve Discriminated ResultMap
     *
     * @param rowResultWrapper the row result wrapper
     * @param resultMap        the result map
     * @param columnPrefix     the column prefix
     * @return result map
     * @throws SQLException the sql exception
     */
    public ResultMap resolveDiscriminatedResultMap(RowResultWrapper rowResultWrapper, ResultMap resultMap, String columnPrefix) throws SQLException {
        Set<String> pastDiscriminators = new HashSet<>();
        Discriminator discriminator = resultMap.getDiscriminator();
        while (discriminator != null) {
            final Object value = getDiscriminatorValue(rowResultWrapper, discriminator, columnPrefix);
            final String discriminatedMapId = discriminator.getMapIdFor(String.valueOf(value));
            if (r2dbcMybatisConfiguration.hasResultMap(discriminatedMapId)) {
                resultMap = r2dbcMybatisConfiguration.getResultMap(discriminatedMapId);
                Discriminator lastDiscriminator = discriminator;
                discriminator = resultMap.getDiscriminator();
                if (discriminator == lastDiscriminator || !pastDiscriminators.add(discriminatedMapId)) {
                    break;
                }
            } else {
                break;
            }
        }
        return resultMap;
    }

    /**
     * get discriminator value
     *
     * @param rowResultWrapper
     * @param discriminator
     * @param columnPrefix
     * @return
     * @throws SQLException
     */
    private Object getDiscriminatorValue(RowResultWrapper rowResultWrapper, Discriminator discriminator, String columnPrefix) throws SQLException {
        final ResultMapping resultMapping = discriminator.getResultMapping();
        final TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
        ((TypeHandleContext) this.delegatedTypeHandler).contextWith(resultMapping.getJavaType(),typeHandler, rowResultWrapper);
        return delegatedTypeHandler.getResult(null, prependPrefix(resultMapping.getColumn(), columnPrefix));
    }

    /**
     * store object
     *
     * @param resultHandler
     * @param resultContext
     * @param rowValue
     * @param parentMapping
     * @param rowResultWrapper
     */
    @SuppressWarnings("unchecked" /* because ResultHandler<?> is always ResultHandler<Object>*/)
    private void storeObject(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue, ResultMapping parentMapping, RowResultWrapper rowResultWrapper) {
        if (parentMapping != null) {
            linkToParents(rowResultWrapper, parentMapping, rowValue);
        } else {
            resultContext.nextResultObject(rowValue);
            ((ResultHandler<Object>) resultHandler).handleResult(resultContext);
        }
    }

    private boolean hasTypeHandlerForResultObject(Class<?> resultType) {
        return typeHandlerRegistry.hasTypeHandler(resultType);
    }

    private void linkToParents(RowResultWrapper rowResultWrapper, ResultMapping parentMapping, Object rowValue) {
        CacheKey parentKey = createKeyForMultipleResults(rowResultWrapper, parentMapping, parentMapping.getColumn(), parentMapping.getForeignColumn());
        List<DefaultReactiveResultHandler.PendingRelation> parents = pendingRelations.get(parentKey);
        if (parents != null) {
            for (DefaultReactiveResultHandler.PendingRelation parent : parents) {
                if (parent != null && rowValue != null) {
                    linkObjects(parent.metaObject, parent.propertyMapping, rowValue);
                }
            }
        }
    }

    private CacheKey createKeyForMultipleResults(RowResultWrapper rowResultWrapper, ResultMapping resultMapping, String names, String columns) {
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(resultMapping);
        if (columns != null && names != null) {
            String[] columnsArray = columns.split(",");
            String[] namesArray = names.split(",");
            Row row = rowResultWrapper.getRow();
            for (int i = 0; i < columnsArray.length; i++) {
                Object value = row.get(columnsArray[i]);
                if (value != null) {
                    cacheKey.update(namesArray[i]);
                    cacheKey.update(value);
                }
            }
        }
        return cacheKey;
    }

    private void putAncestor(Object resultObject, String resultMapId) {
        ancestorObjects.put(resultMapId, resultObject);
    }

    private boolean shouldApplyAutomaticMappings(ResultMap resultMap, boolean isNested) {
        if (resultMap.getAutoMapping() != null) {
            return resultMap.getAutoMapping();
        } else {
            if (isNested) {
                return AutoMappingBehavior.FULL == r2dbcMybatisConfiguration.getAutoMappingBehavior();
            } else {
                return AutoMappingBehavior.NONE != r2dbcMybatisConfiguration.getAutoMappingBehavior();
            }
        }
    }

    private String prependPrefix(String columnName, String prefix) {
        if (columnName == null || columnName.length() == 0 || prefix == null || prefix.length() == 0) {
            return columnName;
        }
        return prefix + columnName;
    }

    private String getColumnPrefix(String parentPrefix, ResultMapping resultMapping) {
        final StringBuilder columnPrefixBuilder = new StringBuilder();
        if (parentPrefix != null) {
            columnPrefixBuilder.append(parentPrefix);
        }
        if (resultMapping.getColumnPrefix() != null) {
            columnPrefixBuilder.append(resultMapping.getColumnPrefix());
        }
        return columnPrefixBuilder.length() == 0 ? null : columnPrefixBuilder.toString().toUpperCase(Locale.ENGLISH);
    }

    private boolean anyNotNullColumnHasValue(ResultMapping resultMapping, String columnPrefix, RowResultWrapper rowResultWrapper) throws SQLException {
        Set<String> notNullColumns = resultMapping.getNotNullColumns();
        if (notNullColumns != null && !notNullColumns.isEmpty()) {
            Row row = rowResultWrapper.getRow();
            for (String column : notNullColumns) {
                if (row.get(prependPrefix(column, columnPrefix)) != null) {
                    return true;
                }
            }
            return false;
        } else if (columnPrefix != null) {
            for (String columnName : rowResultWrapper.getColumnNames()) {
                if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix.toUpperCase(Locale.ENGLISH))) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private ResultMap getNestedResultMap(RowResultWrapper rowResultWrapper, String nestedResultMapId, String columnPrefix) throws SQLException {
        ResultMap nestedResultMap = r2dbcMybatisConfiguration.getResultMap(nestedResultMapId);
        return resolveDiscriminatedResultMap(rowResultWrapper, nestedResultMap, columnPrefix);
    }

    private CacheKey createRowKey(ResultMap resultMap, RowResultWrapper rowResultWrapper, String columnPrefix) throws SQLException {
        final CacheKey cacheKey = new CacheKey();
        cacheKey.update(resultMap.getId());
        List<ResultMapping> resultMappings = getResultMappingsForRowKey(resultMap);
        if (resultMappings.isEmpty()) {
            if (Map.class.isAssignableFrom(resultMap.getType())) {
                createRowKeyForMap(rowResultWrapper, cacheKey);
            } else {
                createRowKeyForUnmappedProperties(resultMap, rowResultWrapper, cacheKey, columnPrefix);
            }
        } else {
            createRowKeyForMappedProperties(resultMap, rowResultWrapper, cacheKey, resultMappings, columnPrefix);
        }
        if (cacheKey.getUpdateCount() < 2) {
            return CacheKey.NULL_CACHE_KEY;
        }
        return cacheKey;
    }

    private CacheKey combineKeys(CacheKey rowKey, CacheKey parentRowKey) {
        if (rowKey.getUpdateCount() > 1 && parentRowKey.getUpdateCount() > 1) {
            CacheKey combinedKey;
            try {
                combinedKey = rowKey.clone();
            } catch (CloneNotSupportedException e) {
                throw new ExecutorException("Error cloning cache key.  Cause: " + e, e);
            }
            combinedKey.update(parentRowKey);
            return combinedKey;
        }
        return CacheKey.NULL_CACHE_KEY;
    }

    private List<ResultMapping> getResultMappingsForRowKey(ResultMap resultMap) {
        List<ResultMapping> resultMappings = resultMap.getIdResultMappings();
        if (resultMappings.isEmpty()) {
            resultMappings = resultMap.getPropertyResultMappings();
        }
        return resultMappings;
    }

    private void createRowKeyForMappedProperties(ResultMap resultMap, RowResultWrapper rowResultWrapper, CacheKey cacheKey, List<ResultMapping> resultMappings, String columnPrefix) throws SQLException {
        for (ResultMapping resultMapping : resultMappings) {
            if (resultMapping.isSimple()) {
                final String column = prependPrefix(resultMapping.getColumn(), columnPrefix);
                final TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
                List<String> mappedColumnNames = rowResultWrapper.getMappedColumnNames(resultMap, columnPrefix);
                // Issue #114
                if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
                    ((TypeHandleContext) this.delegatedTypeHandler).contextWith(resultMapping.getJavaType(),typeHandler, rowResultWrapper);
                    final Object value = this.delegatedTypeHandler.getResult(null, column);
                    if (value != null || r2dbcMybatisConfiguration.isReturnInstanceForEmptyRow()) {
                        cacheKey.update(column);
                        cacheKey.update(value);
                    }
                }
            }
        }
    }

    private void createRowKeyForUnmappedProperties(ResultMap resultMap, RowResultWrapper rowResultWrapper, CacheKey cacheKey, String columnPrefix) throws SQLException {
        final MetaClass metaType = MetaClass.forClass(resultMap.getType(), reflectorFactory);
        List<String> unmappedColumnNames = rowResultWrapper.getUnmappedColumnNames(resultMap, columnPrefix);
        for (String column : unmappedColumnNames) {
            String property = column;
            if (columnPrefix != null && !columnPrefix.isEmpty()) {
                // When columnPrefix is specified, ignore columns without the prefix.
                if (column.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                    property = column.substring(columnPrefix.length());
                } else {
                    continue;
                }
            }
            if (metaType.findProperty(property, r2dbcMybatisConfiguration.isMapUnderscoreToCamelCase()) != null) {
                String value = rowResultWrapper.getRow().get(column, String.class);
                if (value != null) {
                    cacheKey.update(column);
                    cacheKey.update(value);
                }
            }
        }
    }

    private void createRowKeyForMap(RowResultWrapper rowResultWrapper, CacheKey cacheKey) {
        List<String> columnNames = rowResultWrapper.getColumnNames();
        for (String columnName : columnNames) {
            final String value = rowResultWrapper.getRow().get(columnName, String.class);
            if (value != null) {
                cacheKey.update(columnName);
                cacheKey.update(value);
            }
        }
    }

    private void linkObjects(MetaObject metaObject, ResultMapping resultMapping, Object rowValue) {
        final Object collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);
        if (collectionProperty != null) {
            final MetaObject targetMetaObject = r2dbcMybatisConfiguration.newMetaObject(collectionProperty);
            targetMetaObject.add(rowValue);
        } else {
            metaObject.setValue(resultMapping.getProperty(), rowValue);
        }
    }

    private Object instantiateCollectionPropertyIfAppropriate(ResultMapping resultMapping, MetaObject metaObject) {
        final String propertyName = resultMapping.getProperty();
        Object propertyValue = metaObject.getValue(propertyName);
        if (propertyValue == null) {
            Class<?> type = resultMapping.getJavaType();
            if (type == null) {
                type = metaObject.getSetterType(propertyName);
            }
            try {
                if (objectFactory.isCollection(type)) {
                    propertyValue = objectFactory.create(type);
                    metaObject.setValue(propertyName, propertyValue);
                    return propertyValue;
                }
            } catch (Exception e) {
                throw new ExecutorException("Error instantiating collection property for result '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
            }
        } else if (objectFactory.isCollection(propertyValue.getClass())) {
            return propertyValue;
        }
        return null;
    }

    /**
     * get delegate type handler
     *
     * @return TypeHandler
     */
    private TypeHandler<?> initDelegateTypeHandler() {
        return ProxyInstanceFactory.newInstanceOfInterfaces(
                TypeHandler.class,
                () -> new DelegateR2dbcResultRowDataHandler(
                        this.r2dbcMybatisConfiguration.getNotSupportedDataTypes(),
                        this.r2dbcMybatisConfiguration.getR2dbcTypeHandlerAdapterRegistry().getR2dbcTypeHandlerAdapters()
                ),
                TypeHandleContext.class
        );
    }

    private static class PendingRelation {
        /**
         * The Meta object.
         */
        public MetaObject metaObject;
        /**
         * The Property mapping.
         */
        public ResultMapping propertyMapping;
    }

    private static class UnMappedColumnAutoMapping {
        private final String column;
        private final String property;
        private final Class<?> propertyType;
        private final TypeHandler<?> typeHandler;
        private final boolean primitive;

        /**
         * Instantiates a new Un mapped column auto mapping.
         *
         * @param column       the column
         * @param property     the property
         * @param propertyType the property type
         * @param typeHandler  the type handler
         * @param primitive    the primitive
         */
        public UnMappedColumnAutoMapping(String column, String property, Class<?> propertyType, TypeHandler<?> typeHandler, boolean primitive) {
            this.column = column;
            this.property = property;
            this.propertyType = propertyType;
            this.typeHandler = typeHandler;
            this.primitive = primitive;
        }
    }
}
