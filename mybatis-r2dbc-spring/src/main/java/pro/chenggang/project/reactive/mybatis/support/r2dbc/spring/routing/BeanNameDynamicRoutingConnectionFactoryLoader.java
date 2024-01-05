package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * The dynamic routing connection factory loader load connection factories from spring's application context
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
public class BeanNameDynamicRoutingConnectionFactoryLoader implements DynamicRoutingConnectionFactoryLoader, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Map<String, ConnectionFactory> load() {
        return applicationContext.getBeansOfType(ConnectionFactory.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
