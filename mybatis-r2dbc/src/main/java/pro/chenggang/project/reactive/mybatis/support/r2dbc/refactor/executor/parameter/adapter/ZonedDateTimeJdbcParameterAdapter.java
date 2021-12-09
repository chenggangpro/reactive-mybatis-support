package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.JdbcParameterAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;

import java.time.ZonedDateTime;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ZonedDateTimeJdbcParameterAdapter implements JdbcParameterAdapter<ZonedDateTime> {
    @Override
    public Class<ZonedDateTime> adaptClazz() {
        return ZonedDateTime.class;
    }

    @Override
    public void adapt(Statement statement, ParameterHandlerContext parameterHandlerContext, ZonedDateTime parameter) {

    }
}
