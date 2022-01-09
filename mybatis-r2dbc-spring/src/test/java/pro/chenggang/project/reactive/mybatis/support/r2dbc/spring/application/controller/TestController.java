package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.BenchmarkService;
import reactor.core.publisher.Mono;

/**
 * @author Gang Cheng
 * @date 1/4/22.
 */
@RestController
@RequiredArgsConstructor
public class TestController {

    private final BenchmarkService benchmarkService;

    @GetMapping("/benchmark/list")
    public Mono<?> getFromDb(){
        return benchmarkService.getFromDb();
    }

    @GetMapping("/benchmark/one")
    public Mono<?> getFromDb(@RequestParam("id") String id) {
        return benchmarkService.getFromDb(id);
    }

    @PostMapping("/benchmark/add")
    public Mono<?> insertDb(){
        return benchmarkService.insertDb();
    }

    @PostMapping("/benchmark/add/delete")
    public Mono<?> insertThenDeleteDb(){
        return benchmarkService.insertThenDeleteDb();
    }
}
