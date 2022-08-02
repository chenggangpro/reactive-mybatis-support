package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.annotation;

import org.mybatis.spring.annotation.MapperScannerRegistrar;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.R2dbcMapperFactoryBean;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.R2dbcMapperScannerConfigurer;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to register MyBatis mapper interfaces when using Java Config. It performs when same work as
 * {@link R2dbcMapperScannerConfigurer} via {@link MapperScannerRegistrar}.
 *
 * <p>
 * Either {@link #basePackageClasses} or {@link #basePackages} (or its alias {@link #value}) may be specified to define
 * specific packages to scan. Since 2.0.4, If specific packages are not defined, scanning will occur from the package of
 * the class that declares this annotation.
 *
 * copy from original MapperScan
 *
 * @author Michael Lanyon
 * @author Eduardo Macarron
 *
 * @since 1.2.0
 * @see R2dbcMapperScannerRegistrar
 * @see R2dbcMapperFactoryBean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(R2dbcMapperScannerRegistrar.class)
@Repeatable(R2dbcMapperScans.class)
public @interface R2dbcMapperScan {

  /**
   * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
   * {@code @MapperScan("org.my.pkg")} instead of {@code @MapperScan(basePackages = "org.my.pkg"})}.
   *
   * @return base package names
   */
  @AliasFor("basePackages")
  String[] value() default {};

  /**
   * Base packages to scan for MyBatis interfaces. Note that only interfaces with at least one method will be
   * registered; concrete classes will be ignored.
   *
   * @return base package names for scanning mapper interface
   */
  @AliasFor("value")
  String[] basePackages() default {};

  /**
   * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
   * package of each class specified will be scanned.
   * <p>
   * Consider creating a special no-op marker class or interface in each package that serves no purpose other than being
   * referenced by this attribute.
   *
   * @return classes that indicate base package for scanning mapper interface
   */
  Class<?>[] basePackageClasses() default {};

  /**
   * The {@link BeanNameGenerator} class to be used for naming detected components within the Spring container.
   *
   * @return the class of {@link BeanNameGenerator}
   */
  Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

  /**
   * This property specifies the annotation that the scanner will search for.
   * <p>
   * The scanner will register all interfaces in the base package that also have the specified annotation.
   * <p>
   * Note this can be combined with markerInterface.
   *
   * @return the annotation that the scanner will search for
   */
  Class<? extends Annotation> annotationClass() default Annotation.class;

  /**
   * This property specifies the parent that the scanner will search for.
   * <p>
   * The scanner will register all interfaces in the base package that also have the specified interface class as a
   * parent.
   * <p>
   * Note this can be combined with annotationClass.
   *
   * @return the parent that the scanner will search for
   */
  Class<?> markerInterface() default Class.class;

  /**
   * Specifies which {@code ReactiveSqlSessionFactory} to use in the case that there is more than one in the spring context.
   * Usually this is only needed when you have more than one datasource.
   *
   * @return the bean name of {@code ReactiveSqlSessionFactory}
   */
  String sqlSessionFactoryRef() default "";

  /**
   * Specifies a custom R2dbcMapperFactoryBean to return a mybatis proxy as spring bean.
   *
   * @return the class of {@code R2dbcMapperFactoryBean}
   */
  Class<? extends R2dbcMapperFactoryBean> factoryBean() default R2dbcMapperFactoryBean.class;

  /**
   * Whether enable lazy initialization of mapper bean.
   *
   * <p>
   * Default is {@code false}.
   * </p>
   *
   * @return set {@code true} to enable lazy initialization
   * @since 2.0.2
   */
  String lazyInitialization() default "";

  /**
   * Specifies the default scope of scanned mappers.
   *
   * <p>
   * Default is {@code ""} (equiv to singleton).
   * </p>
   *
   * @return the default scope
   */
  String defaultScope() default AbstractBeanDefinition.SCOPE_DEFAULT;

}
