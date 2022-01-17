[![Java CI with Maven](https://github.com/chenggangpro/reactive-mybatis-support/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/chenggangpro/reactive-mybatis-support/actions/workflows/maven.yml)
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
* Support SpringBoot AutoConfiguration,AutoMapperScan and so on.
* Support Spring's Transaction.
* Unsupported mybatis3 feature:
    * ❌ 1 . mybatis-plugin
    * ❌ 2 . multi resultSet and `resultOrdered = true` in mapper.XML
    * ❌ 3 . nested query with multi SQL
    * ⚠️ 4 . blocking java type (aka: InputStream .eg)
    * ⚠️ 5 . Mapper Method only support `Flux<T>`/`Mono<T>`/`Mono<Void>`/`Flux<Void>`, and not supported `void`
* Using Reactor's Context to implement Transaction
* More detail, please see source code and test suits, tests use MySQL database with `test-prepare.sql` schema setup
* It has been piloted in a small scale within the company, and any bugs found will be updated at any time

#### ⚠️ Known issues

* ⚠️ `r2dbc-mysql` driver
  * when calling `Row#<T> T get(int index, Class<T> type)`,with jdbcType is `BIGINT` and javaType is `Long.class`
  * the driver will occur an exception, because  the driver depth binding is `BitInteger.class`,and can't cast to `Long.class`
  * the MySQL-JDBC driver and `r2dbc-mariadb` driver don't have this issue 
  * possible link  [r2dbc-mysql/issues/177](https://github.com/mirromutth/r2dbc-mysql/issues/177)
  * this might be fix in next driver release
* ⚠️ `r2dbc-postgresql` driver
  * when calling `Statement.bind(int index, Object value)`,the driver not recognized the `?` parameter placeholder, only recognized `$` parameter placeholder 
  * the POSTGRESQL-JDBC driver does not have this problem.
  * possible link [r2dbc-postgresql/pull/468](https://github.com/pgjdbc/r2dbc-postgresql/pull/468)
  * this might be fix in next driver release

#### Maven Central

* dependency bom

```xml
<dependencyManagement>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>reactive-mybatis-support</artifactId>
      <version>${latest.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
</dependencyManagement>
```

* module dependency

```xml
<dependencies>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>mybatis-r2dbc</artifactId>
    </dependency>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>mybatis-r2dbc-generator</artifactId>
    </dependency>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>mybatis-r2dbc-spring</artifactId>
    </dependency>
</dependencies>

```

#### Examples

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
            <version>${version}</version>
        </dependency>
    </dependencies>
    
    ```
    
    * then use in project as usual ,also support `@Transaction` and `TransactionalOperator`.

#### Reference

* 1. [linux-china/mybatis-r2dbc](https://github.com/linux-china/mybatis-r2dbc)
* 2. [DefaultDatabaseClient](https://github.com/spring-projects/spring-data-r2dbc/blob/main/src/main/java/org/springframework/data/r2dbc/core/DefaultDatabaseClient.java)
