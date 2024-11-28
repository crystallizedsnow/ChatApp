package com.chatapp.controller;

import com.chatapp.pojo.User;
import com.chatapp.service.ContactListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contractList")
public class ContactListController {
    @Autowired
    private ContactListService contactListService;
    @GetMapping("/get")
    public ResponseEntity<List<String>> getcontactList(@RequestParam String username){
        List<String> contactList= contactListService.getcontactList(username);
        return ResponseEntity.ok(contactList);
    }
    @PostMapping("/add")
    public ResponseEntity<String> addFriend(@RequestParam String username,@RequestParam String friendname){
        contactListService.addFriend(username,friendname);
        return ResponseEntity.ok("Add successful!");
    }
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFriend(@RequestParam String username,@RequestParam String friendname){
        contactListService.deleteFriend(username,friendname);
        return ResponseEntity.ok("Delete successful");
    }
}
