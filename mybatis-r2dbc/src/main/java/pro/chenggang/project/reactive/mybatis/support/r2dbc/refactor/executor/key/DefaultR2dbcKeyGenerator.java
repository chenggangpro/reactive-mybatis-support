package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.key;

import org.apache.ibatis.reflection.ParamNameResolver;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.RowResultWrapper;
import reactor.core.publisher.Mono;

/**
 * @author: chenggang
 * @date 12/12/21.
 */
public class DefaultR2dbcKeyGenerator implements R2dbcKeyGenerator {

    private static final String SECOND_GENERIC_PARAM_NAME = ParamNameResolver.GENERIC_NAME_PREFIX + "2";

    private static final String MSG_TOO_MANY_KEYS = "Too many keys are generated. There are only %d target objects. "
            + "You either specified a wrong 'keyProperty' or encountered a driver bug like #1523.";

    @Override
    public Mono<Void> handleKeyResult(RowResultWrapper rowResultWrapper) {
        //TODO key generator
        return null;
    }


}
