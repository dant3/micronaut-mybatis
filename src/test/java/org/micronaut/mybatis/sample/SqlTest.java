/**
 * Copyright 2010-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.micronaut.mybatis.sample;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@MicronautTest
abstract class SqlTest {
    @Inject private ApplicationContext context;
    @Inject private DataSource dataSource;


    @BeforeEach
    public void setUp() throws Exception {
        applySql(dataSource, "classpath:database-schema.sql");
        applySql(dataSource, "classpath:database-test-data.sql");
    }

    private static void applySql(DataSource dataSource, String sqlFile) throws Exception {
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class)
                .orElseThrow(IllegalStateException::new);
        InputStream file = loader.getResourceAsStream(sqlFile).orElseThrow(IllegalArgumentException::new);
        String sql = IOUtils.readText(new BufferedReader(new InputStreamReader(file)));
        dataSource.getConnection().prepareCall(sql).execute();
    }
}
