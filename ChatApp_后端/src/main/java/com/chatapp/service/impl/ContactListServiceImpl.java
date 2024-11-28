package com.chatapp.service.impl;

import com.chatapp.mapper.ContactListMapper;
import com.chatapp.pojo.User;
import com.chatapp.service.ContactListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactListServiceImpl implements ContactListService {
    @Autowired
    ContactListMapper contactListMapper;
    @Override
    public List<String> getcontactList(String username) {
        return contactListMapper.selectContactList(username);
    }

    @Override
    public void addFriend(String username,String friendname) {
        contactListMapper.insertFriend(username,friendname);
    }

    @Override
    public void deleteFriend(String username,String friendname) {
        contactListMapper.deleteFriend(username,friendname);
    }
}
