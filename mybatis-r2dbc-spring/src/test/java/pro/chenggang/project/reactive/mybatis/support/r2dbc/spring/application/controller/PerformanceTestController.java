package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.BusinessService;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

/**
 * @author: chenggang
 * @date 7/12/21.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class PerformanceTestController {

    private final BusinessService businessService;

    @PostMapping("/test/insert")
    public Mono<Map> testInsertPerformance(@RequestBody Dept dept){
        return businessService.doInsertPerformance(dept)
                .map(insertResult -> {
                    return Collections.singletonMap("EffectRowCount",insertResult);
                });
    }

    @GetMapping("/test/select")
    public Mono<Map> testSelectPerformance(){
        return businessService.doSelectAllPerformance()
                .collectList()
                .map(peopleList -> {
                    return Collections.singletonMap("AllRowList",peopleList);
                });
    }
}
