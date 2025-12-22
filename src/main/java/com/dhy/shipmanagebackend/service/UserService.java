package com.dhy.shipmanagebackend.service;


import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.User;
import org.hibernate.validator.constraints.URL;
import org.springframework.stereotype.Service;


public interface UserService {
    User findByUsername(String username);
    // 修改：增加 code 参数
    void register(String username, String password, String email, String code);
    // 新增：发送验证码
    void sendCode(String email);
    // 更新用户
    void update(User user);
    //邮箱登录
    String loginByEmail(String email, String code);
    //修改头像
    void updateAvatar(String username, String avatarUrl);
}
