package pro.chenggang.project.reactive.mybatis.support.r2dbc.application.entity.extend;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.entity.model.Emp;

/**
 * @author Gang Cheng
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
