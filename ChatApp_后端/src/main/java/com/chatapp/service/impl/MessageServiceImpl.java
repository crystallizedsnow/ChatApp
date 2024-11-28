package com.chatapp.service.impl;

import com.aliyuncs.exceptions.ClientException;
import com.chatapp.mapper.MessageMapper;
import com.chatapp.pojo.Message;
import com.chatapp.service.MessageService;
import com.chatapp.utils.AliOssUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    AliOssUtils aliOssUtils;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Override
    public void deleteByReceiver(String username) throws ClientException {
        List<Message>messages= messageMapper.findByReceiver(username);
        for(Message message:messages){
            if(message.getIsImage()!=0){
                aliOssUtils.deleteImg(message.getContent());
            }
        }
        messageMapper.deleteByReceiver(username);
    }

    @Override
    public List<Message> findByReceiverSender(String username,String sender) {
        return messageMapper.findByReceiverSender(username,sender);
    }

    @Override
    public void save(Message message) {
        messageMapper.save(message);
    }


    public void forwardMessageToReceiver(String receiver, List<Message> messages) {
        log.info("发送信息{}",messages.toString());
        messagingTemplate.convertAndSend ("/topic/user/"+receiver+"/sendToUser",
                messages);
//        messagingTemplate.convertAndSendToUser(receiver, "/queue/messages", messages);
    }


    @Override
    public List<Message> selectLoginUserMessage(String username) {
        return messageMapper.selectLoginUserMessage(username);
    }

}
