package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.apache.ibatis.exceptions.TooManyResultsException;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.BasicColumn;
import org.springframework.beans.factory.annotation.Autowired;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.ReactiveMyBatis3Utils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.People;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.PeopleMapper;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isGreaterThan;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.PeopleDynamicSqlSupport.id;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.PeopleDynamicSqlSupport.nick;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.PeopleDynamicSqlSupport.people;

/**
 * MapperProxyFactory test
 *
 * @author linux_china
 */
public class PeopleMapperTest extends TestApplicationTests {

    @Autowired
    private PeopleMapper peopleMapper;

    @Test
    void count() {
        peopleMapper.count(dsl -> dsl)
                .as(StepVerifier::create)
                .assertNext(count -> assertThat(count).isEqualTo(2))
                .verifyComplete();
    }

    @Test
    void delete() {
        peopleMapper.delete(dsl -> dsl.where(id, isEqualTo(1)))
                .as(this::withRollback)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    void insert() {
        People people = new People();
        people.setNick("test_people");
        people.setCreatedAt(LocalDateTime.now());
        peopleMapper.insert(people)
                .as(this::withRollback)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    void insertMultiple() {
        People people1 = new People();
        people1.setNick("test_people_1");
        people1.setCreatedAt(LocalDateTime.now());
        People people2 = new People();
        people2.setNick("test_people_2");
        people2.setCreatedAt(LocalDateTime.now());
        List<People> peopleList = new ArrayList<>();
        peopleList.add(people1);
        peopleList.add(people2);
        peopleMapper.insertMultiple(peopleList)
                .as(this::withRollback)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEqualTo(2))
                .verifyComplete();
    }

    @Test
    void insertSelective() {
        People people = new People();
        people.setNick("test_people");
        peopleMapper.insert(people)
                .as(this::withRollback)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    void selectOne() {
        peopleMapper.selectOne(dsl -> dsl.where(id, isEqualTo(1)))
                .as(StepVerifier::create)
                .assertNext(people -> assertThat(people.getId()).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    void selectOneWithManyResult() {
        peopleMapper.selectOne(dsl -> dsl)
                .as(StepVerifier::create)
                .expectError(TooManyResultsException.class)
                .verify();
    }

    @Test
    void select() {
        peopleMapper.select(dsl -> dsl.where(id,isGreaterThan(0)))
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void selectDistinct() {
        peopleMapper.selectDistinct(dsl -> dsl.where(id,isGreaterThan(0)))
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void selectDistinctString() {
        ReactiveMyBatis3Utils.selectDistinct(peopleMapper::selectManyStrings,
                        new BasicColumn[]{nick},
                        people,
                        dsl -> dsl.where(id, isGreaterThan(0))
                )
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void update() {
        peopleMapper.update(dsl -> dsl.set(nick).equalTo("update_people").where(id, isEqualTo(1)))
                .as(this::withRollback)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    void updateAll() {
        People people = new People();
        people.setNick("test_people");
        people.setCreatedAt(LocalDateTime.now());
        peopleMapper.updateAll(people, dsl -> dsl.where(id, isEqualTo(1)))
                .as(this::withRollback)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    void updateAllByPrimaryKey() {
        People people = new People();
        people.setId(1);
        people.setNick("test_people");
        people.setCreatedAt(LocalDateTime.now());
        peopleMapper.updateAllByPrimaryKey(people)
                .as(this::withRollback)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    void updateSelective() {
        People people = new People();
        people.setNick("test_people");
        peopleMapper.updateSelective(people,dsl -> dsl.where(id,isEqualTo(1)))
                .as(this::withRollback)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEqualTo(1))
                .verifyComplete();
    }


}
