package pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive SQL Session
 * copy from https://github.com/linux-china/mybatis-r2dbc
 * @author linux_china
 */
public interface ReactiveSqlSession {

    /**
     * select one by statementId + parameter
     * @param statementId
     * @param parameter
     * @param <T>
     * @return
     */
    <T> Mono<T> selectOne(String statementId, Object parameter);

    /**
     * select one by statementId
     * @param statementId
     * @return
     */
    default Mono<Integer> selectOne(String statementId) {
        return selectOne(statementId, null);
    }

    /**
     * select by statementId + parameter
     * @param statementId
     * @param parameter
     * @param <T>
     * @return
     */
    <T> Flux<T> select(String statementId, Object parameter);

    /**
     * select by statementId
     * @param statementId
     * @param <T>
     * @return
     */
    default <T> Flux<T> select(String statementId) {
        return select(statementId, null);
    }

    /**
     * select by statementId + parameter + rowBounds
     * @param statementId
     * @param parameter
     * @param rowBounds
     * @param <T>
     * @return
     */
    <T> Flux<T> select(String statementId, Object parameter, RowBounds rowBounds);

    /**
     * insert by statementId + parameter
     * @param statementId
     * @param parameter
     * @return
     */
    Mono<Integer> insert(String statementId, Object parameter);

    /**
     * insert by statementId
     * @param statementId
     * @return
     */
    default Mono<Integer> insert(String statementId) {
        return insert(statementId, null);
    }

    /**
     * update by statementId + parameter
     * @param statementId
     * @param parameter
     * @return
     */
    Mono<Integer> update(String statementId, Object parameter);

    /**
     * update by statementId
     * @param statementId
     * @return
     */
    default Mono<Integer> update(String statementId) {
        return update(statementId, null);
    }

    /**
     * delete by statementId + parameter
     * @param statementId
     * @param parameter
     * @return
     */
    Mono<Integer> delete(String statementId, Object parameter);

    /**
     * delete by statementId
     * @param statementId
     * @return
     */
    default Mono<Integer> delete(String statementId) {
        return delete(statementId, null);
    }

    /**
     * get configuration
     * @return
     */
    Configuration getConfiguration();

    /**
     * get mapper
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getMapper(Class<T> clazz);
}
