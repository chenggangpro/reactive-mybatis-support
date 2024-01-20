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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.builder;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import io.r2dbc.spi.ValidationDepth;
import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.type.JdbcType;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping.R2dbcDatabaseIdProvider;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping.R2dbcEnvironment;

import java.io.InputStream;
import java.io.Reader;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

/**
 * The type R2dbc xml config builder.
 *
 * @author Clinton Begin
 * @author Kazuki Shimizu
 * @author Gang Cheng
 */
public class R2dbcXMLConfigBuilder extends BaseBuilder {

    private final XPathParser parser;
    private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();
    private boolean parsed;
    private String environment;
    private final R2dbcMybatisConfiguration r2dbcMybatisConfiguration;

    /**
     * Instantiates a new R2dbc xml config builder.
     *
     * @param reader the reader
     */
    public R2dbcXMLConfigBuilder(Reader reader) {
        this(reader, null, null);
    }

    /**
     * Instantiates a new R2dbc xml config builder.
     *
     * @param reader      the reader
     * @param environment the environment
     */
    public R2dbcXMLConfigBuilder(Reader reader, String environment) {
        this(reader, environment, null);
    }

    /**
     * Instantiates a new R2dbc xml config builder.
     *
     * @param reader      the reader
     * @param environment the environment
     * @param props       the props
     */
    public R2dbcXMLConfigBuilder(Reader reader, String environment, Properties props) {
        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    /**
     * Instantiates a new R2dbc xml config builder.
     *
     * @param inputStream the input stream
     */
    public R2dbcXMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null, null);
    }

    /**
     * Instantiates a new R2dbc xml config builder.
     *
     * @param inputStream the input stream
     * @param environment the environment
     */
    public R2dbcXMLConfigBuilder(InputStream inputStream, String environment) {
        this(inputStream, environment, null);
    }

    /**
     * Instantiates a new R2dbc xml config builder.
     *
     * @param inputStream the input stream
     * @param environment the environment
     * @param props       the props
     */
    public R2dbcXMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    private R2dbcXMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        super(new R2dbcMybatisConfiguration());
        this.r2dbcMybatisConfiguration = (R2dbcMybatisConfiguration) this.configuration;
        ErrorContext.instance().resource("SQL Mapper Configuration");
        this.configuration.setVariables(props);
        this.parsed = false;
        this.environment = environment;
        this.parser = parser;
    }

    /**
     * Get r2dbc mybatis configuration .
     *
     * @return the r2dbc mybatis configuration
     */
    public R2dbcMybatisConfiguration getR2dbcMybatisConfiguration(){
        return this.r2dbcMybatisConfiguration;
    }

    /**
     * Parse R2dbc mybatis configuration.
     *
     * @return the R2dbc mybatis configuration
     */
    public R2dbcMybatisConfiguration parse() {
        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
        parseConfiguration(parser.evalNode("/configuration"));
        return (R2dbcMybatisConfiguration) configuration;
    }

    private void parseConfiguration(XNode root) {
        try {
            // issue #117 read properties first
            propertiesElement(root.evalNode("properties"));
            Properties settings = settingsAsProperties(root.evalNode("settings"));
            loadCustomVfs(settings);
            loadCustomLogImpl(settings);
            typeAliasesElement(root.evalNode("typeAliases"));
            pluginElement(root.evalNode("plugins"));
            objectFactoryElement(root.evalNode("objectFactory"));
            objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
            reflectorFactoryElement(root.evalNode("reflectorFactory"));
            settingsElement(settings);
            // read it after objectFactory and objectWrapperFactory issue #631
            environmentsElement(root.evalNode("environments"));
            databaseIdProviderElement(root.evalNode("databaseIdProvider"));
            typeHandlerElement(root.evalNode("typeHandlers"));
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    private Properties settingsAsProperties(XNode context) {
        if (context == null) {
            return new Properties();
        }
        Properties props = context.getChildrenAsProperties();
        // Check that all settings are known to the configuration class
        MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
        for (Object key : props.keySet()) {
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }
        return props;
    }

    private void loadCustomVfs(Properties props) throws ClassNotFoundException {
        String value = props.getProperty("vfsImpl");
        if (value != null) {
            String[] clazzes = value.split(",");
            for (String clazz : clazzes) {
                if (!clazz.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);
                    configuration.setVfsImpl(vfsImpl);
                }
            }
        }
    }

    private void loadCustomLogImpl(Properties props) {
        Class<? extends Log> logImpl = resolveClass(props.getProperty("logImpl"));
        configuration.setLogImpl(logImpl);
    }

    private void typeAliasesElement(XNode parent) {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                if ("package".equals(child.getName())) {
                    String typeAliasPackage = child.getStringAttribute("name");
                    configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
                } else {
                    String alias = child.getStringAttribute("alias");
                    String type = child.getStringAttribute("type");
                    try {
                        Class<?> clazz = Resources.classForName(type);
                        if (alias == null) {
                            typeAliasRegistry.registerAlias(clazz);
                        } else {
                            typeAliasRegistry.registerAlias(alias, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
                    }
                }
            }
        }
    }

    private void pluginElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                String interceptor = child.getStringAttribute("interceptor");
                Properties properties = child.getChildrenAsProperties();
                Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).getDeclaredConstructor()
                        .newInstance();
                interceptorInstance.setProperties(properties);
                configuration.addInterceptor(interceptorInstance);
            }
        }
    }

    private void objectFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties properties = context.getChildrenAsProperties();
            ObjectFactory factory = (ObjectFactory) resolveClass(type).getDeclaredConstructor().newInstance();
            factory.setProperties(properties);
            configuration.setObjectFactory(factory);
        }
    }

    private void objectWrapperFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            ObjectWrapperFactory factory = (ObjectWrapperFactory) resolveClass(type).getDeclaredConstructor()
                    .newInstance();
            configuration.setObjectWrapperFactory(factory);
        }
    }

    private void reflectorFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            ReflectorFactory factory = (ReflectorFactory) resolveClass(type).getDeclaredConstructor().newInstance();
            configuration.setReflectorFactory(factory);
        }
    }

    private void propertiesElement(XNode context) throws Exception {
        if (context != null) {
            Properties defaults = context.getChildrenAsProperties();
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");
            if (resource != null && url != null) {
                throw new BuilderException(
                        "The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
            }
            if (resource != null) {
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                defaults.putAll(Resources.getUrlAsProperties(url));
            }
            Properties vars = configuration.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
        }
    }

    private void settingsElement(Properties props) {
        configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior",
                "PARTIAL"
        )));
        configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty(
                "autoMappingUnknownColumnBehavior",
                "NONE"
        )));
        configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
        configuration.setProxyFactory((ProxyFactory) createInstance(props.getProperty("proxyFactory")));
        configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
        configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
        configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"),
                true
        ));
        configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
        configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
        configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
        configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
        configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));
        configuration.setDefaultResultSetType(resolveResultSetType(props.getProperty("defaultResultSetType")));
        configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
        configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
        configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
        configuration.setLazyLoadTriggerMethods(stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"),
                "equals,clone,hashCode,toString"
        ));
        configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
        configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
        configuration.setDefaultEnumTypeHandler(resolveClass(props.getProperty("defaultEnumTypeHandler")));
        configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
        configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
        configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"),
                false
        ));
        configuration.setLogPrefix(props.getProperty("logPrefix"));
        configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
        configuration.setShrinkWhitespacesInSql(booleanValueOf(props.getProperty("shrinkWhitespacesInSql"), false));
        configuration.setArgNameBasedConstructorAutoMapping(booleanValueOf(props.getProperty(
                "argNameBasedConstructorAutoMapping"), false));
        configuration.setDefaultSqlProviderType(resolveClass(props.getProperty("defaultSqlProviderType")));
        configuration.setNullableOnForEach(booleanValueOf(props.getProperty("nullableOnForEach"), false));
    }

    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (environment == null) {
                environment = context.getStringAttribute("default");
            }
            for (XNode child : context.getChildren()) {
                String id = child.getStringAttribute("id");
                if (isSpecifiedEnvironment(id)) {
                    XNode dataSourceNode = child.evalNode("dataSource");
                    ConnectionFactory connectionFactory = connectionFactoryElement(dataSourceNode);
                    boolean defaultTransactionProxy = Boolean.parseBoolean(dataSourceNode.getChildrenAsProperties()
                            .getProperty("@defaultTransactionProxy",
                                    Boolean.TRUE.toString()
                            ));
                    R2dbcEnvironment r2dbcEnvironment = new R2dbcEnvironment.Builder(id)
                            .connectionFactory(connectionFactory)
                            .withDefaultTransactionProxy(defaultTransactionProxy)
                            .build();
                    this.r2dbcMybatisConfiguration.setR2dbcEnvironment(r2dbcEnvironment);
                    break;
                }
            }
        }
    }

    private void databaseIdProviderElement(XNode context) throws Exception {
        R2dbcDatabaseIdProvider databaseIdProvider = null;
        if (context != null) {
            String type = context.getStringAttribute("type");
            // awful patch to keep backward compatibility
            if ("VENDOR".equals(type) || "DB_VENDOR".equals(type)) {
                type = "R2DBC_VENDOR";
            }
            Properties properties = context.getChildrenAsProperties();
            Object r2dbcDatabaseIdProvider = resolveClass(type).getDeclaredConstructor().newInstance();
            if (!(r2dbcDatabaseIdProvider instanceof R2dbcDatabaseIdProvider)) {
                throw new IllegalArgumentException("DatabaseIdProvider should be an instance of R2dbcDatabaseIdProvider");
            }
            databaseIdProvider = (R2dbcDatabaseIdProvider) r2dbcDatabaseIdProvider;
            databaseIdProvider.setProperties(properties);
        }
        if (this.r2dbcMybatisConfiguration.getR2dbcEnvironment() != null && databaseIdProvider != null) {
            String databaseId = databaseIdProvider.getDatabaseId(this.r2dbcMybatisConfiguration.getR2dbcEnvironment()
                    .getConnectionFactory()
            );
            configuration.setDatabaseId(databaseId);
        }
    }

    private ConnectionFactory connectionFactoryElement(XNode context) throws Exception {
        if (context != null) {
            Properties props = context.getChildrenAsProperties();
            ConnectionFactoryOptions.Builder optionsBuilder = ConnectionFactoryOptions.builder();
            ConnectionFactoryOptionsConfigurer connectionFactoryOptionsConfigurer = null;
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                if ("@configurer".equals(entry.getKey()) && entry.getValue() instanceof String) {
                    connectionFactoryOptionsConfigurer = (ConnectionFactoryOptionsConfigurer) resolveClass((String) entry.getValue())
                            .getDeclaredConstructor()
                            .newInstance();
                    continue;
                }
                if (entry.getKey() == null || entry.getKey().toString().startsWith("@")) {
                    //ignore any other properties witch start with '@'
                    continue;
                }
                if (entry.getKey().toString().startsWith("pool.")) {
                    //ignore connection pool properties witch start with 'pool.'
                    continue;
                }
                optionsBuilder.option(Option.valueOf(String.valueOf(entry.getKey())), entry.getValue());
            }
            if (null != connectionFactoryOptionsConfigurer) {
                connectionFactoryOptionsConfigurer.configure(optionsBuilder);
            }
            ConnectionFactory connectionFactory = ConnectionFactories.get(optionsBuilder.build());
            String type = context.getStringAttribute("type");
            if ("POOLED".equals(type)) {
                ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory);
                this.parsePropertiesTo(props, "pool.name", Function.identity()).ifPresent(builder::name);
                this.parsePropertiesTo(props, "pool.maxSize", Integer::parseInt).ifPresent(builder::maxSize);
                this.parsePropertiesTo(props, "pool.initialSize", Integer::parseInt).ifPresent(builder::initialSize);
                this.parsePropertiesTo(props, "pool.maxIdleTime", Duration::parse).ifPresent(builder::maxIdleTime);
                this.parsePropertiesTo(props, "pool.acquireRetry", Integer::parseInt).ifPresent(builder::acquireRetry);
                this.parsePropertiesTo(props, "pool.backgroundEvictionInterval", Duration::parse).ifPresent(builder::backgroundEvictionInterval);
                this.parsePropertiesTo(props, "pool.maxAcquireTime", Duration::parse).ifPresent(builder::maxAcquireTime);
                this.parsePropertiesTo(props, "pool.maxCreateConnectionTime", Duration::parse).ifPresent(builder::maxCreateConnectionTime);
                this.parsePropertiesTo(props, "pool.maxLifeTime", Duration::parse).ifPresent(builder::maxLifeTime);
                this.parsePropertiesTo(props, "pool.validationDepth", ValidationDepth::valueOf).ifPresent(builder::validationDepth);
                this.parsePropertiesTo(props, "pool.validationQuery", Function.identity()).ifPresent(builder::validationQuery);
                this.parsePropertiesTo(props, "pool.configurer",
                                value -> {
                                    try {
                                        Object poolConfigurationConfigurer = resolveClass(value).getDeclaredConstructor()
                                                .newInstance();
                                        if (!(poolConfigurationConfigurer instanceof ConnectionPoolConfigurationConfigurer)) {
                                            throw new IllegalArgumentException(
                                                    "ConnectionPoolConfigurationConfigurer should be an instance of ConnectionPoolConfigurationConfigurer");
                                        }
                                        return (ConnectionPoolConfigurationConfigurer) poolConfigurationConfigurer;
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                        .ifPresent(connectionPoolConfigurationConfigurer -> connectionPoolConfigurationConfigurer.configure(builder));
                return new ConnectionPool(builder.build());
            }
            return connectionFactory;
        }
        throw new BuilderException("Environment declaration requires a ConnectionFactory.");
    }

    private <T> Optional<T> parsePropertiesTo(Properties properties, String key, Function<String, T> parseFunction) {
        return Optional.ofNullable(properties.getProperty(key))
                .map(parseFunction);
    }

    private void typeHandlerElement(XNode parent) {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                if ("package".equals(child.getName())) {
                    String typeHandlerPackage = child.getStringAttribute("name");
                    typeHandlerRegistry.register(typeHandlerPackage);
                } else {
                    String javaTypeName = child.getStringAttribute("javaType");
                    String jdbcTypeName = child.getStringAttribute("jdbcType");
                    String handlerTypeName = child.getStringAttribute("handler");
                    Class<?> javaTypeClass = resolveClass(javaTypeName);
                    JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
                    Class<?> typeHandlerClass = resolveClass(handlerTypeName);
                    if (javaTypeClass != null) {
                        if (jdbcType == null) {
                            typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                        } else {
                            typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                        }
                    } else {
                        typeHandlerRegistry.register(typeHandlerClass);
                    }
                }
            }
        }
    }

    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                if ("package".equals(child.getName())) {
                    String mapperPackage = child.getStringAttribute("name");
                    configuration.addMappers(mapperPackage);
                } else {
                    String resource = child.getStringAttribute("resource");
                    String url = child.getStringAttribute("url");
                    String mapperClass = child.getStringAttribute("class");
                    if (resource != null && url == null && mapperClass == null) {
                        ErrorContext.instance().resource(resource);
                        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
                            R2dbcXMLMapperBuilder mapperParser = new R2dbcXMLMapperBuilder(inputStream,
                                    configuration,
                                    resource,
                                    configuration.getSqlFragments()
                            );
                            mapperParser.parse();
                        }
                    } else if (resource == null && url != null && mapperClass == null) {
                        ErrorContext.instance().resource(url);
                        try (InputStream inputStream = Resources.getUrlAsStream(url)) {
                            R2dbcXMLMapperBuilder mapperParser = new R2dbcXMLMapperBuilder(inputStream,
                                    configuration,
                                    url,
                                    configuration.getSqlFragments()
                            );
                            mapperParser.parse();
                        }
                    } else if (resource == null && url == null && mapperClass != null) {
                        Class<?> mapperInterface = Resources.classForName(mapperClass);
                        configuration.addMapper(mapperInterface);
                    } else {
                        throw new BuilderException(
                                "A mapper element may only specify a url, resource or class, but not more than one.");
                    }
                }
            }
        }
    }

    private boolean isSpecifiedEnvironment(String id) {
        if (environment == null) {
            throw new BuilderException("No environment specified.");
        }
        if (id == null) {
            throw new BuilderException("Environment requires an id attribute.");
        }
        return environment.equals(id);
    }

}
