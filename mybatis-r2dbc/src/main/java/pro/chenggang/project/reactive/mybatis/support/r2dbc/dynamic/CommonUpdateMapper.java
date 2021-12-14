package pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic;

import org.apache.ibatis.annotations.UpdateProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import reactor.core.publisher.Mono;

/**
 * This is a general purpose MyBatis mapper for update statements.
 *
 * <p>This mapper can be injected as-is into a MyBatis configuration, or it can be extended with existing mappers.
 *
 * @author Jeff Butler
 */
public interface CommonUpdateMapper {
    /**
     * Execute an update statement.
     *
     * @param updateStatement the update statement
     * @return the number of rows affected
     */
    @UpdateProvider(type = SqlProviderAdapter.class, method = "update")
    Mono<Integer> update(UpdateStatementProvider updateStatement);
}
