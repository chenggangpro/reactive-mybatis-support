package pro.chenggang.project.reactive.mybatis.support.common.entity.extend;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Emp;

import java.time.LocalDateTime;
import java.util.List;

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
public class DeptWithEmpList {

    /**
     * dept no
     */
    protected Long deptNo;

    /**
     * dept name
     */
    protected String deptName;

    /**
     * location
     */
    protected String location;

    /**
     * create time
     */
    protected LocalDateTime createTime;

    /**
     * emp list
     */
    protected List<Emp> empList;
}
