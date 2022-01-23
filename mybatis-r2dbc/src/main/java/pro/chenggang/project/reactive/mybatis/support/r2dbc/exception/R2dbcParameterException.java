package pro.chenggang.project.reactive.mybatis.support.r2dbc.exception;

import io.r2dbc.spi.R2dbcException;

/**
 * The type R2dbc parameter exception.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class R2dbcParameterException extends R2dbcException {

    private static final long serialVersionUID = 1600143335984067382L;

    /**
     * Instantiates a new R2dbc parameter exception.
     */
    public R2dbcParameterException() {
        super();
    }

    /**
     * Instantiates a new R2dbc parameter exception.
     *
     * @param reason the reason
     */
    public R2dbcParameterException(String reason) {
        super(reason);
    }

    /**
     * Instantiates a new R2dbc parameter exception.
     *
     * @param reason the reason
     * @param cause  the cause
     */
    public R2dbcParameterException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * Instantiates a new R2dbc parameter exception.
     *
     * @param cause the cause
     */
    public R2dbcParameterException(Throwable cause) {
        super(cause);
    }
}
