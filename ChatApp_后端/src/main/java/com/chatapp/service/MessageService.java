package com.chatapp.service;

import com.aliyuncs.exceptions.ClientException;
import com.chatapp.pojo.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface MessageService {
    void save(Message message);

    void deleteByReceiver(String username) throws ClientException;

    List<Message> findByReceiverSender(String username,String sender);
    void forwardMessageToReceiver(String receiver, List<Message> messages);

    List<Message>selectLoginUserMessage(String username);

}
