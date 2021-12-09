package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.JdbcParameterAdapter;

import java.time.OffsetTime;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class OffsetTimeJdbcParameterAdapter implements JdbcParameterAdapter<OffsetTime> {
    @Override
    public Class<OffsetTime> adaptClazz() {
        return OffsetTime.class;
    }

    @Override
    public void adapt(Statement statement, ParameterHandlerContext parameterHandlerContext, OffsetTime parameter) {
        statement.bind(parameterHandlerContext.getIndex(),parameter.toLocalTime());
    }
}
