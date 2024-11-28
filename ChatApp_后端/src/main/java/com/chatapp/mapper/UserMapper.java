package com.chatapp.mapper;


import com.chatapp.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("select * from user where username=#{username}")
    User findByUsername(@Param("username")String username);
    @Insert("INSERT INTO user (username, password, online) " +
            "VALUES (#{username}, #{password}, TRUE)")
    void insertUser(User user);
    @Update("update user set online=FALSE where username=#{username}")
    void setOffline(String username);
    @Update("update user set online=TRUE where username=#{username}")
    void setOnline(String username);
}
