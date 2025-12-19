package com.dhy.shipmanagebackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.mapper.UserMapper;
import com.dhy.shipmanagebackend.service.UserService;
import com.dhy.shipmanagebackend.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public User findByUsername(String username) {
        // 1. 创建条件构造器
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 2. 设定条件：User实体的 Username 字段 == 传入的 username 参数
        // MP 会自动把这个翻译成 SQL: WHERE username = ?
        wrapper.eq(User::getUsername, username);

        // 3. 调用通用的 selectOne 方法
        return userMapper.selectOne(wrapper);
    }

    @Override
    public void register(String username, String password,String email) {
        // 1. 使用你的 Md5Util 加密密码
        String md5Password = Md5Util.getMD5String(password);

        // 2. 创建对象
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(md5Password);
        user.setEmail(email);
        user.setRole("USER");

        // 3. 保存
        userMapper.insert(user);
    }

    @Override
    public void update(User user) {
        // 设置 updated_at
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);
    }
}
