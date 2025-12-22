package com.dhy.shipmanagebackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

import java.time.LocalDateTime;
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
        String Codehtml = "<div style=\"background:#f2f4f7;padding:56px 16px;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Inter,Roboto,Helvetica,Arial,sans-serif;\">" +

                "<div style=\"max-width:460px;margin:0 auto;background:#ffffff;border-radius:16px;box-shadow:0 20px 40px rgba(0,0,0,0.08);overflow:hidden;\">" +

                // Header
                "<div style=\"padding:36px 32px 24px;text-align:center;\">" +
                "<div style=\"font-size:13px;letter-spacing:1.5px;color:#6b7280;margin-bottom:12px;\">" +
                "SHIP MANAGEMENT SYSTEM" +
                "</div>" +
                "<h1 style=\"margin:0;font-size:22px;font-weight:600;color:#0f172a;\">" +
                "身份验证" +
                "</h1>" +
                "</div>" +

                // Content
                "<div style=\"padding:0 32px 36px;\">" +
                "<p style=\"margin:24px 0 16px;font-size:15px;color:#334155;line-height:1.8;\">" +
                "您好，" +
                "</p>" +

                "<p style=\"margin:0 0 32px;font-size:15px;color:#334155;line-height:1.8;\">" +
                "我们收到了您的登录或注册请求。<br/>" +
                "请输入以下验证码以继续操作：" +
                "</p>" +

                // Code (核心视觉)
                "<div style=\"text-align:center;margin:40px 0 36px;\">" +
                "<div style=\"display:inline-block;padding:18px 28px;border-radius:14px;" +
                "background:linear-gradient(180deg,#f8fafc,#eef2f7);\">" +
                "<span style=\"font-size:40px;font-weight:700;letter-spacing:10px;" +
                "color:#1e3a8a;font-family:ui-monospace,Menlo,Monaco,Consolas,monospace;\">" +
                code +
                "</span>" +
                "</div>" +
                "</div>" +

                "<p style=\"margin:0;font-size:13px;color:#64748b;line-height:1.7;\">" +
                "验证码有效期为 <strong>5 分钟</strong>。<br/>" +
                "如果您未发起此操作，请忽略本邮件。" +
                "</p>" +
                "</div>" +

                // Footer
                "<div style=\"padding:24px;text-align:center;background:#f8fafc;" +
                "font-size:12px;color:#94a3b8;\">" +
                "本邮件由系统自动发送，请勿回复<br/>" +
                "© 2025 Ship Management System" +
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

    @Override
    public void updateAvatar(String avatarUrl, String username) {
        // 创建更新条件
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(User::getUsername, username)      // WHERE username = ?
                .set(User::getAvatarUrl, avatarUrl)   // SET avatar_url = ?
                .set(User::getUpdatedAt, LocalDateTime.now()); // 同时更新时间

        // 执行更新
        userMapper.update(null, updateWrapper);
    }
}
