[![Java CI with Maven](https://github.com/chenggangpro/reactive-mybatis-support/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/chenggangpro/reactive-mybatis-support/actions/workflows/maven.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Pom Maven Central](https://maven-badges.herokuapp.com/maven-central/pro.chenggang/reactive-mybatis-support/badge.svg)](https://maven-badges.herokuapp.com/maven-central/pro.chenggang/reactive-mybatis-support)
# Reactive-Mybatis-Support

This project has met the general business usage scenarios, including:
* 1 . Parameter parsing and mapping
* 2 . One-to-many associative relationships for result mapping
* 3 . Result mapping for one-to-one relationships
* 4 . Returning a generated key
* 5 . generated key by nested query (`@SelectKey`/`<selectKey>`)
* 6 . Manual transaction operation
* 7 . SpringBoot transaction Integration

#### Instruction

* Aimed to adapt mybatis to reactive project (aka WebFlux/Reactor3)
* `mybatis-r2dbc` Base on [linux-china/mybatis-r2dbc](https://github.com/linux-china/mybatis-r2dbc) and `mybatis3`'s original source code
* Using `mybatis-generator` adapt `mybatis-dynamic-sql` to  reactive project
* Support SpringBoot AutoConfiguration, `@R2dbcMapperScan`/`@R2dbcMapperScans` for scan `@Mapper`, Spring XML bean config .
* Support Spring's Transaction.
* Unsupported mybatis3 feature:
    * ❌ 1 . mybatis-plugin
    * ❌ 2 . multi resultSet and `resultOrdered = true` in mapper.XML
    * ❌ 3 . nested query with multi SQL
    * ⚠️ 4 . blocking java type (aka: InputStream .eg)
* ⚠️ Mapper Method's return type only support `Flux<T>`/`Mono<T>`/`Mono<Void>`/`Flux<Void>`, and not supported `void`
* Using Reactor's Context to implement Transaction
* More detail, please see source code and test suits, tests use MySQL database with `test-prepare.sql` schema setup
* It has been piloted in a small scale within the company, and any bugs found will be updated at any time

#### ⚠️ Known issues

* ⚠️ `r2dbc-mysql` driver
  * when calling `Row#<T> T get(int index, Class<T> type)`,with jdbcType is `BIGINT` and javaType is `Long.class`
  * the driver will occur an exception, because  the driver is deeply bound to `BitInteger.class`,and can't cast to `Long.class`
  * MySQL-JDBC driver and `r2dbc-mariadb` driver don't have this issue 
  * possible link  [r2dbc-mysql/issues/177](https://github.com/mirromutth/r2dbc-mysql/issues/177)
  * This might be fixed in the next release of the driver
* ⚠️ `r2dbc-postgresql` driver
  * when calling `Statement.bind(int index, Object value)`,the driver does not recognize the `?` parameter placeholder, only `$` parameter placeholder are recognized 
  * POSTGRESQL-JDBC driver does not have this problem.
  * possible link [r2dbc-postgresql/pull/468](https://github.com/pgjdbc/r2dbc-postgresql/pull/468)
  * This might be fixed in the next release of the driver

#### Maven Central

* dependency

```xml
<dependencies>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>mybatis-r2dbc</artifactId>
      <version>${latest.version}</version>
    </dependency>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>mybatis-r2dbc-generator</artifactId>
      <version>${latest.version}</version>
    </dependency>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>mybatis-r2dbc-spring</artifactId>
      <version>${latest.version}</version>
    </dependency>
</dependencies>

```

#### Examples

> [reactive-mybatis-support-examples](https://github.com/chenggangpro/reactive-mybatis-support-examples)

##### Using mybatis-dynamic-sql

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
          <artifactId>mybatis-r2dbc-generator</artifactId>
          <version>${latest.version}</version>
          <scope>test</scope>
      </dependency>
</dependencies>
```

* copy `mybatis-generator.yml` form `source-code/mybatis-reactive-generator/resources/META-INF/mybatis-generator.yml`
* modify source database settings in `mybatis-generator.yml`
* add a test case or a main method

```java
public class MyBatisGeneratorAction {

    @Test
    public void generate(){
        MybatisDynamicCodeGenerator.getInstance().generate(MyBatisGeneratorAction.class);
    }
    
    // or
    
    public static void main(String[] args){
        MybatisDynamicCodeGenerator.getInstance().generate(MyBatisGeneratorAction.class);
    }

}
```
* run the test ,then it will generate the dynamic code ,mapper interface ,mapper xml
* also see `mybatis-reactive-generator`'s test cases
    
#### Using in spring environment

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
          <version>${latest.version}</version>
      </dependency>
      <dependency>
          <groupId>pro.chenggang</groupId>
          <artifactId>mybatis-r2dbc-spring</artifactId>
          <version>${latest.version}</version>
      </dependency>
  </dependencies>
  
  ```
  
  * then use in project as usual ,also support `@Transaction` and `TransactionalOperator`.
  * other details sees the `mybatis-r2dbc-spring`'s test cases,the test case include mapper tests and service tests.
  * before run the `mybatis-r2dbc-spring`'s test cases ,you should execute `test_prepare.sql` in the test resources.
  * spring-boot-test is not support `@Transaction` in tests ,link [Spring Issue](https://github.com/spring-projects/spring-framework/issues/24226)
  
  * customize `ConnectionFactoryOptions`
    
    ```java
    @Bean
    public ConnectionFactoryOptionsCustomizer connectionFactoryOptionsCustomizer() {
        return connectionFactoryOptionsBuilder -> connectionFactoryOptionsBuilder
                .option(Option.valueOf("name"), "value");
    }
    ```
    
  * customize `R2dbcMybatisConfiguration`
    
    ```java
    @Bean
    public R2dbcMybatisConfigurationCustomizer r2dbcMybatisConfigurationCustomizer() {
        return r2dbcMybatisConfiguration -> r2dbcMybatisConfiguration.setLogPrefix("mybatis-log");
    }
    ```
  * custom mapper scan

    * Original `@MapperScan` can be replaced with `@R2dbcMapperScan`
    * Original `@MapperScans` can be replaced with `@R2dbcMapperScans`

##### Using without mybatis-dynamic-sql

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
            <groupId>pro.chenggang</groupId>
            <artifactId>mybatis-r2dbc-spring</artifactId>
            <version>${latest.version}</version>
        </dependency>
    </dependencies>
    
    ```
    
    * then use in project as usual ,also support `@Transaction` and `TransactionalOperator`.

#### Reference

* 1. [mybatis/mybatis3](https://github.com/mybatis/mybatis-3)
* 2. [mybatis/mybatis-dynamic-sql](https://github.com/mybatis/mybatis-dynamic-sql)
* 3. [linux-china/mybatis-r2dbc](https://github.com/linux-china/mybatis-r2dbc)
* 4. [DefaultDatabaseClient](https://github.com/spring-projects/spring-data-r2dbc/blob/main/src/main/java/org/springframework/data/r2dbc/core/DefaultDatabaseClient.java)
