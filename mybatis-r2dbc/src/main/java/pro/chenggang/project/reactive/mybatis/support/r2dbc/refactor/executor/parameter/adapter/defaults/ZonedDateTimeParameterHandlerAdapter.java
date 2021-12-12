package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.ParameterHandlerAdapter;

import java.time.ZonedDateTime;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ZonedDateTimeParameterHandlerAdapter implements ParameterHandlerAdapter<ZonedDateTime> {

    @Override
    public Class<ZonedDateTime> adaptClazz() {
        return ZonedDateTime.class;
    }

    @Override
    public void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, ZonedDateTime parameter) {
        statement.bind(parameterHandlerContext.getIndex(),parameter.toOffsetDateTime());
    }
}
