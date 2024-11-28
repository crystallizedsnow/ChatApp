package com.chatapp.mapper;

import com.chatapp.pojo.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MessageMapper {
    @Insert("insert into message(sender,receiver,content,timestamp,isImage) values " +
            "(#{sender},#{receiver},#{content},#{timestamp},#{isImage})")
    void save(Message message);
    @Delete("delete from message where receiver=#{username}")
    void deleteByReceiver(String username);
    @Select("select *from message where receiver=#{username} and sender=#{sender}")
    List<Message> findByReceiverSender(String username,String sender);
    @Select("select * from message where receiver=#{username}")
    List<Message>selectLoginUserMessage(String username);
    @Select("select * from message where receiver=#{receiver}")
    List<Message> findByReceiver(String receiver);
}
