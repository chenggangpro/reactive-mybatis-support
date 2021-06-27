package pro.chenggang.project.reactivemybatissupport.mybatisr2dbcspring.application;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbcspring.application.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * User Mapper
 *
 * @author linux_china
 */
@Mapper
public interface UserMapper {

    Mono<Integer> batchInsert(@Param("userList") List<User> userList);

    Mono<Integer> insert(User user);

    Mono<Boolean> update(User demo);

    Mono<User> findById(Integer id);

    Mono<User> dynamicFindExample(User example);

    Mono<Map<String, Object>> find2ById(Integer id);

    @Select("SELECT id, nick, created_at FROM people WHERE nick = #{value}")
    @ResultMap("UserResultMap")
    Mono<User> findByNick(@Param("value") String nick);

    Flux<User> findAll();

    Mono<Long> getAllCount();
}
