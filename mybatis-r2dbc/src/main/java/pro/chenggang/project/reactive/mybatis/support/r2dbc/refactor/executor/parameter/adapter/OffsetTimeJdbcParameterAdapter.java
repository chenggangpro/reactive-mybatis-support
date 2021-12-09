package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.JdbcParameterAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;

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

    }
}
