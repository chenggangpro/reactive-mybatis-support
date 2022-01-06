package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.extend;

import lombok.*;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Emp;

import java.util.List;

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
public class DeptWithEmp extends Dept {

    private List<Emp> empList;
}
