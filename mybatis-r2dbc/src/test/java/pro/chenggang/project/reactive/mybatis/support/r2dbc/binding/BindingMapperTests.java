/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package pro.chenggang.project.reactive.mybatis.support.r2dbc.binding;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.mapper.DeptMapper;
import pro.chenggang.project.reactive.mybatis.support.common.mapper.EmpMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class BindingMapperTests extends MybatisR2dbcBaseTests {

    ReactiveSqlSessionFactory reactiveSqlSessionFactory;
    ReactiveSqlSession reactiveSqlSession;

    @BeforeEach
    void beforeEach() throws Exception {
        this.reactiveSqlSessionFactory = setUp(MySQLContainer.class, true, r2dbcProtocol -> {
            R2dbcMybatisConfiguration r2dbcMybatisConfiguration = new R2dbcMybatisConfiguration();
            r2dbcMybatisConfiguration.addMappers("pro.chenggang.project.reactive.mybatis.support.common.mapper");
            return r2dbcMybatisConfiguration;
        });
        reactiveSqlSession = reactiveSqlSessionFactory.openSession();
    }

    @Test
    void getMapper() {
        DeptMapper deptMapper = reactiveSqlSession.getMapper(DeptMapper.class);
        Assertions.assertNotNull(deptMapper);
        EmpMapper empMapper = reactiveSqlSession.getMapper(EmpMapper.class);
        Assertions.assertNotNull(empMapper);
    }

}
