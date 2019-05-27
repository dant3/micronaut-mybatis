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
import org.micronaut.mybatis.sample.mapper.UserMapper;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
public class MapperTest extends SqlTest {
  @Inject private UserMapper userMapper;

  @Test
  final void testSelectMapper() throws Exception {
    User user = this.userMapper.getUser("u1");
    assertNotNull(user);
    assertEquals(user.getName(), "Pocoyo");
  }

  @Test
  final void testInsertThenSelectMapper() throws Exception {
    User newUser = new User();
    newUser.setId("u6");
    newUser.setName("Mario");

    userMapper.insert(newUser);
    User inserted = userMapper.getUser(newUser.getId());
    assertNotNull(inserted);
    assertEquals(newUser, inserted);
  }

  @Test
  final void testDefaultMethod() throws Exception {
    User newUser = new User();
    newUser.setId("u6");
    newUser.setName("Mario");

    User inserted = userMapper.insertAndSelect(newUser);
    assertNotNull(inserted);
    assertEquals(newUser, inserted);
  }
}
