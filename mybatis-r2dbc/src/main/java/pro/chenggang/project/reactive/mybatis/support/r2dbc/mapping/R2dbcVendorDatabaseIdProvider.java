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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Vendor DatabaseId provider.
 * <p>
 * It returns database product name as a databaseId. If the user provides a properties it uses it to translate database
 * product name key="Microsoft SQL Server", value="ms" will return "ms". It can return null, if no database product name
 * or a properties was specified and no translation was found.
 *
 * @author Eduardo Macarron
 * @author Gang Cheng
 * @see org.apache.ibatis.mapping.VendorDatabaseIdProvider
 */
public class R2dbcVendorDatabaseIdProvider implements R2dbcDatabaseIdProvider {

  private Properties properties;

  @Override
  public String getDatabaseId(ConnectionFactory connectionFactory) {
    if (connectionFactory == null) {
      throw new NullPointerException("dataSource cannot be null");
    }
    try {
      return getDatabaseName(connectionFactory);
    } catch (SQLException e) {
      throw new BuilderException("Error occurred when getting DB product name.", e);
    }
  }

  @Override
  public void setProperties(Properties p) {
    this.properties = p;
  }

  protected String getDatabaseName(ConnectionFactory connectionFactory) throws SQLException {
    String productName = connectionFactory.getMetadata().getName();
    if (this.properties != null) {
      for (Map.Entry<Object, Object> property : properties.entrySet()) {
        if (productName.contains((String) property.getKey())) {
          return (String) property.getValue();
        }
      }
      // no match, return null
      return null;
    }
    return productName;
  }

  private static class LogHolder {
    private static final Log log = LogFactory.getLog(R2dbcVendorDatabaseIdProvider.class);
  }

}