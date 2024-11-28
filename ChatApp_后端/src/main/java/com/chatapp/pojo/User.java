package com.chatapp.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;  // 用户名
    private String password;  // 密码
    private boolean online;   // 是否在线
}
