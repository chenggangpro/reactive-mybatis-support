package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service;

import reactor.core.publisher.Mono;

/**
 * @author Gang Cheng
 * @date 1/4/22.
 * @version 1.0.0
 */
public interface BenchmarkService {

    Mono<?> getFromDb();

    Mono<?> getFromDb(String id);

    Mono<?> insertDb();

    Mono<?> insertThenDeleteDb();
}
