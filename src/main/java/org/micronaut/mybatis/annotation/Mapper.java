package org.micronaut.mybatis.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Type;
import io.micronaut.retry.annotation.Recoverable;
import org.micronaut.mybatis.advice.MapperIntroductionAdvice;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Scope
@Introduction
@Type(MapperIntroductionAdvice.class)
@Recoverable
@Singleton
public @interface Mapper {}
