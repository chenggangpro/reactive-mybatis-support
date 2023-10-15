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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support;

import org.apache.ibatis.mapping.MappedStatement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The type R2dbc statement log factory.
 *
 * @author Gang Cheng
 * @since 1.0.0
 */
public class R2dbcStatementLogFactory {

    private final Map<String,R2dbcStatementLog> r2dbcStatementLogContainer = new HashMap<>();
    private final R2dbcMybatisConfiguration configuration;

    /**
     * Instantiates a new r2dbc statement log factory.
     *
     * @param r2dbcMybatisConfiguration the r2dbc mybatis configuration
     */
    public R2dbcStatementLogFactory(R2dbcMybatisConfiguration r2dbcMybatisConfiguration) {
        this.configuration = r2dbcMybatisConfiguration;
    }

    /**
     * Init r2dbc statement log.
     *
     * @param mappedStatement the mapped statement
     */
    public void initR2dbcStatementLog(MappedStatement mappedStatement){
        String logId = mappedStatement.getId();
        if (configuration.getLogPrefix() != null) {
            logId = configuration.getLogPrefix() + mappedStatement.getId();
        }
        r2dbcStatementLogContainer.put(logId,new R2dbcStatementLog(mappedStatement.getStatementLog()));
    }

    /**
     * Get r2dbc statement log optional.
     *
     * @param mappedStatement the MappedStatement
     * @return the R2dbcStatementLog
     */
    public R2dbcStatementLog getR2dbcStatementLog(MappedStatement mappedStatement){
        String logId = mappedStatement.getId();
        if (configuration.getLogPrefix() != null) {
            logId = configuration.getLogPrefix() + mappedStatement.getId();
        }
        R2dbcStatementLog r2dbcStatementLog = r2dbcStatementLogContainer.get(logId);
        if(Objects.nonNull(r2dbcStatementLog)){
            return r2dbcStatementLog;
        }
        r2dbcStatementLog = new R2dbcStatementLog(mappedStatement.getStatementLog());
        this.r2dbcStatementLogContainer.put(logId,r2dbcStatementLog);
        return r2dbcStatementLog;
    }

    /**
     * get all r2dbc statement logs
     * @return unmodifiable {@code Map<String,R2dbcStatementLog>}
     */
    public Map<String,R2dbcStatementLog> getAllR2dbcStatementLog(){
        return Collections.unmodifiableMap(this.r2dbcStatementLogContainer);
    }

}
