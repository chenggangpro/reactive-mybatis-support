package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.JdbcParameterAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;

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

    }
}
