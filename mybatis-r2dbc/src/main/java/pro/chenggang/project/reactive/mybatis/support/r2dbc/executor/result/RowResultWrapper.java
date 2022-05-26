package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
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

/**
 * The type Row result wrapper.
 * <p>
 * {@link org.apache.ibatis.executor.resultset.ResultSetWrapper}
 *
 * @author Iwao AVE!
 */
public class RowResultWrapper {

    private final Row row;
    private final RowMetadata rowMetadata;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final List<String> columnNames = new ArrayList<>();
    private final List<Class> javaTypes = new ArrayList<>();
    private final List<String> classNames = new ArrayList<>();
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
    private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
    private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

    /**
     * Instantiates a new Row result wrapper.
     *
     * @param row           the row
     * @param rowMetadata   the row metadata
     * @param configuration the configuration
     */
    public RowResultWrapper(Row row, RowMetadata rowMetadata, R2dbcMybatisConfiguration configuration) {
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.row = row;
        this.rowMetadata = rowMetadata;
        rowMetadata.getColumnMetadatas().forEach(columnMetadata -> {
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
     * Gets row.
     *
     * @return the row
     */
    public Row getRow() {
        return row;
    }

    /**
     * Gets row metadata.
     *
     * @return the row metadata
     */
    public RowMetadata getRowMetadata() {
        return rowMetadata;
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
    public List<Class> getJavaTypes() {
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
