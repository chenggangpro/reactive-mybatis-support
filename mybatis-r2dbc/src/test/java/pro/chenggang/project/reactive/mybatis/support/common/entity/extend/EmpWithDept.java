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
package pro.chenggang.project.reactive.mybatis.support.common.entity.extend;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmpWithDept {

    /**
     * emp no
     */
    protected Long empNo;

    /**
     * emp name
     */
    protected String empName;

    /**
     * job
     */
    protected String job;

    /**
     * manager
     */
    protected String manager;

    /**
     * hire date
     */
    protected LocalDate hireDate;

    /**
     * salary
     */
    protected Integer salary;

    /**
     * kpi
     */
    protected BigDecimal kpi;

    /**
     * dept no
     */
    protected Long deptNo;

    /**
     * create time
     */
    protected LocalDateTime createTime;

    /**
     * dept
     */
    protected Dept dept;
}
