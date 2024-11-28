// MessageController.java
package com.chatapp.controller;

import com.aliyuncs.exceptions.ClientException;
import com.chatapp.pojo.Message;
import com.chatapp.pojo.User;
import com.chatapp.service.MessageService;
import com.chatapp.service.UserService;
import com.chatapp.utils.AliOssUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private AliOssUtils aliOssUtils;

    @PostMapping("/send")
    public String sendMessage(@RequestBody Message message) {
        User receiver = userService.findByUsername(message.getReceiver());
        if (receiver != null && receiver.isOnline()) {
            // 接收人在线，立即转发
            // Logic to send message in real-time
            List<Message>messages=new ArrayList<>();
            messages.add(message);
            messageService.forwardMessageToReceiver(receiver.getUsername(), messages);
            return "Message delivered";
        } else {
            // 接收人不在线，保存消息
            messageService.save(message);
            return "Message saved";
        }
    }
}
