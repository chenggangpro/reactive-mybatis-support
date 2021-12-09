package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.JdbcParameterAdapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class TimestampJdbcParameterAdapter implements JdbcParameterAdapter<Timestamp> {
    @Override
    public Class<Timestamp> adaptClazz() {
        return Timestamp.class;
    }

    @Override
    public void adapt(Statement statement, ParameterHandlerContext parameterHandlerContext, Timestamp parameter) {
        LocalDateTime localDateTime = parameter.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        statement.bind(parameterHandlerContext.getIndex(),localDateTime);
    }
}
