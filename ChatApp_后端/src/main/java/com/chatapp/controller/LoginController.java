package com.chatapp.controller;

import com.chatapp.pojo.Message;
import com.chatapp.service.MessageService;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.pojo.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        User existingUser = userService.findByUsername(user.getUsername());
        if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            userService.setOnline(user.getUsername());
            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
    @PostMapping("/register")
    public ResponseEntity<String>register(@RequestBody User user){
        User testUser=userService.findByUsername(user.getUsername());
        if(testUser!=null){
            return new ResponseEntity<>("User has been register!", HttpStatus.BAD_REQUEST);
        }else{
            userService.insertUser(user);
            return ResponseEntity.ok("Register successful!");
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<String>logout(@RequestParam String username){
        userService.setOffline(username);
        return ResponseEntity.ok("log out successful!");
    }
    @GetMapping("/fetchMessages")
    public ResponseEntity<String> fetchMessages(@RequestParam String username) {
        List<Message>messages= messageService.selectLoginUserMessage(username);
        if (messages != null) {
           messageService.forwardMessageToReceiver(username,messages);
        }
        return ResponseEntity.ok("信息接收成功");  // 没有未读消息时返回空列表
    }
}

