package com.chatapp.service;

import com.chatapp.pojo.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ContactListService {
    List<String> getcontactList(String username);

    void addFriend(String username,String friendname);

    void deleteFriend(String username,String friendname);
}
