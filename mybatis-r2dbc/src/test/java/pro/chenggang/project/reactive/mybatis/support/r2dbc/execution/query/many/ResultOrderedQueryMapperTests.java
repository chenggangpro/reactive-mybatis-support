package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.query.many;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.entity.extend.DeptWithEmpAndProjectList;
import pro.chenggang.project.reactive.mybatis.support.common.entity.extend.EmpWithProjectList;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
public class ResultOrderedQueryMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void selectEmpWithProjectListNotOrdered(){
        super.<List<EmpWithProjectList>>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ResultOrderedQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ResultOrderedQueryMapper resultOrderedQueryMapper = reactiveSqlSession.getMapper(
                            ResultOrderedQueryMapper.class);
                    return resultOrderedQueryMapper.selectEmpWithProjectListNotOrdered()
                            .collectList();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(empWithProjectLists -> {
                            assertEquals(14, empWithProjectLists.size());
                            for (EmpWithProjectList empWithProjectList : empWithProjectLists) {
                                // 7,9,14
                                if(empWithProjectList.getEmpNo().equals(7L)){
                                    assertEquals(5, empWithProjectList.getProjectList().size());
                                    continue;
                                }
                                if(empWithProjectList.getEmpNo().equals(9L)){
                                    assertEquals(5, empWithProjectList.getProjectList().size());
                                    continue;
                                }
                                if(empWithProjectList.getEmpNo().equals(14L)){
                                    assertEquals(5, empWithProjectList.getProjectList().size());
                                    continue;
                                }
                                assertTrue(empWithProjectList.getProjectList().isEmpty());
                            }
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectEmpWithProjectListOrdered(){
        super.<List<EmpWithProjectList>>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ResultOrderedQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ResultOrderedQueryMapper resultOrderedQueryMapper = reactiveSqlSession.getMapper(
                            ResultOrderedQueryMapper.class);
                    return resultOrderedQueryMapper.selectEmpWithProjectListOrdered()
                            .take(11,true)
                            .collectList();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(empWithProjectLists -> {
                            assertEquals(11, empWithProjectLists.size());
                            List<EmpWithProjectList> empNoIs7List = empWithProjectLists.stream()
                                    .filter(empWithProjectList -> empWithProjectList.getEmpNo().equals(7L))
                                    .collect(Collectors.toList());
                            assertEquals(5, empNoIs7List.size());
                            for (EmpWithProjectList empWithProjectList : empWithProjectLists) {
                                // 7 -> [single project]
                                if(empWithProjectList.getEmpNo().equals(7L)){
                                    assertEquals(1, empWithProjectList.getProjectList().size());
                                    continue;
                                }
                                assertTrue(empWithProjectList.getProjectList().isEmpty());
                            }
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectDeptWithEmpAndProjectListNotOrdered(){
        super.<List<DeptWithEmpAndProjectList>>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ResultOrderedQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ResultOrderedQueryMapper resultOrderedQueryMapper = reactiveSqlSession.getMapper(
                            ResultOrderedQueryMapper.class);
                    return resultOrderedQueryMapper.selectDeptWithEmpAndProjectListNotOrdered()
                            .collectList();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(deptWithEmpAndProjectLists -> {
                            assertEquals(4, deptWithEmpAndProjectLists.size());
                            DeptWithEmpAndProjectList deptWithEmpAndProjectList = deptWithEmpAndProjectLists.get(0);
                            List<EmpWithProjectList> empWithProjectLists = deptWithEmpAndProjectList.getEmpWithProjectLists();
                            for (EmpWithProjectList empWithProjectList : empWithProjectLists) {
                                // 7 -> [single project]
                                if(empWithProjectList.getEmpNo().equals(7L)){
                                    assertEquals(5, empWithProjectList.getProjectList().size());
                                    continue;
                                }
                                if(empWithProjectList.getEmpNo().equals(9L)){
                                    assertEquals(5, empWithProjectList.getProjectList().size());
                                    continue;
                                }
                                if(empWithProjectList.getEmpNo().equals(14L)){
                                    assertEquals(5, empWithProjectList.getProjectList().size());
                                }
                            }
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectDeptWithEmpAndProjectListOrdered(){
        super.<List<DeptWithEmpAndProjectList>>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ResultOrderedQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ResultOrderedQueryMapper resultOrderedQueryMapper = reactiveSqlSession.getMapper(
                            ResultOrderedQueryMapper.class);
                    return resultOrderedQueryMapper.selectDeptWithEmpAndProjectListOrdered()
                            .take(15,true)
                            .collectList();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(deptWithEmpAndProjectLists -> {
                            assertEquals(15, deptWithEmpAndProjectLists.size());
                            for (DeptWithEmpAndProjectList deptWithEmpAndProjectList : deptWithEmpAndProjectLists) {
                                assertEquals(1,deptWithEmpAndProjectList.getEmpWithProjectLists().size());
                                for (EmpWithProjectList empWithProjectList : deptWithEmpAndProjectList.getEmpWithProjectLists()) {
                                    assertEquals(1,empWithProjectList.getProjectList().size());
                                }
                            }
                        })
                        .verifyComplete()
                )
                .run();
    }

}
