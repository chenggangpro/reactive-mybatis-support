package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.model.User;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.UserMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.stream.Stream;

/**
 * MapperProxyFactory test
 *
 * @author linux_china
 */
public class MapperTest extends TestApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testUserMapperFindById() throws Exception {
        Mono<User> userMono = userMapper.findById(1);
        StepVerifier.create(userMono)
                .expectNextMatches(user -> user.getId() == 1)
                .verifyComplete();
    }

    @Test
    public void testUserMapperFindEmpty() throws Exception {
        Mono<Map<String, Object>> userMono = userMapper.find2ById(100);
        Map<String, Object> user = userMono.block();
        System.out.println(user);
    }

    @Test
    public void testUserMapperDynamicFindExample() throws Exception {
        User example = new User(1, "linux_china");
        Mono<User> userMono = userMapper.dynamicFindExample(example);
        StepVerifier.create(userMono)
                .expectNextMatches(user -> user.getId() == 1)
                .verifyComplete();
    }

    @Test
    public void testUserMapperGetAllCount() throws Exception {
        userMapper.getAllCount().subscribe(count -> {
            System.out.println(count);
        });
        Thread.sleep(2000);
    }

    @Test
    public void testUserMapperFindAll() throws Exception {
        userMapper.findAll().subscribe(user -> {
            System.out.println(user.getNick());
        });
        Thread.sleep(2000);
    }

    @Test
    public void testUserMapperFindByNick() throws Exception {
        userMapper.findByNick("linux_china").subscribe(user -> {
            System.out.println(user.getId());
        });
        Thread.sleep(2000);
    }

    @Test
    public void testUserMapperInsert() throws Exception {
        User user = new User();
        user.setNick("nick007");
        userMapper.insert(user).subscribe(id -> {
            System.out.println(id);
            System.out.println("ID:" + user.getId());
        });
        Thread.sleep(5000);
    }

    @Test
    public void testUserMapperInsertBatch() throws Exception {
        User user = new User();
        user.setNick("nick007");
        userMapper.getAllCount()
                .doOnNext((count)-> System.out.println("Total Count Before Insert : " + count))
                .thenMany(Flux.fromStream(Stream.of(user,user)))
                .collectList()
                .flatMap(userList -> userMapper.batchInsert(userList))
                .doOnNext(rowCount -> System.out.println("Insert Row Count : " + rowCount))
                .then(userMapper.getAllCount())
                .doOnNext((count)-> System.out.println("Total Count After Insert : " + count))
                .subscribe();
        Thread.sleep(5000);
    }
}
