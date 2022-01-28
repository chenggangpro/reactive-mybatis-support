package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.annotation.R2dbcMapperScan;

/**
 * @author Gang Cheng
 */
@R2dbcMapperScan(basePackages = "pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper")
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
