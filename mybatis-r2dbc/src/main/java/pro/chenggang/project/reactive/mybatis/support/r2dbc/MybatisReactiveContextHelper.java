package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.StatementLogHelper;
import reactor.util.context.Context;

/**
 * @author: chenggang
 * @date 12/16/21.
 */
public interface MybatisReactiveContextHelper {

    /**
     * init reactive executor context with StatementLogHelper
     * @param context
     * @param statementLogHelper
     * @return
     */
    Context initReactiveExecutorContext(Context context, StatementLogHelper statementLogHelper);

    /**
     * init reactive executor context
     * @param context
     * @return
     */
    Context initReactiveExecutorContext(Context context);
}
