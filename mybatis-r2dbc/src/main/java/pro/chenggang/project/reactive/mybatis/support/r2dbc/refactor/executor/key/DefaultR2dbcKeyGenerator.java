package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.key;

import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.ArrayUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.util.MapUtil;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.binding.MapperMethod.ParamMap;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.RowResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.TypeHandleContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.handler.DelegateR2DbcResultRowDataHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.support.ProxyInstanceFactory;
import reactor.core.publisher.Mono;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author: chenggang
 * @date 12/12/21.
 */
public class DefaultR2dbcKeyGenerator implements R2dbcKeyGenerator {

    private static final String SECOND_GENERIC_PARAM_NAME = ParamNameResolver.GENERIC_NAME_PREFIX + "2";

    private static final String MSG_TOO_MANY_KEYS = "Too many keys are generated. There are only %d target objects. "
            + "You either specified a wrong 'keyProperty' or encountered a driver bug like #1523.";

    private final LongAdder resultRowCounter = new LongAdder();
    private final MappedStatement mappedStatement;
    private final R2dbcConfiguration r2dbcConfiguration;

    public DefaultR2dbcKeyGenerator(MappedStatement mappedStatement, R2dbcConfiguration r2dbcConfiguration) {
        this.mappedStatement = mappedStatement;
        this.r2dbcConfiguration = r2dbcConfiguration;
    }

    @Override
    public Integer getResultRowCount() {
        return this.resultRowCounter.intValue();
    }

    @Override
    public Mono<Integer> handleKeyResult(RowResultWrapper rowResultWrapper, Object parameter) {
        return Mono.fromRunnable(() -> this.assignKeys(r2dbcConfiguration,rowResultWrapper,mappedStatement.getKeyProperties(),parameter))
                .then(Mono.just(1))
                .doOnNext(v -> this.resultRowCounter.increment());
    }

    @SuppressWarnings("unchecked")
    private void assignKeys(R2dbcConfiguration configuration,
                            RowResultWrapper rowResultWrapper,
                            String[] keyProperties,
                            Object parameter) {
        if (parameter instanceof ParamMap ) {
            // Multi-param or single param with @Param
            assignKeysToParamMap(configuration, rowResultWrapper, keyProperties, (Map<String, ?>) parameter);
        } else if (parameter instanceof ArrayList && !((ArrayList<?>) parameter).isEmpty()
                && ((ArrayList<?>) parameter).get(0) instanceof ParamMap) {
            // Multi-param or single param with @Param in batch operation
            assignKeysToParamMapList(configuration, rowResultWrapper, keyProperties, (ArrayList<ParamMap<?>>) parameter);
        } else {
            // Single param without @Param
            assignKeysToParam(configuration,rowResultWrapper, keyProperties, parameter);
        }
    }

    private void assignKeysToParam(R2dbcConfiguration configuration, RowResultWrapper rowResultWrapper,
                                   String[] keyProperties, Object parameter) {
        List<?> params = collectionize(parameter);
        if (params.isEmpty()) {
            return;
        }
        int i = resultRowCounter.intValue();
        if(params.size() <= i ){
            throw new ExecutorException(String.format(MSG_TOO_MANY_KEYS, params.size()));
        }
        KeyAssigner keyAssigner = new KeyAssigner(configuration, i + 1, null, keyProperties[i]);
        keyAssigner.assign(rowResultWrapper,params.get(i));
    }

    private void assignKeysToParamMapList(R2dbcConfiguration configuration, RowResultWrapper rowResultWrapper,
                                          String[] keyProperties, ArrayList<ParamMap<?>> paramMapList) {
        int i = resultRowCounter.intValue();
        if(paramMapList.size() <= i ){
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

    private void assignKeysToParamMap(R2dbcConfiguration configuration,
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
            if(pair.getKey().size() <= i ){
                throw new ExecutorException(String.format(MSG_TOO_MANY_KEYS,paramMap.size()));
            }
            Object param = pair.getKey().get(i);
            pair.getValue().forEach(x -> x.assign(rowResultWrapper, param));
        }
    }

    private Map.Entry<String, KeyAssigner> getAssignerForParamMap(R2dbcConfiguration config,
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

    private Map.Entry<String, KeyAssigner> getAssignerForSingleParam(R2dbcConfiguration r2dbcConfiguration,
                                                                                       int columnPosition, Map<String, ?> paramMap, String keyProperty, boolean omitParamName) {
        // Assume 'keyProperty' to be a property of the single param.
        String singleParamName = nameOfSingleParam(paramMap);
        String argParamName = omitParamName ? null : singleParamName;
        return MapUtil.entry(singleParamName, new KeyAssigner(r2dbcConfiguration, columnPosition, argParamName, keyProperty));
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

    private class KeyAssigner {

        private final R2dbcConfiguration r2dbcConfiguration;
        private final TypeHandlerRegistry typeHandlerRegistry;
        private final int columnPosition;
        private final String paramName;
        private final String propertyName;
        private TypeHandler<?> typeHandler;
        private final TypeHandler delegatedTypeHandler;

        protected KeyAssigner(R2dbcConfiguration r2dbcConfiguration,
                              int columnPosition,
                              String paramName,
                              String propertyName) {
            super();
            this.r2dbcConfiguration = r2dbcConfiguration;
            this.typeHandlerRegistry = r2dbcConfiguration.getTypeHandlerRegistry();
            this.columnPosition = columnPosition;
            this.paramName = paramName;
            this.propertyName = propertyName;
            this.delegatedTypeHandler = initDelegateTypeHandler();
        }

        /**
         * get delegate type handler
         * @return
         */
        private TypeHandler initDelegateTypeHandler(){
            return ProxyInstanceFactory.newInstanceOfInterfaces(
                    TypeHandler.class,
                    () -> new DelegateR2DbcResultRowDataHandler(
                            this.r2dbcConfiguration.getNotSupportedDataTypes(),
                            this.r2dbcConfiguration.getR2dbcTypeHandlerAdapterRegistry().getR2dbcTypeHandlerAdapters()
                    ),
                    TypeHandleContext.class
            );
        }

        protected void assign(RowResultWrapper rowResultWrapper, Object param) {
            if (paramName != null) {
                // If paramName is set, param is ParamMap
                param = ((ParamMap<?>) param).get(paramName);
            }
            MetaObject metaParam = r2dbcConfiguration.newMetaObject(param);
            try {
                if (typeHandler == null) {
                    if (metaParam.hasSetter(propertyName)) {
                        Class<?> propertyType = metaParam.getSetterType(propertyName);
                        typeHandler = typeHandlerRegistry.getTypeHandler(propertyType);
                    } else {
                        throw new ExecutorException("No setter found for the keyProperty '" + propertyName + "' in '"
                                + metaParam.getOriginalObject().getClass().getName() + "'.");
                    }
                }
                if (typeHandler == null) {
                    // Error?
                } else {
                    ((TypeHandleContext)this.delegatedTypeHandler).contextWith(typeHandler,rowResultWrapper);
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
