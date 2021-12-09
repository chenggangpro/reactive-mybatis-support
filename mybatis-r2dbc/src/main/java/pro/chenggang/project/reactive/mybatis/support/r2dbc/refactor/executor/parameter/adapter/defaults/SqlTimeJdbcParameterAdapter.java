package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.JdbcParameterAdapter;

import java.sql.Time;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class SqlTimeJdbcParameterAdapter implements JdbcParameterAdapter<Time> {
    @Override
    public Class<Time> adaptClazz() {
        return Time.class;
    }

    @Override
    public void adapt(Statement statement, ParameterHandlerContext parameterHandlerContext, Time parameter) {
        statement.bind(parameterHandlerContext.getIndex(),parameter.toLocalTime());
    }
}
