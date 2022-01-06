package pro.chenggang.project.reactive.mybatis.support.generator.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;
import pro.chenggang.project.reactive.mybatis.support.generator.option.LombokConfig;
import pro.chenggang.project.reactive.mybatis.support.generator.annotation.Required;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The type Generator extension properties.
 *
 * @author chenggang
 * @date 2020 -01-21.
 */
@Getter
@Setter
@ToString
public class GeneratorExtensionProperties {

    /**
     * properties file name(yaml)
     */
    public static final String PROPERTIES_FILE_NAME_YAML = "mybatis-generator.yaml";
    /**
     * properties file name(yml)
     */
    public static final String PROPERTIES_FILE_NAME_YML = "mybatis-generator.yml";

    /**
     * base package
     */
    private String basePackage;
    /**
     * java/xml parentLocation
     */
    private String parentLocation = "";
    /**
     * java location
     */
    @Required
    private String javaLocation = "src/main/java";
    /**
     * xml location
     */
    @Required
    private String mapperXmlLocation = "src/main/resources";
    /**
     * model location
     */
    @Required
    private String modelPackage = "entity.model";
    /**
     * mapper package
     */
    @Required
    private String mapperInterfacePackage = "mapper";
    /**
     * xml package
     */
    @Required
    private String mapperXmPackage = "mapper";
    /**
     * jdbc driver class name
     */
    @Required
    private String driverClass;
    /**
     * jdbc connection url
     */
    @Required
    private String connectionUrl;
    /**
     * jdbc username
     */
    @Required
    private String username;
    /**
     * jdbc password
     */
    @Required
    private String password;
    /**
     * target tableName
     */
    private Set<String> tableName;
    /**
     * table trim pattern
     */
    private String tableTrimPattern="";
    /**
     * lombok config
     */
    private LinkedHashSet<LombokConfig> lombok;
    /**
     * generator type
     */
    private Set<GeneratorType> generateType;
    /**
     * generate column comment
     */
    private boolean generateComment = true;
    /**
     * Simple Mapper extend Dynamic whether or not
     */
    private boolean extendDynamicMapper = false;
    /**
     * 是否覆盖
     */
    private boolean overwrite = true;

    /**
     * Validate by default.
     */
    public void validateByDefault(){
        Stream.of(this.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Required.class) && String.class.isAssignableFrom(field.getType()))
                .peek(field -> field.setAccessible(true))
                .forEach(field -> {
                    Object fieldValue = null;
                    try {
                        fieldValue = field.get(this);
                    } catch (IllegalAccessException e) {
                        //ignore
                    }
                    if(Objects.isNull(fieldValue)){
                        throw new IllegalArgumentException("Mybatis Generator Properties ( " + field.getName() + " ) Must Be Set");
                    }
                    String fieldStringValue = (String) fieldValue;
                    if(StringUtils.isEmpty(fieldStringValue)){
                        throw new IllegalArgumentException("Mybatis Generator Properties ( " + field.getName() + " ) Must Be Set");
                    }
                });
        if(StringUtils.isEmpty(this.basePackage)){
            throw new IllegalArgumentException("Mybatis Generator Properties ( basePackage ) Must Be Set");
        }
        if(Objects.isNull(tableName) || tableName.isEmpty()){
            tableName = new HashSet<>();
            tableName.add("%");
        }
        if(Objects.isNull(lombok)){
            lombok = new LinkedHashSet<>();
        }
        if(Objects.isNull(generateType) || generateType.isEmpty()){
            generateType = new HashSet<>();
            generateType.add(GeneratorType.SIMPLE);
            generateType.add(GeneratorType.DYNAMIC);
        }
        if(!"".equals(parentLocation) && !parentLocation.endsWith("/")){
            parentLocation = parentLocation + "/";
        }
    }

}
