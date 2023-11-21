package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.update;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectContent;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonUpdateMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.ReactiveMyBatis3Utils;
import reactor.core.publisher.Mono;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.createTime;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.dept;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.deptName;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.deptNo;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.location;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface UpdateMapper extends CommonUpdateMapper {

    Mono<Integer> updateDeptByDeptNo(Dept dept);

    @Update("UPDATE dept SET dept_name = #{deptName}, location = #{location} , create_time = #{createTime} WHERE dept_no = #{deptNo}")
    Mono<Integer> updateDeptByDeptNoWithAnnotation(Dept dept);

    default Mono<Integer> updateDeptByDeptNoWithDynamic(Dept row) {
        return ReactiveMyBatis3Utils.update(this::update,
                dept,
                dsl -> dsl.set(deptName)
                        .equalTo(row.getDeptName())
                        .set(location)
                        .equalTo(row.getLocation())
                        .set(createTime)
                        .equalTo(row.getCreateTime())
                        .where(deptNo, isEqualTo(row.getDeptNo()))
        );
    }

    Mono<Integer> updateBlobAndClodById(SubjectContent subjectContent);
}
