MyBatis Micronaut Adapter
======================

[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

![mybatis-spring](http://mybatis.github.io/images/mybatis-logo.png)

Micronaut-MyBatis adapter is an easy-to-use Micronaut bridge for MyBatis sql mapping framework.

Disclaimer
----------

This is a very basic version, which has a lot to cover. Right now only Java api is supported. See tests for examples.


Essentials
----------

Declare your regular mybatis mapper, like this:
```
@Mapper
public interface UserMapper {
  @Select("select * from users order by id")
  List<User> getUsers();

  @Select("select * from users where id=#{value}")
  User getUser(String userId);

  @Insert("insert into users VALUES (#{id},  #{name})")
  void insert(User user);
}
```

Notice the `@Mapper` annotation - it declares your mapper as a `@Singleton` so that you able to inject this later on in your service:

```
@Singleton
public class FooService {
  private final UserMapper userMapper;

  public FooService(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  public User doSomeBusinessStuff(String userId) {
    return this.userMapper.getUser(userId);
  }
}
```

Thats it! Micronaut-mybatis will take care of instantiating SqlSessionFactory, creating a new session and using it to call your mapper.

Every mapper call is made in a single transaction, so, if you want to make multiple calls withing transaction, just declare a default method in your mapper:

```
  default User insertAndSelect(User user) {
    insert(user);
    return getUser(user.getId());
  }
```