package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key;

import org.apache.ibatis.mapping.MappedStatement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;
import reactor.core.publisher.Mono;

/**
 * The interface R2dbc key generator.
 *
 * @author Gang Cheng
 * @version 1.0.2
 * @since 1.0.2
 */
public interface R2dbcKeyGenerator {

    /**
     * Key generator type key generator type.
     *
     * @return the key generator type
     */
    KeyGeneratorType keyGeneratorType();

    /**
     * Process select key mono.
     *
     * @param keyGeneratorType the KeyGeneratorType
     * @param ms               the ms
     * @param parameter        the parameter
     * @return the mono
     */
    Mono<Boolean> processSelectKey(KeyGeneratorType keyGeneratorType, MappedStatement ms, Object parameter);

    /**
     * Process generated key result mono.
     *
     * @param rowResultWrapper the row result wrapper
     * @param parameter        the parameter
     * @return the mono
     */
    Long processGeneratedKeyResult(RowResultWrapper rowResultWrapper, Object parameter);
}
