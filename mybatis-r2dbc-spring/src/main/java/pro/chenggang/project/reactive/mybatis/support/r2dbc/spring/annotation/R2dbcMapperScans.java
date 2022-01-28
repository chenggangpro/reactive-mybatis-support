/**
 * Copyright 2010-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Container annotation that aggregates several {@link R2dbcMapperScan} annotations.
 *
 * <p>
 * Can be used natively, declaring several nested {@link R2dbcMapperScan} annotations. Can also be used in conjunction with
 * Java 8's support for repeatable annotations, where {@link R2dbcMapperScan} can simply be declared several times on the
 * same method, implicitly generating this container annotation.
 * <p>
 * copy from original MapperScans
 *
 * @author Kazuki Shimizu
 * @see R2dbcMapperScan
 * @since 1.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(R2dbcMapperScannerRegistrar.R2dbcRepeatingRegistrar.class)
public @interface R2dbcMapperScans {

    /**
     * R2dbcMapperScans
     *
     * @return R2dbcMapperScan
     */
    R2dbcMapperScan[] value();
}
