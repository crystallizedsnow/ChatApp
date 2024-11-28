package com.chatapp.controller;

import com.aliyuncs.exceptions.ClientException;
import com.chatapp.pojo.Message;
import com.chatapp.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
@Slf4j
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private MessageService messageService;
    // 接收客户端发送到 "/app/chat" 的消息
    @MessageMapping("/chat")
    public void sendMessage(Message message) {
        // 获取接收人
        String receiver = message.getReceiver();
        // 将消息推送到指定用户的 "/queue/messages" 端点
        messagingTemplate.convertAndSendToUser(receiver, "/queue/messages", message);
    }
    // 服务器：处理确认消息，并删除消息
    @MessageMapping("/topic/acknowledgment")
    public void handleAcknowledgment(@Payload Map<String, String> payload) throws ClientException {
        String confirmReceiver = payload.get("username");
        log.info("Received acknowledgment for username: " + confirmReceiver);

        // 删除数据库中的消息记录
        messageService.deleteByReceiver(confirmReceiver);
//        // 可选：发送确认消息回客户端（比如消息已删除）
//        messagingTemplate.convertAndSend("/topic/user/" + receiver + "/messageDeleted", "Message deleted successfully");
    }
}

