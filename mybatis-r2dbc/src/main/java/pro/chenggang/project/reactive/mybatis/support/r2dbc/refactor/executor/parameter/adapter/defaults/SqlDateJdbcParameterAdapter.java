package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.JdbcParameterAdapter;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
        LocalDateTime localDateTime = parameter.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        statement.bind(parameterHandlerContext.getIndex(),localDateTime);
    }
}
