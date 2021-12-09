package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter;

import io.r2dbc.spi.Statement;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public interface JdbcParameterAdapter<T> {

    /**
     * adapted class
     * @return
     */
    Class<T> adaptClazz();

    /**
     * adapt
     * @param statement
     * @param parameterHandlerContext
     * @param parameter
     */
    void adapt(Statement statement,ParameterHandlerContext parameterHandlerContext, T parameter);
}
