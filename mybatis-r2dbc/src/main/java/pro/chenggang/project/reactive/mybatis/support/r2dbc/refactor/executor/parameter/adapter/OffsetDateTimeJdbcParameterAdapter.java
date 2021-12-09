package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.JdbcParameterAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;

import java.time.OffsetDateTime;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class OffsetDateTimeJdbcParameterAdapter implements JdbcParameterAdapter<OffsetDateTime> {
    @Override
    public Class<OffsetDateTime> adaptClazz() {
        return OffsetDateTime.class;
    }

    @Override
    public void adapt(Statement statement, ParameterHandlerContext parameterHandlerContext, OffsetDateTime parameter) {

    }
}
