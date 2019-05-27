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
package org.micronaut.mybatis;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.io.scan.AnnotationScanner;
import io.micronaut.core.io.scan.ClassPathAnnotationScanner;
import io.micronaut.discovery.event.ServiceStartedEvent;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.micronaut.logging.Logger;
import org.micronaut.logging.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static io.micronaut.core.util.ArrayUtils.isEmpty;

/**
 * {@code FactoryBean} that creates an MyBatis {@code SqlSessionFactory}. This is the usual way to set up a shared
 * MyBatis {@code SqlSessionFactory} in a Micronaut application context; the SqlSessionFactory can then be passed to
 * MyBatis-based DAOs via dependency injection.
 *
 * @author Viacheslav Blinov
 *
 * @see #setDataSource
 */
@Factory
public class SqlSessionFactoryBean implements ApplicationEventListener<ServiceStartedEvent> {
  private static final Logger log = LoggerFactory.getLogger(SqlSessionFactoryBean.class);

  private final ApplicationContext applicationContext;

  private Configuration configuration;
  private DataSource dataSource;
  private TransactionFactory transactionFactory;
  private Properties configurationProperties;
  private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
  private SqlSessionFactory sqlSessionFactory;

  // EnvironmentAware requires mybatis 3.1
  private String environment = SqlSessionFactoryBean.class.getSimpleName();
  private boolean failFast;
  private Interceptor[] plugins;
  private TypeHandler<?>[] typeHandlers;
  private LanguageDriver[] scriptingLanguageDrivers;
  private Class<? extends LanguageDriver> defaultScriptingLanguageDriver;

  // issue #19. No default provider.
  private DatabaseIdProvider databaseIdProvider;
  private Class<? extends VFS> vfs;
  private Cache cache;
  private ObjectFactory objectFactory;
  private ObjectWrapperFactory objectWrapperFactory;

  @Inject
  public SqlSessionFactoryBean(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /**
   * Sets the ObjectFactory.
   *
   * @since 1.1.2
   * @param objectFactory
   *          a custom ObjectFactory
   */
  public void setObjectFactory(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  /**
   * Sets the ObjectWrapperFactory.
   *
   * @since 1.1.2
   * @param objectWrapperFactory
   *          a specified ObjectWrapperFactory
   */
  public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
    this.objectWrapperFactory = objectWrapperFactory;
  }

  /**
   * Gets the DatabaseIdProvider
   *
   * @since 1.1.0
   * @return a specified DatabaseIdProvider
   */
  public DatabaseIdProvider getDatabaseIdProvider() {
    return databaseIdProvider;
  }

  /**
   * Sets the DatabaseIdProvider. As of version 1.2.2 this variable is not initialized by default.
   *
   * @since 1.1.0
   * @param databaseIdProvider
   *          a DatabaseIdProvider
   */
  public void setDatabaseIdProvider(DatabaseIdProvider databaseIdProvider) {
    this.databaseIdProvider = databaseIdProvider;
  }

  /**
   * Gets the VFS.
   *
   * @return a specified VFS
   */
  public Class<? extends VFS> getVfs() {
    return this.vfs;
  }

  /**
   * Sets the VFS.
   *
   * @param vfs
   *          a VFS
   */
  public void setVfs(Class<? extends VFS> vfs) {
    this.vfs = vfs;
  }

  /**
   * Gets the Cache.
   *
   * @return a specified Cache
   */
  public Cache getCache() {
    return this.cache;
  }

  /**
   * Sets the Cache.
   *
   * @param cache
   *          a Cache
   */
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  /**
   * Mybatis plugin list.
   *
   * @since 1.0.1
   *
   * @param plugins
   *          list of plugins
   *
   */
  public void setPlugins(Interceptor... plugins) {
    this.plugins = plugins;
  }

  /**
   * Set type handlers. They must be annotated with {@code MappedTypes} and optionally with {@code MappedJdbcTypes}
   *
   * @since 1.0.1
   *
   * @param typeHandlers
   *          Type handler list
   */
  public void setTypeHandlers(TypeHandler<?>... typeHandlers) {
    this.typeHandlers = typeHandlers;
  }

  /**
   * If true, a final check is done on Configuration to assure that all mapped statements are fully loaded and there is
   * no one still pending to resolve includes. Defaults to false.
   *
   * @since 1.0.1
   *
   * @param failFast
   *          enable failFast
   */
  public void setFailFast(boolean failFast) {
    this.failFast = failFast;
  }

  /**
   * Set a customized MyBatis configuration.
   *
   * @param configuration
   *          MyBatis configuration
   * @since 1.3.0
   */
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Set optional properties to be passed into the SqlSession configuration, as alternative to a
   * {@code &lt;properties&gt;} tag in the configuration xml file. This will be used to resolve placeholders in the
   * config file.
   *
   * @param sqlSessionFactoryProperties
   *          optional properties for the SqlSessionFactory
   */
  public void setConfigurationProperties(Properties sqlSessionFactoryProperties) {
    this.configurationProperties = sqlSessionFactoryProperties;
  }

  /**
   * Set the JDBC {@code DataSource} that this instance should manage transactions for. The {@code DataSource} should
   * match the one used by the {@code SqlSessionFactory}: for example, you could specify the same JNDI DataSource for
   * both.
   *
   * A transactional JDBC {@code Connection} for this {@code DataSource} will be provided to application code accessing
   * this {@code DataSource} directly via {@code DataSourceUtils} or {@code DataSourceTransactionManager}.
   *
   * The {@code DataSource} specified here should be the target {@code DataSource} to manage transactions for, not a
   * {@code TransactionAwareDataSourceProxy}. Only data access code may work with
   * {@code TransactionAwareDataSourceProxy}, while the transaction manager needs to work on the underlying target
   * {@code DataSource}. If there's nevertheless a {@code TransactionAwareDataSourceProxy} passed in, it will be
   * unwrapped to extract its target {@code DataSource}.
   *
   * @param dataSource
   *          a JDBC {@code DataSource}
   *
   */
  @Inject
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Sets the {@code SqlSessionFactoryBuilder} to use when creating the {@code SqlSessionFactory}.
   *
   * This is mainly meant for testing so that mock SqlSessionFactory classes can be injected. By default,
   * {@code SqlSessionFactoryBuilder} creates {@code DefaultSqlSessionFactory} instances.
   *
   * @param sqlSessionFactoryBuilder
   *          a SqlSessionFactoryBuilder
   *
   */
  public void setSqlSessionFactoryBuilder(SqlSessionFactoryBuilder sqlSessionFactoryBuilder) {
    this.sqlSessionFactoryBuilder = sqlSessionFactoryBuilder;
  }

  /**
   * Set the MyBatis TransactionFactory to use. Default is {@code SpringManagedTransactionFactory}
   *
   * The default {@code SpringManagedTransactionFactory} should be appropriate for all cases: be it Spring transaction
   * management, EJB CMT or plain JTA. If there is no active transaction, SqlSession operations will execute SQL
   * statements non-transactionally.
   *
   * <b>It is strongly recommended to use the default {@code TransactionFactory}.</b> If not used, any attempt at
   * getting an SqlSession through Spring's MyBatis framework will throw an exception if a transaction is active.
   *
   * @param transactionFactory
   *          the MyBatis TransactionFactory
   */
  public void setTransactionFactory(TransactionFactory transactionFactory) {
    this.transactionFactory = transactionFactory;
  }

  /**
   * <b>NOTE:</b> This class <em>overrides</em> any {@code Environment} you have set in the MyBatis config file. This is
   * used only as a placeholder name. The default value is {@code SqlSessionFactoryBean.class.getSimpleName()}.
   *
   * @param environment
   *          the environment name
   */
  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  /**
   * Set scripting language drivers.
   *
   * @param scriptingLanguageDrivers
   *          scripting language drivers
   * @since 2.0.2
   */
  public void setScriptingLanguageDrivers(LanguageDriver... scriptingLanguageDrivers) {
    this.scriptingLanguageDrivers = scriptingLanguageDrivers;
  }

  /**
   * Set a default scripting language driver class.
   *
   * @param defaultScriptingLanguageDriver
   *          A default scripting language driver class
   * @since 2.0.2
   */
  public void setDefaultScriptingLanguageDriver(Class<? extends LanguageDriver> defaultScriptingLanguageDriver) {
    this.defaultScriptingLanguageDriver = defaultScriptingLanguageDriver;
  }

  @Override
  public void onApplicationEvent(final ServiceStartedEvent event) {
    createSqlSessionFactory();
  }

  private void createSqlSessionFactory() {
    this.sqlSessionFactory = buildSqlSessionFactory();
  }

  /**
   * Build a {@code SqlSessionFactory} instance.
   *
   * The default implementation uses the standard MyBatis {@code XMLConfigBuilder} API to build a
   * {@code SqlSessionFactory} instance based on an Reader. Since 1.3.0, it can be specified a {@link Configuration}
   * instance directly(without config file).
   *
   * @return SqlSessionFactory
   * @throws Exception
   *           if configuration is failed
   */
  protected SqlSessionFactory buildSqlSessionFactory() {
    final Configuration targetConfiguration;

    if (this.configuration != null) {
      targetConfiguration = this.configuration;
      if (targetConfiguration.getVariables() == null) {
        targetConfiguration.setVariables(this.configurationProperties);
      } else if (this.configurationProperties != null) {
        targetConfiguration.getVariables().putAll(this.configurationProperties);
      }
    } else {
      log.debug(() -> "Property 'configuration' or 'configLocation' not specified, using default MyBatis Configuration");
      targetConfiguration = new Configuration();
      Optional.ofNullable(this.configurationProperties).ifPresent(targetConfiguration::setVariables);
    }

    Optional.ofNullable(this.objectFactory).ifPresent(targetConfiguration::setObjectFactory);
    Optional.ofNullable(this.objectWrapperFactory).ifPresent(targetConfiguration::setObjectWrapperFactory);
    Optional.ofNullable(this.vfs).ifPresent(targetConfiguration::setVfsImpl);


    if (!isEmpty(this.plugins)) {
      Stream.of(this.plugins).forEach(plugin -> {
        targetConfiguration.addInterceptor(plugin);
        log.debug(() -> "Registered plugin: '" + plugin + "'");
      });
    }


    Stream<TypeHandler> contextTypeHandlers = applicationContext.streamOfType(TypeHandler.class);
    Stream<TypeHandler> setterTypeHandlers = isEmpty(this.typeHandlers) ? Stream.empty() : Stream.of(this.typeHandlers);

    Stream.concat(contextTypeHandlers, setterTypeHandlers)
            .forEach(typeHandler -> {
              //noinspection unchecked
              targetConfiguration.getTypeHandlerRegistry().register(typeHandler);
              log.debug(() -> "Registered type handler: '" + typeHandler + "'");
            });

    scanAnnotatedClasses(org.micronaut.mybatis.annotation.Mapper.class)
            .filter(Class::isInterface)
            .forEach(clazz -> {
              targetConfiguration.addMapper((Class<?>) clazz);
              log.debug(() -> "Registered mapper: '" + clazz.getCanonicalName() + "'");
            });

    if (!isEmpty(this.scriptingLanguageDrivers)) {
      Stream.of(this.scriptingLanguageDrivers).forEach(languageDriver -> {
        targetConfiguration.getLanguageRegistry().register(languageDriver);
        log.debug(() -> "Registered scripting language driver: '" + languageDriver + "'");
      });
    }
    Optional.ofNullable(this.defaultScriptingLanguageDriver)
            .ifPresent(targetConfiguration::setDefaultScriptingLanguage);

    Optional.ofNullable(this.cache).ifPresent(targetConfiguration::addCache);

    targetConfiguration.setEnvironment(new Environment(
            this.environment,
            this.transactionFactory == null ? new ManagedTransactionFactory() : this.transactionFactory,
            this.dataSource)
    );

    return this.sqlSessionFactoryBuilder.build(targetConfiguration);
  }

  @Singleton
  public SqlSessionFactory getObject() throws Exception {
    if (this.sqlSessionFactory == null) {
      createSqlSessionFactory();
    }

    return this.sqlSessionFactory;
  }


  private <A extends Annotation> Stream<Class> scanAnnotatedClasses(Class<A> annotationClass) {
    AnnotationScanner annotationScanner = new ClassPathAnnotationScanner(applicationContext.getEnvironment().getClassLoader());
    return annotationScanner.scan(annotationClass, applicationContext.getEnvironment().getPackages());
  }
}
