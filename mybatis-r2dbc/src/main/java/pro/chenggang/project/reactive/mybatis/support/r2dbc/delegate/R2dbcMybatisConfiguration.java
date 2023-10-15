/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialectRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.defaults.DefaultPlaceholderDialectRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLog;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLogFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapterRegistry;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLXML;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * The type R2dbc mybatis configuration.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class R2dbcMybatisConfiguration extends Configuration {

    private final R2dbcMapperRegistry r2dbcMapperRegistry = new R2dbcMapperRegistry(this);
    private final R2dbcTypeHandlerAdapterRegistry r2dbcTypeHandlerAdapterRegistry = new R2dbcTypeHandlerAdapterRegistry(this);
    private final R2dbcStatementLogFactory r2dbcStatementLogFactory = new R2dbcStatementLogFactory(this);
    private final PlaceholderDialectRegistry placeholderDialectRegistry = new DefaultPlaceholderDialectRegistry();
    private final Set<Class<?>> notSupportedDataTypes = new HashSet<>();
    private Integer formattedDialectSqlCacheMaxSize = 10_000;
    private Duration formattedDialectSqlCacheExpireDuration = Duration.ofHours(6);

    private ConnectionFactory connectionFactory;

    /**
     * Instantiates a new R2dbc mybatis configuration.
     */
    public R2dbcMybatisConfiguration() {
        this.loadNotSupportedDataTypes();
    }

    /**
     * Instantiates a new R2dbc mybatis configuration.
     *
     * @param environment the environment
     */
    public R2dbcMybatisConfiguration(Environment environment) {
        super(environment);
        this.loadNotSupportedDataTypes();
    }

    /**
     * load not supported data types
     */
    private void loadNotSupportedDataTypes() {
        this.notSupportedDataTypes.add(InputStream.class);
        this.notSupportedDataTypes.add(SQLXML.class);
        this.notSupportedDataTypes.add(Reader.class);
        this.notSupportedDataTypes.add(StringReader.class);
    }

    /**
     * Gets mapper.
     *
     * @param <T>                the type parameter
     * @param type               the type
     * @param reactiveSqlSession the reactive sql session
     * @return the mapper
     */
    public <T> T getMapper(Class<T> type, ReactiveSqlSession reactiveSqlSession) {
        return r2dbcMapperRegistry.getMapper(type, reactiveSqlSession);
    }

    /**
     * Gets connection factory.
     *
     * @return the connection factory
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Sets connection factory.
     *
     * @param connectionFactory the connection factory
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        this.r2dbcMapperRegistry.addMapper(type);
    }

    @Override
    public void addMappers(String packageName, Class<?> superType) {
        this.r2dbcMapperRegistry.addMappers(packageName, superType);
    }

    @Override
    public void addMappers(String packageName) {
        this.r2dbcMapperRegistry.addMappers(packageName);
    }

    @Override
    public boolean hasMapper(Class<?> type) {
        return this.r2dbcMapperRegistry.hasMapper(type);
    }

    @Override
    public void addMappedStatement(MappedStatement ms) {
        super.addMappedStatement(ms);
        this.r2dbcStatementLogFactory.initR2dbcStatementLog(ms);
    }

    /**
     * Add R2dbc type handler adapter.
     *
     * @param r2dbcTypeHandlerAdapter the R2dbc type handler adapter
     */
    public void addR2dbcTypeHandlerAdapter(R2dbcTypeHandlerAdapter<?> r2dbcTypeHandlerAdapter) {
        this.r2dbcTypeHandlerAdapterRegistry.register(r2dbcTypeHandlerAdapter);
    }

    /**
     * Add R2dbc type handler adapter.
     *
     * @param packageName the package name
     */
    public void addR2dbcTypeHandlerAdapter(String packageName) {
        this.r2dbcTypeHandlerAdapterRegistry.register(packageName);
    }

    /**
     * Gets R2dbc type handler adapter registry.
     *
     * @return the R2dbc type handler adapter registry
     */
    public R2dbcTypeHandlerAdapterRegistry getR2dbcTypeHandlerAdapterRegistry() {
        return r2dbcTypeHandlerAdapterRegistry;
    }

    /**
     * Sets not supported jdbc type.
     *
     * @param clazz the clazz
     */
    public void setNotSupportedJdbcType(Class<?> clazz) {
        this.notSupportedDataTypes.add(clazz);
    }

    /**
     * Gets not supported data types.
     *
     * @return the not supported data types
     */
    public Set<Class<?>> getNotSupportedDataTypes() {
        return this.notSupportedDataTypes;
    }

    /**
     * get r2dbc statement log
     *
     * @param mappedStatement target MappedStatement
     * @return the R2dbcStatementLog
     */
    public R2dbcStatementLog getR2dbcStatementLog(MappedStatement mappedStatement) {
        return this.r2dbcStatementLogFactory.getR2dbcStatementLog(mappedStatement);
    }

    /**
     * get r2dbc statement log factory
     *
     * @return the R2dbcStatementLogFactory
     */
    public R2dbcStatementLogFactory getR2dbcStatementLogFactory() {
        return r2dbcStatementLogFactory;
    }

    /**
     * Add placeholder dialect.
     *
     * @param placeholderDialect the placeholder dialect
     */
    public void addPlaceholderDialect(PlaceholderDialect placeholderDialect){
        this.placeholderDialectRegistry.register(placeholderDialect);
    }

    /**
     * Get placeholder dialect registry
     *
     * @return the placeholder dialect registry
     */
    public PlaceholderDialectRegistry getPlaceholderDialectRegistry(){
        return this.placeholderDialectRegistry;
    }

    /**
     * get formatted dialect sql cache max size
     *
     * @return cache max size
     */
    public Integer getFormattedDialectSqlCacheMaxSize() {
        return formattedDialectSqlCacheMaxSize;
    }

    /**
     * Sets formatted dialect sql cache max size.
     *
     * @param formattedDialectSqlCacheMaxSize the formatted dialect sql cache max size
     */
    public void setFormattedDialectSqlCacheMaxSize(Integer formattedDialectSqlCacheMaxSize) {
        if(formattedDialectSqlCacheMaxSize < 1){
            throw new IllegalArgumentException("Formatted dialect sql cache's max size must greater than 0");
        }
        this.formattedDialectSqlCacheMaxSize = formattedDialectSqlCacheMaxSize;
    }

    /**
     * get formatted dialect sql timeout duration
     *
     * @return cache expire duration
     */
    public Duration getFormattedDialectSqlCacheExpireDuration() {
        return formattedDialectSqlCacheExpireDuration;
    }

    /**
     * Sets formatted dialect sql cache expire duration.
     *
     * @param formattedDialectSqlCacheExpireDuration the formatted dialect sql expire duration
     */
    public void setFormattedDialectSqlCacheExpireDuration(Duration formattedDialectSqlCacheExpireDuration) {
        if(formattedDialectSqlCacheExpireDuration.isNegative() || formattedDialectSqlCacheExpireDuration.isZero()){
            throw new IllegalArgumentException("Formatted dialect sql cache's expire duration must greater than 0");
        }
        this.formattedDialectSqlCacheExpireDuration = formattedDialectSqlCacheExpireDuration;
    }
}
