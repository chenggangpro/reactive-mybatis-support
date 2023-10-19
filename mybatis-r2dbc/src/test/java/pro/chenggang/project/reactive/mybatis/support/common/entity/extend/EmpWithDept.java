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
 * @author evans
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
