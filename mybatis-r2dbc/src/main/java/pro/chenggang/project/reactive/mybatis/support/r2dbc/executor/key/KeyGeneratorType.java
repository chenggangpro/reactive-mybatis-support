package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key;

/**
 * The enum Key generator type.
 *
 * @author evans
 * @version 1.0.2
 * @since 1.0.2
 */
public enum KeyGeneratorType {

    /**
     * no generated key
     */
    NONE,

    /**
     * simple returned generated value
     */
    SIMPLE_RETURN,

    /**
     * select key before
     */
    SELECT_KEY_BEFORE,

    /**
     * select key after
     */
    SELECT_KEY_AFTER,

    ;

}
