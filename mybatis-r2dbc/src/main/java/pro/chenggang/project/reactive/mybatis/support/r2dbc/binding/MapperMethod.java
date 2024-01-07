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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.binding;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import static org.apache.ibatis.mapping.SqlCommandType.FLUSH;

/**
 * The Mapper method.
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Lasse Voss
 * @author Kazuki Shimizu
 * @author Gang Cheng
 */
public class MapperMethod {

    private static final Log log = LogFactory.getLog(MapperMethod.class);

    private final SqlCommand command;
    private final MethodSignature method;

    /**
     * Instantiates a new Mapper method.
     *
     * @param mapperInterface the mapper interface
     * @param method          the method
     * @param config          the config
     */
    public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        this.command = new SqlCommand(config, mapperInterface, method);
        this.method = new MethodSignature(config, mapperInterface, method);
    }

    /**
     * Parse inferred class.
     *
     * @param genericType the generic type
     * @return the class
     */
    public static Class<?> parseInferredClass(Type genericType) {
        Class<?> inferredClass = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) genericType;
            Type[] typeArguments = type.getActualTypeArguments();
            if (typeArguments.length > 0) {
                final Type typeArgument = typeArguments[0];
                if (typeArgument instanceof ParameterizedType) {
                    inferredClass = (Class<?>) ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
                } else if (typeArgument instanceof Class) {
                    inferredClass = (Class<?>) typeArgument;
                } else {
                    String typeName = typeArgument.getTypeName();
                    if (typeName.contains(" ")) {
                        typeName = typeName.substring(typeName.lastIndexOf(" ") + 1);
                    }
                    if (typeName.contains("<")) {
                        typeName = typeName.substring(0, typeName.indexOf("<"));
                    }
                    try {
                        inferredClass = Class.forName(typeName);
                    } catch (Exception e) {
                        log.error("MapperMethod parse inferred class error ", e);
                    }
                }
            }
        }
        if (inferredClass == null && genericType instanceof Class) {
            inferredClass = (Class<?>) genericType;
        }
        return inferredClass;
    }

    /**
     * Execute object.
     *
     * @param sqlSession the sql session
     * @param args       the args
     * @return the object
     */
    public Object execute(ReactiveSqlSession sqlSession, Object[] args) {
        Object result;
        switch (command.getType()) {
            case INSERT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.insert(command.getName(), param));
                break;
            }
            case UPDATE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.update(command.getName(), param));
                break;
            }
            case DELETE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.delete(command.getName(), param));
                break;
            }
            case SELECT:
                if (method.returnsVoid()) {
                    result = executeWithVoid(sqlSession, args);
                } else if (method.returnsMany()) {
                    result = executeForMany(sqlSession, args);
                } else {
                    Object param = method.convertArgsToSqlCommandParam(args);
                    result = sqlSession.selectOne(command.getName(), param);
                }
                break;
            case FLUSH:
                throw new UnsupportedOperationException("Unsupported execution command : " + FLUSH);
            default:
                throw new BindingException("Unknown execution method for: " + command.getName());
        }
        if (result == null && method.getReturnType()
                .isPrimitive() && !method.returnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                    + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
        }
        return result;
    }

    private Object rowCountResult(Mono<Integer> rowCount) {
        if (method.returnsVoid()) {
            return rowCount.then();
        }
        if (Integer.class.equals(method.getReturnInferredType()) || Integer.TYPE.equals(method.getReturnInferredType())) {
            return rowCount.defaultIfEmpty(0);
        }
        if (Long.class.equals(method.getReturnInferredType()) || Long.TYPE.equals(method.getReturnInferredType())) {
            return rowCount.map(Long::valueOf).defaultIfEmpty(0L);
        }
        if (Boolean.class.equals(method.getReturnInferredType()) || Boolean.TYPE.equals(method.getReturnInferredType())) {
            return rowCount.map(value -> value > 0).defaultIfEmpty(false);
        }
        throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
    }

    private Mono<Void> executeWithVoid(ReactiveSqlSession sqlSession, Object[] args) {
        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
        if (void.class.equals(ms.getResultMaps().get(0).getType())) {
            throw new BindingException("method " + command.getName()
                    + " needs either a @ResultMap annotation, a @ResultType annotation,"
                    + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
        }
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            return sqlSession.selectList(command.getName(), param, rowBounds).then();
        }
        return sqlSession.selectList(command.getName(), param).then();
    }

    private <E> Flux<E> executeForMany(ReactiveSqlSession sqlSession, Object[] args) {
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            return sqlSession.selectList(command.getName(), param, rowBounds);
        }
        return sqlSession.selectList(command.getName(), param);
    }

    /**
     * The type Sql command.
     */
    public static class SqlCommand {

        private final String name;
        private final SqlCommandType type;

        /**
         * Instantiates a new Sql command.
         *
         * @param configuration   the configuration
         * @param mapperInterface the mapper interface
         * @param method          the method
         */
        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            final String methodName = method.getName();
            final Class<?> declaringClass = method.getDeclaringClass();
            MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
                    configuration
            );
            if (ms == null) {
                if (method.getAnnotation(Flush.class) != null) {
                    throw new UnsupportedOperationException("Unsupported execution command : " + FLUSH);
                }
                throw new BindingException("Invalid bound statement (not found): "
                        + mapperInterface.getName() + "." + methodName);
            } else {
                name = ms.getId();
                type = ms.getSqlCommandType();
                if (type == SqlCommandType.UNKNOWN) {
                    throw new BindingException("Unknown execution method for: " + name);
                }
            }
        }

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets type.
         *
         * @return the type
         */
        public SqlCommandType getType() {
            return type;
        }

        private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
                                                       Class<?> declaringClass, Configuration configuration) {
            String statementId = mapperInterface.getName() + "." + methodName;
            if (configuration.hasStatement(statementId)) {
                return configuration.getMappedStatement(statementId);
            }
            if (mapperInterface.equals(declaringClass)) {
                return null;
            }
            for (Class<?> superInterface : mapperInterface.getInterfaces()) {
                if (declaringClass.isAssignableFrom(superInterface)) {
                    MappedStatement ms = resolveMappedStatement(superInterface, methodName,
                            declaringClass, configuration
                    );
                    if (ms != null) {
                        return ms;
                    }
                }
            }
            return null;
        }
    }

    /**
     * The type Method signature.
     */
    public static class MethodSignature {

        private final boolean returnsMany;
        private final boolean returnsVoid;
        private final Class<?> returnType;
        private final Class<?> returnInferredType;
        private final Integer resultHandlerIndex;
        private final Integer rowBoundsIndex;
        private final ParamNameResolver paramNameResolver;

        /**
         * Instantiates a new Method signature.
         *
         * @param configuration   the configuration
         * @param mapperInterface the mapper interface
         * @param method          the method
         */
        public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
            Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
            if (resolvedReturnType instanceof Class<?>) {
                this.returnType = (Class<?>) resolvedReturnType;
            } else if (resolvedReturnType instanceof ParameterizedType) {
                this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
            } else {
                this.returnType = method.getReturnType();
            }
            this.returnInferredType = parseInferredClass(method.getGenericReturnType());
            this.returnsVoid = Void.class.equals(this.returnInferredType);
            this.returnsMany = Flux.class.equals(this.returnType);
            this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
            this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
            this.paramNameResolver = new ParamNameResolver(configuration, method);
            checkReactorType();
        }

        /**
         * check reactor type
         */
        private void checkReactorType() {
            if (Mono.class.equals(this.returnType)
                    && Collection.class.isAssignableFrom(this.returnInferredType)) {
                throw new UnsupportedOperationException(
                        "Return type assignable from Mono<Collection<T>> should be changed to Flux<T>");
            }
            if (void.class.equals(this.returnType)) {
                throw new UnsupportedOperationException(
                        "Return type is void should be changed to Mono<Void> or Flux<Void>");
            }
            if (!Mono.class.equals(this.returnType) && !Flux.class.equals(this.returnType)) {
                throw new UnsupportedOperationException("Return type should by either Mono or Flux");
            }
        }

        /**
         * Convert args to sql command param object.
         *
         * @param args the args
         * @return the object
         */
        public Object convertArgsToSqlCommandParam(Object[] args) {
            return paramNameResolver.getNamedParams(args);
        }

        /**
         * Has row bounds boolean.
         *
         * @return the boolean
         */
        public boolean hasRowBounds() {
            return rowBoundsIndex != null;
        }

        /**
         * Extract row bounds.
         *
         * @param args the args
         * @return the row bounds
         */
        public RowBounds extractRowBounds(Object[] args) {
            return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
        }

        /**
         * Has result handler boolean.
         *
         * @return the boolean
         */
        public boolean hasResultHandler() {
            return resultHandlerIndex != null;
        }

        /**
         * Extract result handler result handler.
         *
         * @param args the args
         * @return the result handler
         */
        public ResultHandler<?> extractResultHandler(Object[] args) {
            return hasResultHandler() ? (ResultHandler<?>) args[resultHandlerIndex] : null;
        }

        /**
         * Gets return type.
         *
         * @return the return type
         */
        public Class<?> getReturnType() {
            return returnType;
        }

        /**
         * Gets return inferred type.
         *
         * @return the return inferred type
         */
        public Class<?> getReturnInferredType() {
            return returnInferredType;
        }

        /**
         * Returns many boolean.
         *
         * @return the boolean
         */
        public boolean returnsMany() {
            return returnsMany;
        }

        /**
         * Returns void boolean.
         *
         * @return the boolean
         */
        public boolean returnsVoid() {
            return returnsVoid;
        }

        private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
            Integer index = null;
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (paramType.isAssignableFrom(argTypes[i])) {
                    if (index == null) {
                        index = i;
                    } else {
                        throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                    }
                }
            }
            return index;
        }
    }

}
