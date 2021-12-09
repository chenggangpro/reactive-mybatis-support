package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.JdbcParameterAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;

import java.sql.Timestamp;

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

    }
}
