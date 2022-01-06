package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.extend;

import lombok.*;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Emp;

/**
 * @author chenggang
 * @date 12/15/21.
 */
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmpWithDept extends Emp {

    private Dept dept;
}