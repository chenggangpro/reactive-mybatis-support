[![Build and Test With Maven [2.x]](https://github.com/chenggangpro/reactive-mybatis-support/actions/workflows/workflow-2.x.yml/badge.svg?branch=2.x)](https://github.com/chenggangpro/reactive-mybatis-support/actions/workflows/workflow-2.x.yml) [![Maven Central](https://img.shields.io/maven-central/v/pro.chenggang/mybatis-r2dbc?versionSuffix=RELEASE&versionPrefix=2&color=%2352c82c)](https://search.maven.org/artifact/pro.chenggang/reactive-mybatis-support) 

[![Build and Test With Maven [3.x]](https://github.com/chenggangpro/reactive-mybatis-support/actions/workflows/workflow-3.x.yml/badge.svg?branch=3.x)](https://github.com/chenggangpro/reactive-mybatis-support/actions/workflows/workflow-3.x.yml) [![Maven Central](https://img.shields.io/maven-central/v/pro.chenggang/mybatis-r2dbc?versionSuffix=RELEASE&versionPrefix=3&color=%2352c82c)](https://search.maven.org/artifact/pro.chenggang/reactive-mybatis-support)
# [Reactive Mybatis Support](https://github.com/chenggangpro/reactive-mybatis-support/wiki)

## JDK/R2DBC-SPI/Spring-Boot Compatibility

* Refer to the table below to determine the appropriate version of `mybatis-r2dbc` for your project.
> Exclude the `v` prefix when using the version number.

| Compiled JDK            | 8                                                                                                                                                    | 11                                                                                                                                                   | 17                                                                                                                                                   |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| r2dbc-spi               | `0.9.1.RELEASE`                                                                                                                                      | `1.0.0.RELEASE`                                                                                                                                      | `1.0.0.RELEASE`                                                                                                                                      |
| mybatis-r2dbc           | [![Maven Central](https://img.shields.io/maven-central/v/pro.chenggang/mybatis-r2dbc?versionSuffix=RELEASE&versionPrefix=2&label=%20&color=%2352c82c)](https://search.maven.org/artifact/pro.chenggang/mybatis-r2dbc) | [![Maven Central](https://img.shields.io/maven-central/v/pro.chenggang/mybatis-r2dbc?versionSuffix=RELEASE&versionPrefix=2&label=%20&color=%2352c82c)](https://search.maven.org/artifact/pro.chenggang/mybatis-r2dbc) | --                                                                                                                                                   |
| mybatis-r2dbc-generator | [![Maven Central](https://img.shields.io/maven-central/v/pro.chenggang/mybatis-r2dbc-generator?versionSuffix=RELEASE&versionPrefix=2&label=%20&color=%2352c82c)](https://search.maven.org/artifact/pro.chenggang/mybatis-r2dbc-generator) | [![Maven Central](https://img.shields.io/maven-central/v/pro.chenggang/mybatis-r2dbc-generator?versionSuffix=RELEASE&versionPrefix=3&label=%20&color=%2352c82c)](https://search.maven.org/artifact/pro.chenggang/mybatis-r2dbc-generator) | --                                                                                                                                                   |
| mybatis-r2dbc-spring    | [![Maven Central](https://img.shields.io/maven-central/v/pro.chenggang/mybatis-r2dbc-spring?versionSuffix=RELEASE&versionPrefix=2&label=%20&color=%2352c82c)](https://search.maven.org/artifact/pro.chenggang/mybatis-r2dbc-spring) | --                                                                                                                                                   | [![Maven Central](https://img.shields.io/maven-central/v/pro.chenggang/mybatis-r2dbc-spring?versionSuffix=RELEASE&versionPrefix=3&label=%20&color=%2352c82c)](https://search.maven.org/artifact/pro.chenggang/mybatis-r2dbc-spring) |
| Spring Boot             | [`2.7.x`,`3.0.0`)                                                                                                                                    | --                                                                                                                                                   | [`3.0.0`,`~`)                                                                                                                                        |


* The whole project `reactive-mybatis-support(2.x.x)` is compiled with `JDK8` and `SpringBoot(2.7.x)`, aka `r2dbc-spi(0.9.1.RELEASE)`
* The `mybatis-r2dbc(3.x.x)` and `mybatis-r2dbc-generator(3.x.x)` are compiled with `JDK11`, aka `r2dbc-spi(1.0.0.RELEASE)`.
* The `mybatis-r2dbc-spring(3.x.x)` is compiled with `JDK17` and  `SpringBoot(3.x.x)`, aka `r2dbc-spi(1.0.0.RELEASE)`.

#### Instruction

* Reactive Mybatis Support is aimed to adapt original mybatis to reactive project (aka WebFlux/Reactor3) with r2dbc drivers.
* `mybatis-r2dbc` module is inspired by [linux-china/mybatis-r2dbc](https://github.com/linux-china/mybatis-r2dbc) and based on `mybatis3`'s original source code.
* `mybatis-generator` module is used to adapt `mybatis-dynamic-sql` to reactive project.
* Most of the MyBatis3 features are applicable, but there are a few features that are not supported:
    * ❌ 1 . mybatis-plugin
    * ❌ 2 . multi ResultSets or Results
    * ❌ 3 . nested query with multi SQL statements
    * ❌️ 4 . blocking java type (aka: InputStream .eg)
* For more usage instructions, please refer to the wiki: [WIKI](https://github.com/chenggangpro/reactive-mybatis-support/wiki)

#### Maven Central

* dependency

```xml
<dependencies>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>mybatis-r2dbc</artifactId>
      <version>${compatible-version}</version>
    </dependency>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>mybatis-r2dbc-generator</artifactId>
      <version>${compatible-version}</version>
    </dependency>
    <dependency>
      <groupId>pro.chenggang</groupId>
      <artifactId>mybatis-r2dbc-spring</artifactId>
      <version>${compatible-version}</version>
    </dependency>
</dependencies>

```

#### Reference

* 1. [mybatis/mybatis3](https://github.com/mybatis/mybatis-3)
* 2. [mybatis/mybatis-dynamic-sql](https://github.com/mybatis/mybatis-dynamic-sql)
* 3. [linux-china/mybatis-r2dbc](https://github.com/linux-china/mybatis-r2dbc)
* 4. [DefaultDatabaseClient](https://github.com/spring-projects/spring-data-r2dbc/blob/main/src/main/java/org/springframework/data/r2dbc/core/DefaultDatabaseClient.java)
