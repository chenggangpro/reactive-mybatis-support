package pro.chenggang.project.reactive.mybatis.support.r2dbc.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisBaseTests;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.mapper.PeopleMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.model.People;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author: chenggang
 * @date 7/11/21.
 */
public class PeopleMapperTests extends MybatisBaseTests {

    private PeopleMapper peopleMapper;

    @BeforeAll
    public void setUp() throws Exception{
        super.setUp();
        this.peopleMapper = super.reactiveSqlSessionFactory.openSession().getMapper(PeopleMapper.class);
    }

    @Test
    public void testPeopleMapperFindById() throws Exception {
        Mono<People> peopleMono = peopleMapper.findById(1);
        StepVerifier.create(peopleMono)
                .expectNextMatches(people -> people.getId() == 1)
                .verifyComplete();
    }

    @Test
    public void testPeopleMapperFind2ById() throws Exception {
        Mono<Map<String, Object>> peopleMono = peopleMapper.find2ById(1);
        Map<String, Object> people = peopleMono.block();
        System.out.println(people);
    }

    @Test
    public void testPeopleMapperDynamicFindExample() throws Exception {
        People example = new People(1, "mybatis", LocalDateTime.now());
        Mono<People> peopleMono = peopleMapper.dynamicFindExample(example);
        StepVerifier.create(peopleMono)
                .expectNextMatches(people -> people.getId() == 1)
                .verifyComplete();
    }

    @Test
    public void testPeopleMapperGetAllCount() throws Exception {
        peopleMapper.getAllCount().subscribe(count -> {
            System.out.println(count);
        });
        Thread.sleep(2000);
    }

    @Test
    public void testPeopleMapperFindAll() throws Exception {
        peopleMapper.findAll().subscribe(people -> {
            System.out.println(people.getNick());
        });
        Thread.sleep(2000);
    }

    @Test
    public void testPeopleMapperFindByNick() throws Exception {
        peopleMapper.findByNick("linux_china").subscribe(people -> {
            System.out.println(people.getId());
        });
        Thread.sleep(2000);
    }

    @Test
    public void testPeopleMapperInsert() throws Exception {
        People people = new People();
        people.setNick("nick007");
        peopleMapper.insert(people).subscribe(id -> {
            System.out.println(id);
            System.out.println("ID:" + people.getId());
        });
        Thread.sleep(5000);
    }

    @Test
    public void testPeopleMapperInsertBatch() throws Exception {
        People people = new People();
        people.setNick("nick007");
        peopleMapper.getAllCount()
                .doOnNext((count)-> System.out.println("Total Count Before Insert : " + count))
                .thenMany(Flux.fromStream(Stream.of(people)))
                .collectList()
                .flatMap(peopleList -> peopleMapper.batchInsert(peopleList))
                .doOnNext(rowCount -> System.out.println("Insert Row Count : " + rowCount))
                .then(peopleMapper.getAllCount())
                .doOnNext((count)-> System.out.println("Total Count After Insert : " + count))
                .subscribe();
        Thread.sleep(5000);
    }
}
