package pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic;

import org.apache.ibatis.annotations.InsertProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import reactor.core.publisher.Mono;

/**
 * This is a general purpose mapper for executing various types of insert statements.
 * This mapper is appropriate for insert statements that do NOT expect generated keys.
 *
 * @param <T> the type of record associated with this mapper
 */
public interface CommonInsertMapper<T> {
    /**
     * Execute an insert statement with input fields mapped to values in a POJO.
     *
     * @param insertStatement the insert statement
     * @return the number of rows affected
     */
    @InsertProvider(type = SqlProviderAdapter.class, method = "insert")
    Mono<Integer> insert(InsertStatementProvider<T> insertStatement);

    /**
     * Execute an insert statement with input fields supplied directly.
     *
     * @param insertStatement the insert statement
     * @return the number of rows affected
     */
    @InsertProvider(type = SqlProviderAdapter.class, method = "generalInsert")
    Mono<Integer> generalInsert(GeneralInsertStatementProvider insertStatement);

    /**
     * Execute an insert statement with input fields supplied by a select statement.
     *
     * @param insertSelectStatement the insert statement
     * @return the number of rows affected
     */
    @InsertProvider(type = SqlProviderAdapter.class, method = "insertSelect")
    Mono<Integer> insertSelect(InsertSelectStatementProvider insertSelectStatement);

    /**
     * Execute an insert statement that inserts multiple rows. The row values are supplied by mapping
     * to values in a List of POJOs.
     *
     * @param insertStatement the insert statement
     * @return the number of rows affected
     */
    @InsertProvider(type = SqlProviderAdapter.class, method = "insertMultiple")
    Mono<Integer> insertMultiple(MultiRowInsertStatementProvider<T> insertStatement);

}
