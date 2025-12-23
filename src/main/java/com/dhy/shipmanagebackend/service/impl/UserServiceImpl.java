package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.mapper.UserMapper;
import com.dhy.shipmanagebackend.service.MailService;
import com.dhy.shipmanagebackend.service.UserService;
import com.dhy.shipmanagebackend.utils.BcryptUtil;
import com.dhy.shipmanagebackend.utils.JwtUtil;
import com.dhy.shipmanagebackend.utils.RandomUtil;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger =
            Logger.getLogger(UserServiceImpl.class.getName());

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MailService mailService;

    @Value("${resend.from-email}")
    private String fromEmail;

    // ==================== 查询用户 ====================

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
    }

    // ==================== 发送邮箱验证码 ====================

    @Override
    public void sendCode(String email) {

        // 1️⃣ 防刷：60 秒内只能发一次
        String lockKey = "send:lock:" + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }

        // 2️⃣ 生成验证码
        String code = RandomUtil.getSixBitRandom();

        // 3️⃣ 存 Redis（5 分钟）
        stringRedisTemplate.opsForValue()
                .set("register:code:" + email, code, 5, TimeUnit.MINUTES);

        // 4️⃣ 防刷锁（60 秒）
        stringRedisTemplate.opsForValue()
                .set(lockKey, "1", 60, TimeUnit.SECONDS);

        // 5️⃣ 构建 HTML
        String html = buildMailHtml(code);

        // 6️⃣ 异步发送（不阻塞接口）
        mailService.send(email, html);


        logger.info("验证码已生成并发送任务已提交：" + email);
    }

    // ==================== 异步发邮件 ====================

    @Async
    public void sendMailAsync(String email, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "船舶管理系统官方");
            helper.setTo(email);
            helper.setSubject("【船舶管理系统】登录验证码");
            helper.setText(html, true);

            // 内嵌 Logo
            helper.addInline(
                    "logo",
                    new ClassPathResource("static/logo.png")
            );

            mailSender.send(message);

            logger.info("邮件发送成功：" + email);

        } catch (Exception e) {
            logger.warning("邮件发送失败：" + e.getMessage());
        }
        logger.info("当前线程：" + Thread.currentThread().getName());
    }

    // ==================== 注册 ====================

    @Override
    public void register(String username, String password,
                         String email, String code) {

        String redisCode =
                stringRedisTemplate.opsForValue()
                        .get("register:code:" + email);

        if (redisCode == null || !redisCode.equals(code)) {
            throw new RuntimeException("验证码错误或已失效");
        }

        // 删除验证码，防重放
        stringRedisTemplate.delete("register:code:" + email);

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(BcryptUtil.encode(password));
        user.setEmail(email);
        user.setRole("USER");

        userMapper.insert(user);
    }

    // ==================== 邮箱登录 ====================

    @Override
    public String loginByEmail(String email, String code) {

        String redisCode =
                stringRedisTemplate.opsForValue()
                        .get("register:code:" + email);

        if (redisCode == null || !redisCode.equals(code)) {
            throw new RuntimeException("验证码错误或已失效");
        }

        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("该邮箱尚未注册");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String token = JwtUtil.genToken(claims);

        stringRedisTemplate.opsForValue()
                .set(token, token, 12, TimeUnit.HOURS);

        stringRedisTemplate.delete("register:code:" + email);

        return token;
    }

    // ==================== 更新用户 ====================

    @Override
    public void update(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(String avatarUrl, String username) {
        userMapper.update(
                null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getUsername, username)
                        .set(User::getAvatarUrl, avatarUrl)
                        .set(User::getUpdatedAt, LocalDateTime.now())
        );
    }

    // ==================== 邮件 HTML ====================

    private String buildMailHtml(String code) {
        return "<div style=\"background:#f2f4f7;padding:56px 16px;" +
                "font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Inter,Roboto,Helvetica,Arial,sans-serif;\">" +

                "<div style=\"max-width:460px;margin:0 auto;background:#ffffff;" +
                "border-radius:16px;box-shadow:0 20px 40px rgba(0,0,0,0.08);overflow:hidden;\">" +

                // ===== Header =====
                "<div style=\"padding:40px 32px 28px;text-align:center;\">" +

                // Logo
                "<div style=\"margin-bottom:16px;\">" +
                "<img src=\"cid:logo\" alt=\"Ship Management\" " +
                "style=\"height:64px;display:block;margin:0 auto;\" />" +
                "</div>" +

                "<div style=\"font-size:13px;letter-spacing:1.6px;" +
                "color:#64748b;margin-bottom:10px;\">" +
                "SHIP MANAGEMENT SYSTEM" +
                "</div>" +

                "<h1 style=\"margin:0;font-size:22px;font-weight:600;" +
                "color:#0f172a;\">" +
                "身份验证" +
                "</h1>" +
                "</div>" +

                // ===== Content =====
                "<div style=\"padding:0 32px 40px;\">" +

                "<p style=\"margin:24px 0 16px;font-size:15px;" +
                "color:#334155;line-height:1.8;\">" +
                "您好，" +
                "</p>" +

                "<p style=\"margin:0 0 32px;font-size:15px;" +
                "color:#334155;line-height:1.8;\">" +
                "我们收到了您的登录或注册请求。<br/>" +
                "请输入以下验证码以继续操作：" +
                "</p>" +

                // ===== Code =====
                "<div style=\"text-align:center;margin:40px 0 36px;\">" +
                "<div style=\"display:inline-block;padding:18px 32px;" +
                "border-radius:14px;background:linear-gradient(180deg,#f8fafc,#eef2f7);\">" +
                "<span style=\"font-size:40px;font-weight:700;" +
                "letter-spacing:10px;color:#1e3a8a;" +
                "font-family:ui-monospace,Menlo,Monaco,Consolas,monospace;\">" +
                code +
                "</span>" +
                "</div>" +
                "</div>" +

                "<p style=\"margin:0;font-size:13px;color:#64748b;line-height:1.7;\">" +
                "验证码有效期为 <strong>5 分钟</strong>。<br/>" +
                "如果您未发起此操作，请忽略本邮件。" +
                "</p>" +
                "</div>" +

                // ===== Footer =====
                "<div style=\"padding:24px;text-align:center;background:#f8fafc;" +
                "font-size:12px;color:#94a3b8;\">" +
                "本邮件由系统自动发送，请勿回复<br/>" +
                "© 2025 Ship Management System" +
                "</div>" +

                "</div>" +
                "</div>";
    }
}
