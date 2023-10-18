package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.query.simple;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import reactor.core.publisher.Mono;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface SimpleQueryMapper {

    Mono<Long> countAll();

    Mono<Dept> selectOne();

    Mono<Dept> selectByDeptNo(@Param("deptNo") Long deptNo);

    @Results(id = "selectByDeptNoWithAnnotationResult", value = {
            @Result(column = "dept_no", property = "deptNo", jdbcType = JdbcType.BIGINT, id = true),
            @Result(column = "dept_name", property = "deptName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "location", property = "location", jdbcType = JdbcType.VARCHAR),
            @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP)
    })
    @Select("SELECT * FROM dept WHERE dept_no = #{deptNo}")
    Mono<Dept> selectByDeptNoWithAnnotatedResult(@Param("deptNo") Long deptNo);

    Mono<Dept> selectByDeptNoWithResultMap(@Param("deptNo") Long deptNo);
}
