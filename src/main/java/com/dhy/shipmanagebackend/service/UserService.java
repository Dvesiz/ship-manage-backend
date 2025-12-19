package com.dhy.shipmanagebackend.service;


import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.User;
import org.springframework.stereotype.Service;


public interface UserService {
    User findByUsername(String username);
    // 注册
    void register(String username, String password , String email);
    // 更新用户
    void update(User user);
}
