package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.JdbcParameterAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;

import java.sql.Date;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class SqlDateJdbcParameterAdapter implements JdbcParameterAdapter<Date> {
    @Override
    public Class<Date> adaptClazz() {
        return Date.class;
    }

    @Override
    public void adapt(Statement statement, ParameterHandlerContext parameterHandlerContext, Date parameter) {

    }
}
