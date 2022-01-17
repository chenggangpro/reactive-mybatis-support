package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key;

import org.apache.ibatis.mapping.MappedStatement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;
import reactor.core.publisher.Mono;

/**
 * The type No key r 2 dbc key generator.
 *
 * @author evans
 * @date 2022 /1/17.
 * @since 1.0.2
 * @version 1.0.2
 */
public class NoKeyR2dbcKeyGenerator implements R2dbcKeyGenerator {

    private NoKeyR2dbcKeyGenerator() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static NoKeyR2dbcKeyGenerator getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public KeyGeneratorType keyGeneratorType() {
        return KeyGeneratorType.NONE;
    }

    @Override
    public Mono<Boolean> processSelectKey(KeyGeneratorType keyGeneratorType, MappedStatement ms, Object parameter) {
        return Mono.just(true);
    }

    @Override
    public Integer processGeneratedKeyResult(RowResultWrapper rowResultWrapper, Object parameter) {
        return 0;
    }

    private static class InstanceHolder {

        private final static NoKeyR2dbcKeyGenerator INSTANCE;

        static {
            INSTANCE = new NoKeyR2dbcKeyGenerator();
        }

    }
}
