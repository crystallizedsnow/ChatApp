package com.chatapp.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String sender;//发送userName
    private String receiver;//接收userName
    private String content;//message内容
    private LocalDateTime timestamp;//发送时间
    private int isImage;//图片1，文字0
}

