package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reactive executor context attribute
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class ReactiveExecutorContextAttribute {

    private final Map<String,Object> attribute = new ConcurrentHashMap<>();

    /**
     * Gets attribute.
     *
     * @return the attribute
     */
    public Map<String, Object> getAttribute() {
        return attribute;
    }
}
