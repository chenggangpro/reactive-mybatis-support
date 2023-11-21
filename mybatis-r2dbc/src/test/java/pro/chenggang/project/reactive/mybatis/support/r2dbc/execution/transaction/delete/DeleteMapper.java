package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.delete;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonDeleteMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.ReactiveMyBatis3Utils;
import reactor.core.publisher.Mono;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.dept;

/**
 * The interface Delete mapper.
 *
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface DeleteMapper extends CommonDeleteMapper {

    Mono<Integer> deleteByDeptNo(Long deptNo);

    @Delete("DELETE FROM dept WHERE dept_no = #{deptNo}")
    Mono<Integer> deleteByDeptNoWithAnnotation(Long deptNo);

    default Mono<Integer> deleteByDeptNoWithDynamic(Long deptNo) {
        return ReactiveMyBatis3Utils.deleteFrom(this::delete,
                dept,
                dsl -> dsl.where(DeptDynamicSqlSupport.deptNo, isEqualTo(deptNo))
        );
    }
}
