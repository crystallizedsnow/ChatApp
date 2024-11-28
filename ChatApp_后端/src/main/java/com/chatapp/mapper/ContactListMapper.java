package com.chatapp.mapper;

import com.chatapp.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ContactListMapper {
    @Select("SELECT friendname FROM friends WHERE username = #{username} " +
            "UNION " +
            "SELECT username FROM friends WHERE friendname = #{username}")
    List<String> selectContactList(String username);

    @Insert("INSERT INTO friends(username, friendname) VALUES (#{username},#{friendname})")
    void insertFriend(@Param("username") String username, @Param("friendname") String friendname);
    @Delete("DELETE FROM friends " +
            "WHERE (username = #{username} AND friendname = #{friendname}) OR " +
            "(username = #{friendname} AND friendname = #{username})")
    void deleteFriend(@Param("username") String username, @Param("friendname") String friendname);

}
