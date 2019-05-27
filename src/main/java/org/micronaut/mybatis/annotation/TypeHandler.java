package org.micronaut.mybatis.annotation;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Scope
@Singleton
public @interface TypeHandler {}
