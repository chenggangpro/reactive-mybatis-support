/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package pro.chenggang.project.reactive.mybatis.support.r2dbc.exception;

import io.r2dbc.spi.R2dbcException;

/**
 * The type R2dbc result exception.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class R2dbcResultException extends R2dbcException {

    private static final long serialVersionUID = 7651039074018197180L;

    /**
     * Instantiates a new R2dbc result exception.
     */
    public R2dbcResultException() {
        super();
    }

    /**
     * Instantiates a new R2dbc result exception.
     *
     * @param reason the reason
     */
    public R2dbcResultException(String reason) {
        super(reason);
    }

    /**
     * Instantiates a new R2dbc result exception.
     *
     * @param reason the reason
     * @param cause  the cause
     */
    public R2dbcResultException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * Instantiates a new R2dbc result exception.
     *
     * @param cause the cause
     */
    public R2dbcResultException(Throwable cause) {
        super(cause);
    }
}
