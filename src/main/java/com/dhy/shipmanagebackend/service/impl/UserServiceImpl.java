package com.dhy.shipmanagebackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.mapper.UserMapper;
import com.dhy.shipmanagebackend.service.UserService;
import com.dhy.shipmanagebackend.utils.Md5Util;
import com.dhy.shipmanagebackend.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JavaMailSender mailSender; // Spring自动注入邮件发送器

    @Autowired
    private StringRedisTemplate stringRedisTemplate; // Redis工具

    // 读取配置文件中的发件人邮箱
    @Value("${resend.from-email}")
    private String fromEmail;

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
    public void sendCode(String email) {
        // 1. 生成 6 位随机验证码
        String code = RandomUtil.getSixBitRandom();

        // 2. 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // 必须和配置文件里的一致
        message.setTo(email);
        message.setSubject("【船舶管理系统】注册验证码");
        message.setText("您好，您的注册验证码是：" + code + "，有效期为5分钟，请勿泄露给他人。");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("邮件发送失败，请检查邮箱地址或稍后重试");
        }

        // 3. 存入 Redis，有效期 5 分钟
        // Key 的格式建议加前缀，例如 "register:code:邮箱"
        stringRedisTemplate.opsForValue().set("register:code:" + email, code, 5, TimeUnit.MINUTES);
    }

    @Override
    public void register(String username, String password,String email, String code) {
        // 1. 验证码校验
        String redisCode = stringRedisTemplate.opsForValue().get("register:code:" + email);
        if (!code.equals(redisCode)) {
            throw new RuntimeException("验证码错误或已失效");
        }

        // 2. 验证通过，手动删除 Redis 中的验证码（防止二次使用）
        stringRedisTemplate.delete("register:code:" + email);
        // 使用Md5Util 加密密码
        String md5Password = Md5Util.getMD5String(password);

        // 创建对象
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
