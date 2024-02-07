package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.query.many;

import org.apache.ibatis.annotations.Mapper;
import pro.chenggang.project.reactive.mybatis.support.common.entity.extend.DeptWithEmpAndProjectList;
import pro.chenggang.project.reactive.mybatis.support.common.entity.extend.EmpWithProjectList;
import reactor.core.publisher.Flux;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface ResultOrderedQueryMapper {

    Flux<EmpWithProjectList> selectEmpWithProjectListNotOrdered();

    Flux<EmpWithProjectList> selectEmpWithProjectListOrdered();

    Flux<DeptWithEmpAndProjectList> selectDeptWithEmpAndProjectListNotOrdered();

    Flux<DeptWithEmpAndProjectList> selectDeptWithEmpAndProjectListOrdered();

}
