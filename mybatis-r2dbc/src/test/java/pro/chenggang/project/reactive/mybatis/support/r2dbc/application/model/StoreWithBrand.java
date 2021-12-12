package pro.chenggang.project.reactive.mybatis.support.r2dbc.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author: chenggang
 * @date 12/3/21.
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StoreWithBrand extends Store {

    private Brand brand;
    private List<User> userList;
}
