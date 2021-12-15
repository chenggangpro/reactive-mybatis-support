# Reactive-Mybatis-Support

#### Instruction

* Aimed to adapt mybatis to reactive project (aka WebFlux/Reactor3)
* `mybatis-r2dbc` Base on [linux-china/mybatis-r2dbc](https://github.com/linux-china/mybatis-r2dbc) 
* Using `mybatis-generator` adapt `mybatis-dynamic-sql` to  reactive project
* Support SpringBoot AutoConfiguration,AutoMapperScan and so on.
* Support Spring's Transaction .
* Unsupported mybatis3 feature:
    * ❌ 1 . mybatis-plugin
    * ❌ 2 . resultSet and resultOrdered = true
    * ❌ 3 . nested query
    * ❌ 4 . generated key by nested query
    * ❌ 5 . blocking java type (aka: InputStream .eg)
    * ⚠️ 6 . there is no reactive-sql-session cache implementation
    * ⚠️ 7 . Mapper Method only support `Flux<T>`/`Mono<T>`/`Mono<Void>`/`Flux<Void>`, and not supported `void` 
#### Examples

* Generate `mybatis-dynamic-sql` 

> Note:
> the generator based on `mybatis-generator-core` ,that cause generator rely on jdbc-driver

* import dependency

```xml
<dependencies>
      <dependency>
          <groupId>mysql</groupId>
          <artifactId>mysql-connector-java</artifactId>
          <scope>test</scope>
      </dependency>
      <dependency>
          <groupId>org.apache.commons</groupId>
          <artifactId>commons-pool2</artifactId>
          <scope>test</scope>
      </dependency>
      <dependency>
          <groupId>org.mybatis.dynamic-sql</groupId>
          <artifactId>mybatis-dynamic-sql</artifactId>
      </dependency>
      <dependency>
          <groupId>pro.chenggang</groupId>
          <artifactId>mybatis-reactive-generator</artifactId>
          <version>${version}</version>
          <scope>test</scope>
      </dependency>
</dependencies>
```

* copy `mybatis-generator.yml` form `source-code/mybatis-reactive-generator/resources/META-INF/mybatis-generator.yml`
* modify source database settings in `mybatis-generator.yml`
* add a test case 
```java
public class MyBatisGeneratorAction {

    @Test
    public void generate(){
        MybatisDynamicCodeGenerator.getInstance().generate(MyBatisGeneratorAction.class);
    }

}
```
* run the test ,then it will generate the dynamic code ,mapper interface ,mapper xml
* Also see `mybatis-reactive-generator`'s test cases
    
* Using in spring environment

    * import dependency
    
    ```xml
    <dependencies>
        <dependency>
            <groupId>org.mariadb</groupId>
            <artifactId>r2dbc-mariadb</artifactId>
        </dependency>
        <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-data-r2dbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.dynamic-sql</groupId>
            <artifactId>mybatis-dynamic-sql</artifactId>
        </dependency>
        <dependency>
            <groupId>pro.chenggang</groupId>
            <artifactId>mybatis-r2dbc-spring</artifactId>
            <version>${version}</version>
        </dependency>
    </dependencies>
    
    ```
    
    * then use in project as usual ,also support `@Transaction` and `TransactionalOperator`.
    * other details sees the `mybatis-r2dbc-spring`'s test cases,the test case include mapper tests and service tests.
    * before run the `mybatis-r2dbc-spring`'s test cases ,you should execute `test_prepare.sql` in the test resources.
    * spring-boot-test is not support `@Transaction` in tests ,link [Spring Issue](https://github.com/spring-projects/spring-framework/issues/24226)
    


#### Adapt Spring

* In the original project [linux-china/mybatis-r2dbc](https://github.com/linux-china/mybatis-r2dbc) ,
the `ReactiveSqlSession` get the `Connection` use the `ConnecionFactory` directly.This cause an issue when you used in
spring environment and multiple database operation in one transaction.In reactive environment,if you want handle with resource 
,you should use `Mono.usingWhen() or Flux.usingWhen()` and not `doFinally()`.
* To resolve above issue, refer to the `spring-data-r2dbc` project .
Found the [DefaultDatabaseClient](https://github.com/spring-projects/spring-data-r2dbc/blob/main/src/main/java/org/springframework/data/r2dbc/core/DefaultDatabaseClient.java)
 resolve this issue perfectly

#### Reference

* 1. [linux-china/mybatis-r2dbc](https://github.com/linux-china/mybatis-r2dbc)
* 2. [DefaultDatabaseClient](https://github.com/spring-projects/spring-data-r2dbc/blob/main/src/main/java/org/springframework/data/r2dbc/core/DefaultDatabaseClient.java)