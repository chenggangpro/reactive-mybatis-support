package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * The type R2dbc mybatis routing connection factory properties.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Getter
@Setter
@ToString
public class R2dbcMybatisRoutingConnectionFactoryProperties {

    public static final String PREFIX = "spring.r2dbc.mybatis.routing";

    /**
     * Whether enable the routing connection factory configuration
     */
    private Boolean enabled = false;

    /**
     * The r2dbc mybatis connection factory properties definitions
     */
    private List<R2dbcMybatisConnectionFactoryProperties> definitions = new ArrayList<>();
}
