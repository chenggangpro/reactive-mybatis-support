package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public interface ParameterHandlerAdapter<T> {

    /**
     * adapted class
     * @return
     */
    Class<T> adaptClazz();

    /**
     * setParameter
     * @param statement
     * @param parameterHandlerContext
     * @param parameter
     */
    void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, T parameter);
}
