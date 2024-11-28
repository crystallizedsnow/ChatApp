package com.chatapp.service;

import com.chatapp.pojo.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User findByUsername(String username);

    void insertUser(User user);

    void setOffline(String username);

    void setOnline(String username);
}
