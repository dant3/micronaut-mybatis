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
package org.micronaut.mybatis.advice;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.inject.ExecutableMethod;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.micronaut.mybatis.annotation.Mapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Singleton
public class MapperIntroductionAdvice implements MethodInterceptor<Object, Object> {
    private final SqlSessionFactory sqlSessionFactory;

    @Inject
    public MapperIntroductionAdvice(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        AnnotationValue<Mapper> clientAnnotation = context.findAnnotation(Mapper.class).orElseThrow(() ->
                new IllegalStateException("Mapper advice called from type that is not annotated with @Mapper: " + context)
        );

        boolean isSelect = context.findAnnotation(Select.class).isPresent();

        Class<?> mapperInterface = context.getDeclaringType();

        ExecutableMethod<Object, Object> methodCall = context.getExecutableMethod();
        Method method = methodCall.getTargetMethod();

        Object[] reflectionArguments = javaReflectionArguments(context);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            Object mapper = sqlSession.getMapper(mapperInterface);
            Object result = method.invoke(mapper, reflectionArguments);
            if (!isSelect) {
                sqlSession.commit();
            }
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private Object[] javaReflectionArguments(MethodInvocationContext<Object, Object> context) {
        return javaReflectionArguments(context.getArguments(), context.getParameters());
    }

    private Object[] javaReflectionArguments(Argument[] arguments, Map<String, MutableArgumentValue<?>> parameters) {
        Object[] result = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            Argument argument = arguments[i];
            String argumentName = argument.getName();
            MutableArgumentValue<?> value = parameters.get(argumentName);
            Object definedValue = value.getValue();
            result[i] = definedValue;
        }
        return result;
    }
}
