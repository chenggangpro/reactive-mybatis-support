package pro.chenggang.project.reactive.mybatis.support.generator.properties;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class YamlGeneratorPropertiesLoader implements GeneratorPropertiesLoader {

    private final Yaml yaml;
    private String classpathConfigurationFileName;
    private String customConfigurationFileName;

    public YamlGeneratorPropertiesLoader() {
        this(null);
    }

    public YamlGeneratorPropertiesLoader(String configurationFileName) {
        if (StringUtils.startsWith(configurationFileName, "classpath:")) {
            this.classpathConfigurationFileName = StringUtils.substringAfter(configurationFileName, "classpath:");
        } else if (StringUtils.isNotBlank(configurationFileName)) {
            this.customConfigurationFileName = configurationFileName;
        }
        Representer represent = new Representer();
        represent.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(new Constructor(GeneratorProperties.class), represent);
        yaml.setBeanAccess(BeanAccess.FIELD);
    }

    @Override
    public GeneratorProperties load() {
        InputStream configurationFileInputStream = this.getConfigurationFileInputStream();
        if (Objects.isNull(configurationFileInputStream)) {
            throw new IllegalArgumentException("Can not load configuration file with specific configuration");
        }
        return yaml.load(configurationFileInputStream);
    }

    @SneakyThrows
    private InputStream getConfigurationFileInputStream() {
        if (StringUtils.isNotBlank(this.customConfigurationFileName)) {
            return new FileInputStream(this.customConfigurationFileName);
        }
        if (StringUtils.isNotBlank(this.classpathConfigurationFileName)) {
            return this.getClass().getClassLoader().getResourceAsStream(customConfigurationFileName);
        }
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("mybatis-generator.yaml");
        if (Objects.nonNull(inputStream)) {
            return inputStream;
        }
        inputStream = this.getClass().getClassLoader().getResourceAsStream("mybatis-generator.yml");
        if (Objects.nonNull(inputStream)) {
            return inputStream;
        }
        return null;
    }

}
