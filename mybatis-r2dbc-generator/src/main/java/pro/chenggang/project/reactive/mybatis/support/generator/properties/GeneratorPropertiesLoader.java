package pro.chenggang.project.reactive.mybatis.support.generator.properties;

/**
 * The generator properties loader
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface GeneratorPropertiesLoader {

    /**
     * Load GeneratorProperties
     *
     * @return the GeneratorProperties
     */
    GeneratorProperties load();
}
