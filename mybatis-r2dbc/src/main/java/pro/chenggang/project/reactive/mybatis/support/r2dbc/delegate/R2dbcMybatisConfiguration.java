/*
 *    Copyright 2009-2024 the original author or authors.
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

import io.r2dbc.spi.Blob;
import io.r2dbc.spi.Clob;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialectRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.defaults.DefaultPlaceholderDialectRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLog;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLogFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapterRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.converter.MybatisTypeHandlerConverter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.support.ForceToUseR2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping.R2dbcEnvironment;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping.R2dbcVendorDatabaseIdProvider;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLXML;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type R2dbc mybatis configuration.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class R2dbcMybatisConfiguration extends Configuration {

    protected final R2dbcMapperRegistry r2dbcMapperRegistry = new R2dbcMapperRegistry(this);
    protected final R2dbcTypeHandlerAdapterRegistry r2dbcTypeHandlerAdapterRegistry = new R2dbcTypeHandlerAdapterRegistry(
            this);
    protected final R2dbcStatementLogFactory r2dbcStatementLogFactory = new R2dbcStatementLogFactory(this);
    protected final PlaceholderDialectRegistry placeholderDialectRegistry = new DefaultPlaceholderDialectRegistry();
    protected final Set<Class<?>> notSupportedDataTypes = new HashSet<>();
    protected final AtomicBoolean initializedFlag = new AtomicBoolean(false);
    protected Integer formattedDialectSqlCacheMaxSize = 10_000;
    protected Duration formattedDialectSqlCacheExpireDuration = Duration.ofHours(6);

    private R2dbcEnvironment r2dbcEnvironment;

    /**
     * Instantiates a new R2dbc mybatis configuration.
     */
    public R2dbcMybatisConfiguration() {
        this.loadNotSupportedDataTypes();
        this.registerInternalTypeAlias();
        this.registerForceToUseR2dbcTypeHandlerAdapter();
    }

    /**
     * Instantiates a new R2dbc mybatis configuration.
     *
     * @param r2dbcEnvironment the r2dbc environment
     */
    public R2dbcMybatisConfiguration(R2dbcEnvironment r2dbcEnvironment) {
        this();
        this.r2dbcEnvironment = r2dbcEnvironment;
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
     * register internal type alias
     */
    private void registerInternalTypeAlias() {
        typeAliasRegistry.registerAlias("R2DBC_VENDOR", R2dbcVendorDatabaseIdProvider.class);
    }

    /**
     * register ForceToUseR2dbcTypeHandlerAdapter
     */
    private void registerForceToUseR2dbcTypeHandlerAdapter() {
        typeHandlerRegistry.register(Blob.class, JdbcType.BLOB, ForceToUseR2dbcTypeHandlerAdapter.class);
        typeHandlerRegistry.register(Clob.class, JdbcType.CLOB, ForceToUseR2dbcTypeHandlerAdapter.class);
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
     * Gets r2dbc environment.
     *
     * @return the r2dbc environment
     */
    public R2dbcEnvironment getR2dbcEnvironment() {
        return r2dbcEnvironment;
    }

    /**
     * Sets r2dbc environment.
     *
     * @param r2dbcEnvironment the r2dbc environment
     */
    public void setR2dbcEnvironment(R2dbcEnvironment r2dbcEnvironment) {
        this.r2dbcEnvironment = r2dbcEnvironment;
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
     * Add mybatis type handler converter.
     *
     * @param mybatisTypeHandlerConverter the mybatis type handler converter
     */
    public void addMybatisTypeHandlerConverter(MybatisTypeHandlerConverter mybatisTypeHandlerConverter) {
        this.r2dbcTypeHandlerAdapterRegistry.register(mybatisTypeHandlerConverter);
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
    public void addPlaceholderDialect(PlaceholderDialect placeholderDialect) {
        this.placeholderDialectRegistry.register(placeholderDialect);
    }

    /**
     * Get placeholder dialect registry
     *
     * @return the placeholder dialect registry
     */
    public PlaceholderDialectRegistry getPlaceholderDialectRegistry() {
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
        if (formattedDialectSqlCacheMaxSize < 1) {
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
        if (formattedDialectSqlCacheExpireDuration.isNegative() || formattedDialectSqlCacheExpireDuration.isZero()) {
            throw new IllegalArgumentException("Formatted dialect sql cache's expire duration must greater than 0");
        }
        this.formattedDialectSqlCacheExpireDuration = formattedDialectSqlCacheExpireDuration;
    }

    /**
     * Convert mybatis type handler.
     *
     * @param mybatisTypeHandlerConverters the mybatis type handler converters
     */
    protected void convertMybatisTypeHandler(List<MybatisTypeHandlerConverter> mybatisTypeHandlerConverters) {
        if (Objects.isNull(mybatisTypeHandlerConverters) || mybatisTypeHandlerConverters.isEmpty()) {
            return;
        }
        Collection<TypeHandler<?>> typeHandlers = this.getTypeHandlerRegistry()
                .getTypeHandlers();
        for (TypeHandler<?> typeHandler : typeHandlers) {
            for (MybatisTypeHandlerConverter mybatisTypeHandlerConverter : mybatisTypeHandlerConverters) {
                if (Objects.isNull(mybatisTypeHandlerConverter)) {
                    continue;
                }
                if (mybatisTypeHandlerConverter.shouldConvert(typeHandler)) {
                    R2dbcTypeHandlerAdapter<?> transformedR2dbcTypeHandlerAdapter = mybatisTypeHandlerConverter.convert(
                            typeHandler);
                    this.addR2dbcTypeHandlerAdapter(transformedR2dbcTypeHandlerAdapter);
                }
            }
        }
    }

    /**
     * Initialize r2dbc configuration.
     */
    public void initialize() {
        if (null == this.r2dbcEnvironment) {
            throw new IllegalStateException("R2dbc environment can not be null, please check your configuration");
        }
        if (initializedFlag.compareAndSet(false, true)) {
            this.convertMybatisTypeHandler(this.getR2dbcTypeHandlerAdapterRegistry().getMybatisTypeHandlerConverters());
        }
    }
}
