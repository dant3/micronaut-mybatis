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

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.micronaut.mybatis.sample.domain.User;
import org.micronaut.mybatis.sample.service.FooService;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
public class ServiceTest extends SqlTest {
  @Inject private FooService fooService;

  @Test
  final void testFooService() {
    User user = this.fooService.doSomeBusinessStuff("u1");
    assertNotNull(user);
    assertEquals(user.getName(), "Pocoyo");
  }
}
