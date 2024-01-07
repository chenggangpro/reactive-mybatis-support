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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.ArrayUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.util.MapUtil;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.TypeHandleContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler.DelegateR2dbcResultRowDataHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;
import reactor.core.publisher.Mono;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * The type Default r2dbc key generator.
 * <p>
 * {@link org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator}
 *
 * @author Gang Cheng
 * @version 1.0.2
 * @since 1.0.0
 */
public class DefaultR2dbcKeyGenerator implements R2dbcKeyGenerator {

    private static final String SECOND_GENERIC_PARAM_NAME = ParamNameResolver.GENERIC_NAME_PREFIX + "2";

    private static final String MSG_TOO_MANY_KEYS = "Too many keys are generated. There are only %d target objects. "
            + "You either specified a wrong 'keyProperty' or encountered a driver bug like #1523.";

    private final LongAdder resultRowCounter = new LongAdder();
    private final MappedStatement mappedStatement;
    private final R2dbcMybatisConfiguration r2dbcMybatisConfiguration;

    /**
     * Instantiates a new Default R2dbc key generator.
     *
     * @param mappedStatement           the mapped statement
     * @param r2dbcMybatisConfiguration the R2dbc mybatis configuration
     */
    public DefaultR2dbcKeyGenerator(MappedStatement mappedStatement, R2dbcMybatisConfiguration r2dbcMybatisConfiguration) {
        this.mappedStatement = mappedStatement;
        this.r2dbcMybatisConfiguration = r2dbcMybatisConfiguration;
    }

    private static String nameOfSingleParam(Map<String, ?> paramMap) {
        // There is virtually one parameter, so any key works.
        return paramMap.keySet().iterator().next();
    }

    private static List<?> collectionize(Object param) {
        if (param instanceof Collection) {
            return new ArrayList<Object>((Collection) param);
        } else if (param instanceof Object[]) {
            return Arrays.asList((Object[]) param);
        } else {
            return Arrays.asList(param);
        }
    }

    @Override
    public KeyGeneratorType keyGeneratorType() {
        return KeyGeneratorType.SIMPLE_RETURN;
    }

    @Override
    public Mono<Boolean> processSelectKey(KeyGeneratorType keyGeneratorType, MappedStatement ms, Object parameter) {
        return Mono.just(false);
    }

    @Override
    public Long processGeneratedKeyResult(RowResultWrapper rowResultWrapper, Object parameter) {
        this.assignKeys(r2dbcMybatisConfiguration, rowResultWrapper, mappedStatement.getKeyProperties(), parameter);
        this.resultRowCounter.increment();
        return this.resultRowCounter.longValue();
    }

    @SuppressWarnings("unchecked")
    private void assignKeys(R2dbcMybatisConfiguration configuration,
                            RowResultWrapper rowResultWrapper,
                            String[] keyProperties,
                            Object parameter) {
        if (parameter instanceof ParamMap) {
            // Multi-param or single param with @Param
            assignKeysToParamMap(configuration, rowResultWrapper, keyProperties, (Map<String, ?>) parameter);
        } else if (parameter instanceof ArrayList && !((ArrayList<?>) parameter).isEmpty()
                && ((ArrayList<?>) parameter).get(0) instanceof ParamMap) {
            // Multi-param or single param with @Param in batch operation
            assignKeysToParamMapList(configuration, rowResultWrapper, keyProperties, (ArrayList<ParamMap<?>>) parameter);
        } else {
            // Single param without @Param
            assignKeysToParam(configuration, rowResultWrapper, keyProperties, parameter);
        }
    }

    private void assignKeysToParam(R2dbcMybatisConfiguration configuration, RowResultWrapper rowResultWrapper,
                                   String[] keyProperties, Object parameter) {
        List<?> params = collectionize(parameter);
        if (params.isEmpty()) {
            return;
        }
        int i = resultRowCounter.intValue();
        if (params.size() <= i) {
            throw new ExecutorException(String.format(MSG_TOO_MANY_KEYS, params.size()));
        }
        KeyAssigner keyAssigner = new KeyAssigner(configuration, i + 1, null, keyProperties[i]);
        keyAssigner.assign(rowResultWrapper, params.get(i));
    }

    private void assignKeysToParamMapList(R2dbcMybatisConfiguration configuration, RowResultWrapper rowResultWrapper,
                                          String[] keyProperties, ArrayList<ParamMap<?>> paramMapList) {
        int i = resultRowCounter.intValue();
        if (paramMapList.size() <= i) {
            throw new ExecutorException(String.format(MSG_TOO_MANY_KEYS, paramMapList.size()));
        }
        List<KeyAssigner> assignerList = new ArrayList<>();
        ParamMap<?> paramMap = paramMapList.get(i);
        for (int j = 0; j < keyProperties.length; j++) {
            assignerList.add(getAssignerForParamMap(configuration, j + 1, paramMap, keyProperties[j], keyProperties, false)
                    .getValue());
        }
        assignerList.forEach(x -> x.assign(rowResultWrapper, paramMap));
    }

    private void assignKeysToParamMap(R2dbcMybatisConfiguration configuration,
                                      RowResultWrapper rowResultWrapper,
                                      String[] keyProperties,
                                      Map<String, ?> paramMap) {
        if (paramMap.isEmpty()) {
            return;
        }
        Map<String, Map.Entry<List<?>, List<KeyAssigner>>> assignerMap = new HashMap<>();
        for (int i = 0; i < keyProperties.length; i++) {
            Map.Entry<String, KeyAssigner> entry = getAssignerForParamMap(configuration, i + 1, paramMap, keyProperties[i],
                    keyProperties, true);
            Map.Entry<List<?>, List<KeyAssigner>> iteratorPair = MapUtil.computeIfAbsent(assignerMap, entry.getKey(),
                    k -> MapUtil.entry(collectionize(paramMap.get(k)), new ArrayList<>()));
            iteratorPair.getValue().add(entry.getValue());
        }
        int i = resultRowCounter.intValue();
        for (Map.Entry<List<?>, List<KeyAssigner>> pair : assignerMap.values()) {
            if (pair.getKey().size() <= i) {
                throw new ExecutorException(String.format(MSG_TOO_MANY_KEYS, paramMap.size()));
            }
            Object param = pair.getKey().get(i);
            pair.getValue().forEach(x -> x.assign(rowResultWrapper, param));
        }
    }

    private Map.Entry<String, KeyAssigner> getAssignerForParamMap(R2dbcMybatisConfiguration config,
                                                                  int columnPosition,
                                                                  Map<String, ?> paramMap,
                                                                  String keyProperty,
                                                                  String[] keyProperties,
                                                                  boolean omitParamName) {
        Set<String> keySet = paramMap.keySet();
        // A caveat : if the only parameter has {@code @Param("param2")} on it,
        // it must be referenced with param name e.g. 'param2.x'.
        boolean singleParam = !keySet.contains(SECOND_GENERIC_PARAM_NAME);
        int firstDot = keyProperty.indexOf('.');
        if (firstDot == -1) {
            if (singleParam) {
                return getAssignerForSingleParam(config, columnPosition, paramMap, keyProperty, omitParamName);
            }
            throw new ExecutorException("Could not determine which parameter to assign generated keys to. "
                    + "Note that when there are multiple parameters, 'keyProperty' must include the parameter name (e.g. 'param.id'). "
                    + "Specified key properties are " + ArrayUtil.toString(keyProperties) + " and available parameters are "
                    + keySet);
        }
        String paramName = keyProperty.substring(0, firstDot);
        if (keySet.contains(paramName)) {
            String argParamName = omitParamName ? null : paramName;
            String argKeyProperty = keyProperty.substring(firstDot + 1);
            return MapUtil.entry(paramName, new KeyAssigner(config, columnPosition, argParamName, argKeyProperty));
        } else if (singleParam) {
            return getAssignerForSingleParam(config, columnPosition, paramMap, keyProperty, omitParamName);
        } else {
            throw new ExecutorException("Could not find parameter '" + paramName + "'. "
                    + "Note that when there are multiple parameters, 'keyProperty' must include the parameter name (e.g. 'param.id'). "
                    + "Specified key properties are " + ArrayUtil.toString(keyProperties) + " and available parameters are "
                    + keySet);
        }
    }

    private Map.Entry<String, KeyAssigner> getAssignerForSingleParam(R2dbcMybatisConfiguration r2dbcMybatisConfiguration,
                                                                     int columnPosition, Map<String, ?> paramMap, String keyProperty, boolean omitParamName) {
        // Assume 'keyProperty' to be a property of the single param.
        String singleParamName = nameOfSingleParam(paramMap);
        String argParamName = omitParamName ? null : singleParamName;
        return MapUtil.entry(singleParamName, new KeyAssigner(r2dbcMybatisConfiguration, columnPosition, argParamName, keyProperty));
    }

    private class KeyAssigner {

        private final R2dbcMybatisConfiguration r2dbcMybatisConfiguration;
        private final TypeHandlerRegistry typeHandlerRegistry;
        private final int columnPosition;
        private final String paramName;
        private final String propertyName;
        private final TypeHandler<?> delegatedTypeHandler;
        private TypeHandler<?> typeHandler;

        /**
         * Instantiates a new Key assigner.
         *
         * @param r2dbcMybatisConfiguration the R2dbc mybatis configuration
         * @param columnPosition            the column position
         * @param paramName                 the param name
         * @param propertyName              the property name
         */
        protected KeyAssigner(R2dbcMybatisConfiguration r2dbcMybatisConfiguration,
                              int columnPosition,
                              String paramName,
                              String propertyName) {
            super();
            this.r2dbcMybatisConfiguration = r2dbcMybatisConfiguration;
            this.typeHandlerRegistry = r2dbcMybatisConfiguration.getTypeHandlerRegistry();
            this.columnPosition = columnPosition;
            this.paramName = paramName;
            this.propertyName = propertyName;
            this.delegatedTypeHandler = initDelegateTypeHandler();
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
                            this.r2dbcMybatisConfiguration.getR2dbcTypeHandlerAdapterRegistry()
                    ),
                    TypeHandleContext.class
            );
        }

        /**
         * Assign.
         *
         * @param rowResultWrapper the row result wrapper
         * @param param            the param
         */
        protected void assign(RowResultWrapper rowResultWrapper, Object param) {
            if (paramName != null) {
                // If paramName is set, param is ParamMap
                param = ((ParamMap<?>) param).get(paramName);
            }
            MetaObject metaParam = r2dbcMybatisConfiguration.newMetaObject(param);
            try {
                Class<?> propertyType = null;
                if (typeHandler == null) {
                    if (metaParam.hasSetter(propertyName)) {
                        propertyType = metaParam.getSetterType(propertyName);
                        typeHandler = typeHandlerRegistry.getTypeHandler(propertyType);
                    } else {
                        throw new ExecutorException("No setter found for the keyProperty '" + propertyName + "' in '"
                                + metaParam.getOriginalObject().getClass().getName() + "'.");
                    }
                }
                if (typeHandler == null) {
                    // Error?
                } else {
                    ((TypeHandleContext) this.delegatedTypeHandler).contextWith(propertyType, typeHandler, rowResultWrapper);
                    ResultSet resultSet = null;
                    Object value = delegatedTypeHandler.getResult(resultSet, columnPosition);
                    metaParam.setValue(propertyName, value);
                }
            } catch (SQLException e) {
                throw new ExecutorException("Error getting generated key or setting result to parameter object. Cause: " + e,
                        e);
            }
        }
    }

}
