package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.key;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.RowResultWrapper;
import reactor.core.publisher.Mono;

/**
 * @author: chenggang
 * @date 12/12/21.
 */
public interface R2dbcKeyGenerator {

    /**
     * handle key result
     * @param rowResultWrapper
     * @return
     */
    Mono<Void> handleKeyResult(RowResultWrapper rowResultWrapper);
}
