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
package org.micronaut.mybatis.sample.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.micronaut.mybatis.annotation.Mapper;
import org.micronaut.mybatis.sample.domain.User;

import java.util.List;

@Mapper
public interface UserMapper {
  @Select("select * from users order by id")
  List<User> getUsers();

  @Select("select * from users where id=#{value}")
  User getUser(String userId);

  @Insert("insert into users VALUES (#{id},  #{name})")
  void insert(User user);

  default User insertAndSelect(User user) {
    insert(user);
    return getUser(user.getId());
  }

  @Insert("insert into users VALUES (#{id},  #{name})")
  User failingInsert(User user);
}
