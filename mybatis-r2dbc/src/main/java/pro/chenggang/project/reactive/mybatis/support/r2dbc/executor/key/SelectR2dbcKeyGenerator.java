package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key;

import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveMybatisExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * The type Default r2dbc key generator.
 * <p>
 * {@link org.apache.ibatis.executor.keygen.SelectKeyGenerator}
 *
 * @author Gang Cheng
 * @version 1.0.3
 * @since 1.0.2
 */
public class SelectR2dbcKeyGenerator implements R2dbcKeyGenerator {

    private final boolean executeBefore;
    private final MappedStatement keyStatement;
    private final R2dbcMybatisConfiguration r2dbcMybatisConfiguration;
    private final ReactiveMybatisExecutor reactiveMybatisExecutor;

    /**
     * Instantiates a new Select r2dbc key generator.
     *
     * @param selectKeyGenerator        the select key generator
     * @param r2dbcMybatisConfiguration the r2dbc mybatis configuration
     * @param reactiveMybatisExecutor   the reactive mybatis executor
     */
    public SelectR2dbcKeyGenerator(SelectKeyGenerator selectKeyGenerator, R2dbcMybatisConfiguration r2dbcMybatisConfiguration, ReactiveMybatisExecutor reactiveMybatisExecutor) {
        this.executeBefore = SelectKeyGeneratorFieldContainer.getOriginalExecuteBefore(selectKeyGenerator);
        this.keyStatement = SelectKeyGeneratorFieldContainer.getOriginalKeyStatement(selectKeyGenerator);
        this.r2dbcMybatisConfiguration = r2dbcMybatisConfiguration;
        this.reactiveMybatisExecutor = reactiveMybatisExecutor;
    }

    @Override
    public KeyGeneratorType keyGeneratorType() {
        return this.executeBefore ? KeyGeneratorType.SELECT_KEY_BEFORE : KeyGeneratorType.SELECT_KEY_AFTER;
    }

    @Override
    public Mono<Boolean> processSelectKey(KeyGeneratorType keyGeneratorType, MappedStatement ms, Object parameter) {
        if (!this.keyGeneratorType().equals(keyGeneratorType)) {
            return Mono.just(false);
        }
        try {
            if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
                String[] keyProperties = keyStatement.getKeyProperties();
                final MetaObject metaParam = this.r2dbcMybatisConfiguration.newMetaObject(parameter);
                // Do not close keyExecutor.
                // The transaction will be closed by parent executor.
                return this.reactiveMybatisExecutor.query(keyStatement, parameter, RowBounds.DEFAULT)
                        .collectList()
                        .flatMap(values -> {
                            if (values.isEmpty()) {
                                return Mono.error(new ExecutorException("SelectKey returned no data."));
                            }
                            if (values.size() > 1) {
                                return Mono.error(new ExecutorException("SelectKey returned more than one value."));
                            }
                            MetaObject metaResult = this.r2dbcMybatisConfiguration.newMetaObject(values.get(0));
                            if (keyProperties.length == 1) {
                                if (metaResult.hasGetter(keyProperties[0])) {
                                    setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
                                } else {
                                    // no getter for the property - maybe just a single value object
                                    // so try that
                                    setValue(metaParam, keyProperties[0], values.get(0));
                                }
                            } else {
                                handleMultipleProperties(keyProperties, metaParam, metaResult);
                            }
                            return Mono.just(true);
                        });

            }
        } catch (Exception e) {
            return Mono.error(new ExecutorException("Error selecting key or setting result to parameter object. Cause: " + e, e));
        }
        return Mono.just(false);
    }

    @Override
    public Long processGeneratedKeyResult(RowResultWrapper rowResultWrapper, Object parameter) {
        return 0L;
    }

    private void handleMultipleProperties(String[] keyProperties,
                                          MetaObject metaParam, MetaObject metaResult) {
        String[] keyColumns = keyStatement.getKeyColumns();

        if (keyColumns == null || keyColumns.length == 0) {
            // no key columns specified, just use the property names
            for (String keyProperty : keyProperties) {
                setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
            }
        } else {
            if (keyColumns.length != keyProperties.length) {
                throw new ExecutorException("If SelectKey has key columns, the number must match the number of key properties.");
            }
            for (int i = 0; i < keyProperties.length; i++) {
                setValue(metaParam, keyProperties[i], metaResult.getValue(keyColumns[i]));
            }
        }
    }

    private void setValue(MetaObject metaParam, String property, Object value) {
        if (metaParam.hasSetter(property)) {
            metaParam.setValue(property, value);
        } else {
            throw new ExecutorException("No setter found for the keyProperty '" + property + "' in " + metaParam.getOriginalObject().getClass().getName() + ".");
        }
    }

    /**
     * SelectKeyGenerator original field container ,only initialize once
     * @version 1.0.3
     * @since 1.0.3
     */
    private static class SelectKeyGeneratorFieldContainer {

        private static final Field executeBeforeField;
        private static final Field keyStatementField;

        static {
            Field[] declaredFields = SelectKeyGenerator.class.getDeclaredFields();
            Field originalExecuteBeforeField = null;
            Field originalKeyStatementField = null;
            int reduceCount = 0;
            try {
                for (Field declaredField : declaredFields) {
                    if (reduceCount > 2) {
                        break;
                    }
                    declaredField.setAccessible(true);
                    Type genericType = declaredField.getGenericType();
                    if ("boolean".equals(genericType.toString())) {
                        originalExecuteBeforeField = declaredField;
                        reduceCount++;
                    } else if (declaredField.getType().isAssignableFrom(MappedStatement.class)) {
                        originalKeyStatementField = declaredField;
                        reduceCount++;
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
            executeBeforeField = originalExecuteBeforeField;
            keyStatementField = originalKeyStatementField;
        }

        public static boolean getOriginalExecuteBefore(SelectKeyGenerator selectKeyGenerator) {
            try {
                return (boolean) executeBeforeField.get(selectKeyGenerator);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public static MappedStatement getOriginalKeyStatement(SelectKeyGenerator selectKeyGenerator) {
            try {
                return (MappedStatement) keyStatementField.get(selectKeyGenerator);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
