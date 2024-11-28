package com.chatapp.controller;

import com.aliyuncs.exceptions.ClientException;
import com.chatapp.pojo.Message;
import com.chatapp.service.MessageService;
import com.chatapp.utils.AliOssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final AliOssUtils aliOssUtils;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    // 上传图片并保存消息
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadImage(@RequestParam("image") MultipartFile image,
                              @RequestParam("sender") String sender,
                              @RequestParam("receiver") String receiver,
                              @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp
    ) throws IOException, ClientException {
        log.info("上传图片: {}", image.getOriginalFilename());

        // 上传图片到阿里云 OSS
        String imageUrl = aliOssUtils.upload(image);

        // 构造消息对象
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(imageUrl);
        message.setTimestamp(timestamp);
        message.setIsImage(1);
        // 判断接收者是否在线，实时推送或存储离线消息
        if (isUserOnline(receiver)) {
            List<Message>messages=new ArrayList<>();
            messages.add(message);
           messageService.forwardMessageToReceiver(message.getReceiver(),messages);
        }
        else {
            messageService.save(message);
        }



        return "Image uploaded and message sent successfully!";
    }

    private boolean isUserOnline(String username) {
        // 这里根据具体实现判断用户在线状态
        return true; // 示例：假设用户在线
    }
}

