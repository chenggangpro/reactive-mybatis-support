package pro.chenggang.project.reactive.mybatis.support.generator.option;

/**
 * The enum Generator type.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public enum GeneratorType {

    /**
     * dynamic sql code
     */
    DYNAMIC,

    /**
     * simple code
     */
    SIMPLE,

    /**
     * simple model only
     */
    MODEL,

    /**
     * simple model and xml only
     */
    MODEL_XML,

    /**
     * dynamic model only
     */
    DYNAMIC_MAPPER,

    ;
}
