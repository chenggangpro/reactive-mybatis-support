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
package pro.chenggang.project.reactive.mybatis.support.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Emp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * emp mapper
 * 
 * @author autoGenerated
 */
@Mapper
public interface EmpMapper {

    Mono<Long> countAll();

    Flux<Emp> selectAll();
}