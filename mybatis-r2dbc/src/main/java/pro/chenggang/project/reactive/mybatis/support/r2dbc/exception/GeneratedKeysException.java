package pro.chenggang.project.reactive.mybatis.support.r2dbc.exception;

import io.r2dbc.spi.R2dbcException;

/**
 * The Generated keys exception.
 * when returnGeneratedKeys configured and keyColumn is not specific
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.10
 */
public class GeneratedKeysException extends R2dbcException {

    private static final long serialVersionUID = -5831830868937457622L;

    /**
     * Instantiates a new Generated keys exception.
     *
     * @param reason the reason
     */
    public GeneratedKeysException(String reason) {
        super(reason);
    }
}
