package com.chatapp.service.impl;

import com.chatapp.mapper.UserMapper;
import com.chatapp.pojo.User;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public void insertUser(User user) {
        userMapper.insertUser(user);
    }

    @Override
    public void setOffline(String username) {
        userMapper.setOffline(username);
    }

    @Override
    public void setOnline(String username) {
        userMapper.setOnline(username);
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
}
