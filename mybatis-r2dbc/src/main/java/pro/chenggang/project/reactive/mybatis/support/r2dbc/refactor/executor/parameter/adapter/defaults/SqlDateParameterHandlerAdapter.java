package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.ParameterHandlerAdapter;

import java.sql.Date;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class SqlDateParameterHandlerAdapter implements ParameterHandlerAdapter<Date> {

    @Override
    public Class<Date> adaptClazz() {
        return Date.class;
    }

    @Override
    public void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, Date parameter) {
        statement.bind(parameterHandlerContext.getIndex(),parameter.toLocalDate());
    }
}
