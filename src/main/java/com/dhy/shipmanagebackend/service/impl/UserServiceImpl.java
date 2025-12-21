package com.dhy.shipmanagebackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.mapper.UserMapper;
import com.dhy.shipmanagebackend.service.UserService;
import com.dhy.shipmanagebackend.utils.BcryptUtil;
import com.dhy.shipmanagebackend.utils.JwtUtil;
import com.dhy.shipmanagebackend.utils.Md5Util;
import com.dhy.shipmanagebackend.utils.RandomUtil;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());
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
        String Codehtml = "<div style=\"background-color: #f6f8fa; padding: 20px; font-family: -apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;\">" +

                // 卡片容器：白底，微弱边框，圆角
                "<div style=\"max-width: 500px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e1e4e8; border-radius: 6px; overflow: hidden;\">" +

                // 头部：品牌色条或简单的标题
                "<div style=\"padding: 24px; border-bottom: 1px solid #e1e4e8; background-color: #ffffff;\">" +
                "<h1 style=\"margin: 0; font-size: 20px; color: #24292e; font-weight: 600;\">船舶管理系统</h1>" +
                "</div>" +

                // 内容区域
                "<div style=\"padding: 32px 24px;\">" +
                "<p style=\"margin: 0 0 20px; font-size: 16px; color: #24292e;\">您好，</p>" +
                "<p style=\"margin: 0 0 24px; font-size: 16px; color: #24292e; line-height: 1.6;\">" +
                "我们收到了您的登录/注册请求。请使用以下验证码完成身份验证：" +
                "</p>" +

                // 验证码区域：灰色背景盒，强调文字
                "<div style=\"background-color: #f6f8fa; padding: 16px; text-align: center; border-radius: 6px; margin-bottom: 24px;\">" +
                "<span style=\"font-size: 32px; font-family: monospace; font-weight: 700; letter-spacing: 6px; color: #0969da;\">" +
                code +
                "</span>" +
                "</div>" +

                // 提示信息
                "<p style=\"margin: 0; font-size: 14px; color: #57606a;\">" +
                "该验证码将在 <strong>5 分钟</strong> 后失效。如果这不是您的操作，请忽略此邮件。" +
                "</p>" +
                "</div>" +

                // 底部：弱化显示的版权信息
                "<div style=\"padding: 16px 24px; background-color: #f6f8fa; border-top: 1px solid #e1e4e8; font-size: 12px; color: #8c959f; text-align: center;\">" +
                "<p style=\"margin: 0;\">此邮件由系统自动发送，请勿回复。</p>" +
                "<p style=\"margin: 5px 0 0;\">© 2025 Ship Management System</p>" +
                "</div>" +

                "</div>" +
                "</div>";
        // 2. 发送邮件
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail, "船舶管理系统官方"); // 第二个参数是发件人昵称

            helper.setTo(email);
            helper.setSubject("【船舶管理系统】登录验证码");
            helper.setText(Codehtml, true);

            mailSender.send(message);

            logger.info("邮件发送成功！验证码为："+ code);

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
        // 使用Bcrypt 加密密码
        String BcryptPassword = BcryptUtil.encode(password);

        // 创建对象
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(BcryptPassword);
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

    @Override
    public String loginByEmail(String email, String code) {
        String redisCode = stringRedisTemplate.opsForValue().get("register:code:" + email);

        if (redisCode == null || !redisCode.equals(code)) {
            throw new RuntimeException("验证码错误或已失效");
        }

        User user = userMapper.findByEmail(email);
        if (user == null) {
            // 这里建议抛出异常或者自动注册，具体看你业务
            throw new RuntimeException("该邮箱尚未注册");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        String token = JwtUtil.genToken(claims);

        stringRedisTemplate.opsForValue().set(token, token, 12, TimeUnit.HOURS);
        stringRedisTemplate.delete("login:code:" + email);

        return token;
    }
}
