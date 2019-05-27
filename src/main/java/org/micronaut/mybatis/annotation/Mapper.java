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

/**
 * {@code Mapper} is an annotation that declares your mapper interface so that it is visible to the micronaut application.
 * The {@link MapperIntroductionAdvice} will take care of providing actual implementation of access to that mapper
 *
 * @author Viacheslav Blinov
 *
 */
@Documented
@Retention(RUNTIME)
@Scope
@Introduction
@Type(MapperIntroductionAdvice.class)
@Recoverable
@Singleton
public @interface Mapper {}
