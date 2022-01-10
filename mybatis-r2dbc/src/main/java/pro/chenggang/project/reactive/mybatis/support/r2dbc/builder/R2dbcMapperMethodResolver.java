package pro.chenggang.project.reactive.mybatis.support.r2dbc.builder;

import org.apache.ibatis.builder.annotation.MethodResolver;

import java.lang.reflect.Method;

/**
 * The type R2dbc mapper method resolver.
 *
 * @author Eduardo Macarron
 * @author Gang Cheng
 */
public class R2dbcMapperMethodResolver extends MethodResolver {

    private final R2dbcMapperAnnotationBuilder annotationBuilder;
    private final Method method;

    /**
     * Instantiates a new R2dbc mapper method resolver.
     *
     * @param annotationBuilder the annotation builder
     * @param method            the method
     */
    public R2dbcMapperMethodResolver(R2dbcMapperAnnotationBuilder annotationBuilder, Method method) {
        super(annotationBuilder, method);
        this.annotationBuilder = annotationBuilder;
        this.method = method;
    }


    @Override
    public void resolve() {
        annotationBuilder.parseStatement(method);
    }

}