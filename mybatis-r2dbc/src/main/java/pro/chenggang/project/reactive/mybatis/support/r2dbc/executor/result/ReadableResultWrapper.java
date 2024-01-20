/*
 *    Copyright 2009-2024 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result;

import io.r2dbc.spi.OutParameters;
import io.r2dbc.spi.Readable;
import io.r2dbc.spi.ReadableMetadata;
import io.r2dbc.spi.Row;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper.Functions.OUT_PARAMETERS_METADATA_EXTRACTOR;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper.Functions.OUT_PARAMETERS_METADATA_EXTRACTOR_BY_INDEX;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper.Functions.OUT_PARAMETERS_METADATA_EXTRACTOR_BY_NAME;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper.Functions.ROW_METADATA_EXTRACTOR;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper.Functions.ROW_METADATA_EXTRACTOR_BY_INDEX;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper.Functions.ROW_METADATA_EXTRACTOR_BY_NAME;

/**
 * The type Row result wrapper.
 * <p>
 * {@link org.apache.ibatis.executor.resultset.ResultSetWrapper}
 *
 * @param <T> the type parameter
 * @author Gang Cheng
 */
public class ReadableResultWrapper<T extends Readable> {

    /**
     * The RowResultWrapper Support Functions.
     *
     * @author Gang Cheng
     */
    public abstract static class Functions {

        /**
         * The constant ROW_METADATA_EXTRACTOR.
         * {@code Function<Row, List<? extends ReadableMetadata>>}
         */
        public static final Function<Row, List<? extends ReadableMetadata>> ROW_METADATA_EXTRACTOR
                = row -> row.getMetadata().getColumnMetadatas();
        /**
         * The constant OUT_PARAMETERS_METADATA_EXTRACTOR.
         * {@code Function<Row, List<? extends ReadableMetadata>>}
         */
        public static final Function<OutParameters, List<? extends ReadableMetadata>> OUT_PARAMETERS_METADATA_EXTRACTOR
                = outParameters -> outParameters.getMetadata().getParameterMetadatas();

        /**
         * The constant ROW_METADATA_EXTRACTOR_BY_INDEX.
         * {@code BiFunction<Row, Integer, ReadableMetadata>}
         */
        public static final BiFunction<Row, Integer, ReadableMetadata> ROW_METADATA_EXTRACTOR_BY_INDEX
                = (row, index) -> row.getMetadata().getColumnMetadata(index);
        /**
         * The constant ROW_METADATA_EXTRACTOR_BY_NAME.
         * {@code BiFunction<Row, String, ReadableMetadata>}
         */
        public static final BiFunction<Row, String, ReadableMetadata> ROW_METADATA_EXTRACTOR_BY_NAME
                = (row, name) -> row.getMetadata().getColumnMetadata(name);
        /**
         * The constant OUT_PARAMETERS_METADATA_EXTRACTOR_BY_INDEX.
         * {@code BiFunction<OutParameters, Integer, ReadableMetadata>}
         */
        public static final BiFunction<OutParameters, Integer, ReadableMetadata> OUT_PARAMETERS_METADATA_EXTRACTOR_BY_INDEX
                = (outParameters, index) -> outParameters.getMetadata().getParameterMetadata(index);
        /**
         * The constant OUT_PARAMETERS_METADATA_EXTRACTOR_BY_NAME.
         * {@code BiFunction<OutParameters, String, ReadableMetadata>}
         */
        public static final BiFunction<OutParameters, String, ReadableMetadata> OUT_PARAMETERS_METADATA_EXTRACTOR_BY_NAME
                = (outParameters, name) -> outParameters.getMetadata().getParameterMetadata(name);
    }

    /**
     * New result wrapper of row readable.
     *
     * @param row                       the row
     * @param r2dbcMybatisConfiguration the r2dbc mybatis configuration
     * @return the readable result wrapper
     */
    public static ReadableResultWrapper<Row> ofRow(Row row, R2dbcMybatisConfiguration r2dbcMybatisConfiguration) {
        return new ReadableResultWrapper<>(
                row,
                ROW_METADATA_EXTRACTOR,
                ROW_METADATA_EXTRACTOR_BY_INDEX,
                ROW_METADATA_EXTRACTOR_BY_NAME,
                r2dbcMybatisConfiguration
        );
    }

    /**
     * New result wrapper of out parameters.
     *
     * @param outParameters             the out parameters
     * @param r2dbcMybatisConfiguration the r2dbc mybatis configuration
     * @return the readable result wrapper
     */
    public static ReadableResultWrapper<OutParameters> ofOutParameters(OutParameters outParameters,
                                                                       R2dbcMybatisConfiguration r2dbcMybatisConfiguration) {
        return new ReadableResultWrapper<>(
                outParameters,
                OUT_PARAMETERS_METADATA_EXTRACTOR,
                OUT_PARAMETERS_METADATA_EXTRACTOR_BY_INDEX,
                OUT_PARAMETERS_METADATA_EXTRACTOR_BY_NAME,
                r2dbcMybatisConfiguration
        );
    }

    private final T readable;
    private final BiFunction<T, Integer, ReadableMetadata> metadataExtractorByIndex;
    private final BiFunction<T, String, ReadableMetadata> metadataExtractorByName;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final List<String> columnNames = new ArrayList<>();
    private final List<Class<?>> javaTypes = new ArrayList<>();
    private final List<String> classNames = new ArrayList<>();
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
    private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
    private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();


    /**
     * Instantiates a new Row result wrapper.
     *
     * @param readable                 the readable
     * @param allMetadataExtractor     all metadata extra
     * @param metadataExtractorByIndex the metadata extractor by index
     * @param metadataExtractorByName  the metadata extractor by name
     * @param configuration            the configuration
     */
    public ReadableResultWrapper(T readable,
                                 Function<T, List<? extends ReadableMetadata>> allMetadataExtractor,
                                 BiFunction<T, Integer, ReadableMetadata> metadataExtractorByIndex,
                                 BiFunction<T, String, ReadableMetadata> metadataExtractorByName,
                                 R2dbcMybatisConfiguration configuration) {
        this.readable = readable;
        this.metadataExtractorByIndex = metadataExtractorByIndex;
        this.metadataExtractorByName = metadataExtractorByName;
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        allMetadataExtractor.apply(readable).forEach(columnMetadata -> {
            //jdbc provide ResultSetMetaData#getColumnLabel(int index) to get column label
            //bug r2dbc ColumnMetadata doesn't provide any method to get column label
            columnNames.add(columnMetadata.getName());
            Class<?> javaType = columnMetadata.getJavaType();
            if (null == javaType) {
                javaType = Object.class;
            }
            javaTypes.add(javaType);
            classNames.add(javaType.getSimpleName());
        });
    }


    /**
     * Gets readable.
     *
     * @return the readable
     */
    public Readable getReadable() {
        return readable;
    }

    /**
     * Gets readable metadata by index.
     *
     * @param index the index
     * @return the readable metadata by index
     */
    public ReadableMetadata getReadableMetadataByIndex(int index) {
        return this.metadataExtractorByIndex.apply(this.readable, index);
    }

    /**
     * Gets readable metadata by name.
     *
     * @param name the name
     * @return the readable metadata by name
     */
    public ReadableMetadata getReadableMetadataByName(String name) {
        return this.metadataExtractorByName.apply(this.readable, name);
    }

    /**
     * Gets column names.
     *
     * @return the column names
     */
    public List<String> getColumnNames() {
        return this.columnNames;
    }

    /**
     * Gets class names.
     *
     * @return the class names
     */
    public List<String> getClassNames() {
        return Collections.unmodifiableList(classNames);
    }

    /**
     * Gets java types.
     *
     * @return the java types
     */
    public List<Class<?>> getJavaTypes() {
        return javaTypes;
    }

    /**
     * Gets the type handler to use when reading the result set.
     * Tries to get from the TypeHandlerRegistry by searching for the property type.
     * If not found it gets the column JDBC type and tries to get a handler for it.
     *
     * @param propertyType the property type
     * @param columnName   the column name
     * @return the type handler
     */
    public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
        TypeHandler<?> handler = null;
        Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
        if (columnHandlers == null) {
            columnHandlers = new HashMap<>();
            typeHandlerMap.put(columnName, columnHandlers);
        } else {
            handler = columnHandlers.get(propertyType);
        }
        if (handler == null) {
            handler = typeHandlerRegistry.getTypeHandler(propertyType, null);
            // Replicate logic of UnknownTypeHandler#resolveTypeHandler
            // See issue #59 comment 10
            if (handler == null || handler instanceof UnknownTypeHandler) {
                final int index = columnNames.indexOf(columnName);
                final Class<?> javaType = resolveClass(classNames.get(index));
                if (javaType != null) {
                    handler = typeHandlerRegistry.getTypeHandler(javaType);
                }
            }
            if (handler == null || handler instanceof UnknownTypeHandler) {
                handler = new ObjectTypeHandler();
            }
            columnHandlers.put(propertyType, handler);
        }
        return handler;
    }

    private Class<?> resolveClass(String className) {
        try {
            // #699 className could be null
            if (className != null) {
                return Resources.classForName(className);
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) {
        List<String> mappedColumnNames = new ArrayList<>();
        List<String> unmappedColumnNames = new ArrayList<>();
        final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
        final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);
        for (String columnName : columnNames) {
            final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
            if (mappedColumns.contains(upperColumnName)) {
                mappedColumnNames.add(upperColumnName);
            } else {
                unmappedColumnNames.add(columnName);
            }
        }
        mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
        unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
    }

    /**
     * Gets mapped column names.
     *
     * @param resultMap    the result map
     * @param columnPrefix the column prefix
     * @return the mapped column names
     */
    public List<String> getMappedColumnNames(ResultMap resultMap, String columnPrefix) {
        List<String> mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        if (mappedColumnNames == null) {
            loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
            mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        }
        return mappedColumnNames;
    }

    /**
     * Gets unmapped column names.
     *
     * @param resultMap    the result map
     * @param columnPrefix the column prefix
     * @return the unmapped column names
     */
    public List<String> getUnmappedColumnNames(ResultMap resultMap, String columnPrefix) {
        List<String> unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        if (unMappedColumnNames == null) {
            loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
            unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        }
        return unMappedColumnNames;
    }

    private String getMapKey(ResultMap resultMap, String columnPrefix) {
        return resultMap.getId() + ":" + columnPrefix;
    }

    private Set<String> prependPrefixes(Set<String> columnNames, String prefix) {
        if (columnNames == null || columnNames.isEmpty() || prefix == null || prefix.length() == 0) {
            return columnNames == null ? Collections.emptySet() : columnNames;
        }
        final Set<String> prefixed = new HashSet<>();
        for (String columnName : columnNames) {
            prefixed.add(prefix + columnName);
        }
        return prefixed;
    }

}
