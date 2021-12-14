package pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic;

import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import reactor.core.publisher.Mono;

/**
 * This is a general purpose MyBatis mapper for count statements. Count statements are select statements that always
 * return a long.
 *
 * <p>This mapper can be injected as-is into a MyBatis configuration, or it can be extended with existing mappers.
 *
 * @author Jeff Butler
 */
public interface CommonCountMapper {
    /**
     * Execute a select statement that returns a long (typically a select(count(*)) statement). This mapper
     * assumes the statement returns a single row with a single column that cen be retrieved as a long.
     *
     * @param selectStatement the select statement
     * @return the long value
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    Mono<Long> count(SelectStatementProvider selectStatement);
}
