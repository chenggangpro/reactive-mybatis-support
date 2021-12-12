package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result;

import org.apache.ibatis.type.TypeHandler;

/**
 * @author: chenggang
 * @date 12/11/21.
 */
public interface TypeHandleContext {

    /**
     * set delegated type handler
     * @param delegatedTypeHandler
     */
    void contextWith(TypeHandler delegatedTypeHandler,RowResultWrapper rowResultWrapper);

}
