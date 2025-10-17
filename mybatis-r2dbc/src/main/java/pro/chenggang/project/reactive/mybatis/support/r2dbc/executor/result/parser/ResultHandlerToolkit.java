package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.parser;

import io.r2dbc.spi.OutParameters;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.TypeHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.exception.R2dbcResultException;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.TypeHandleContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Gang Cheng
 * @version 0.1.0
 * @since 3.1.0
 */
public abstract class ResultHandlerToolkit {

    public static TypeHandler<?> initDelegateTypeHandler(R2dbcMybatisConfiguration r2dbcMybatisConfiguration) {
        return ProxyInstanceFactory.newInstanceOfInterfaces(
                TypeHandler.class,
                () -> new DelegateR2dbcResultRowDataHandler(
                        r2dbcMybatisConfiguration.getNotSupportedDataTypes(),
                        r2dbcMybatisConfiguration.getR2dbcTypeHandlerAdapterRegistry()
                ),
                TypeHandleContext.class
        );
    }

    public static Mono<Long> handleOutputParameters(Result.OutSegment segment,
                                                    R2dbcMybatisConfiguration r2dbcMybatisConfiguration,
                                                    BoundSql boundSql,
                                                    ParameterHandler parameterHandler) {
        ReadableResultWrapper<OutParameters> readableResultWrapper = ReadableResultWrapper.ofOutParameters(
                segment.outParameters(),
                r2dbcMybatisConfiguration

        );
        final Object parameterObject = parameterHandler.getParameterObject();
        final MetaObject metaParam = r2dbcMybatisConfiguration.newMetaObject(parameterObject);
        final List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        final TypeHandler<?> outputDelegatedTypeHandler = ResultHandlerToolkit.initDelegateTypeHandler(r2dbcMybatisConfiguration);
        return Flux.fromIterable(parameterMappings)
                .filter(parameterMapping -> parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT)
                .concatMap(parameterMapping -> {
                    if (ResultSet.class.equals(parameterMapping.getJavaType())
                            || Row.class.equals(parameterMapping.getJavaType())
                            || Result.class.equals(parameterMapping.getJavaType())) {
                        return Mono.error(new UnsupportedOperationException(
                                "Unsupported Java type encountered: '" + parameterMapping.getJavaType() + "' during output parameter mapping." +
                                        " To handle multiple rows of output parameters, " +
                                        "consider using a query operation rather than an update operation." +
                                        " Receiving output parameters with an update operation is only effective for single-row results."));
                    }
                    try {
                        final TypeHandler<?> typeHandler = parameterMapping.getTypeHandler();
                        ((TypeHandleContext) outputDelegatedTypeHandler).contextWith(parameterMapping.getJavaType(), typeHandler, readableResultWrapper);
                        Object value = outputDelegatedTypeHandler.getResult(null, parameterMapping.getProperty());
                        metaParam.setValue(parameterMapping.getProperty(), value);
                    } catch (SQLException e) {
                        return Mono.error(new R2dbcResultException(e));
                    }
                    return Mono.just(true);
                })
                .filter(Boolean::booleanValue)
                .count()
                .map(totalProcessedParameters -> {
                    if (totalProcessedParameters > 0) {
                        return 1L;
                    }
                    return 0L;
                });
    }
}
