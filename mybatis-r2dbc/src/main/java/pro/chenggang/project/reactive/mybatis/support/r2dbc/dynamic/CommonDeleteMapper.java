package pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic;

import org.apache.ibatis.annotations.DeleteProvider;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import reactor.core.publisher.Mono;

/**
 * This is a general purpose MyBatis mapper for delete statements.
 *
 * <p>This mapper can be injected as-is into a MyBatis configuration, or it can be extended with existing mappers.
 *
 * @author Jeff Butler
 */
public interface CommonDeleteMapper {
    /**
     * Execute a delete statement.
     *
     * @param deleteStatement the delete statement
     * @return the number of rows affected
     */
    @DeleteProvider(type = SqlProviderAdapter.class, method = "delete")
    Mono<Integer> delete(DeleteStatementProvider deleteStatement);
}
