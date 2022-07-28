package pro.chenggang.project.reactive.mybatis.support.generator.properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * The Generator Properties holder.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneratorPropertiesHolder {

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private GeneratorProperties generatorProperties;
    private GeneratorPropertiesLoader generatorPropertiesLoader;

    /**
     * Get instance .
     *
     * @return the generator properties holder
     */
    public static GeneratorPropertiesHolder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * get generator properties
     *
     * @return the generator properties
     */
    public GeneratorProperties getGeneratorProperties() {
        return this.getGeneratorPropertiesInternal()
                .orElseGet(this::loadGeneratorProperties);
    }

    /**
     * set generator properties loader
     *
     * @param generatorPropertiesLoader the GeneratorPropertiesLoader
     */
    public void setGeneratorPropertiesLoader(GeneratorPropertiesLoader generatorPropertiesLoader) {
        WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        try {
            this.generatorPropertiesLoader = generatorPropertiesLoader;
            this.generatorProperties = null;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * get generator properties internal
     *
     * @return the optional GeneratorProperties
     */
    private Optional<GeneratorProperties> getGeneratorPropertiesInternal() {
        ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        try {
            return Optional.ofNullable(generatorProperties);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Load generator properties internal
     */
    private GeneratorProperties loadGeneratorProperties() {
        WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        try {
            if (Objects.nonNull(this.generatorProperties)) {
                return this.generatorProperties;
            }
            if (Objects.isNull(this.generatorPropertiesLoader)) {
                throw new IllegalStateException("Generator properties loader is null, can not load properties");
            }
            this.generatorProperties = this.generatorPropertiesLoader.load();
            if (Objects.isNull(this.generatorProperties)) {
                throw new IllegalStateException("Generator properties is null after load from " + this.generatorPropertiesLoader.getClass().getName());
            }
            return this.generatorProperties;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * instance holder
     */
    private static class InstanceHolder {

        private static final GeneratorPropertiesHolder INSTANCE;

        static {
            INSTANCE = new GeneratorPropertiesHolder();
        }
    }
}
