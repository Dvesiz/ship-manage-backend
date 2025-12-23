package com.dhy.shipmanagebackend.controller;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.entity.UserVO;
import com.dhy.shipmanagebackend.service.UserService;
import com.dhy.shipmanagebackend.utils.BcryptUtil;
import com.dhy.shipmanagebackend.utils.JwtUtil;
import com.dhy.shipmanagebackend.utils.Md5Util;
import com.dhy.shipmanagebackend.utils.ThreadLocalUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Email;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取图形验证码
     * Redis 优化版：将验证码存入 Redis，有效期 2 分钟
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        // 1. 生成线段干扰的验证码 (宽120, 高40, 4个字符, 20条干扰线)
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String code = lineCaptcha.getCode(); // 验证码文本
        String imageBase64 = lineCaptcha.getImageBase64(); // 图片 Base64

        // 2. 生成唯一标识 UUID
        String uuid = UUID.randomUUID().toString();

        // 3. 存入 Redis (Key: "captcha:UUID", Value: "AB12", 过期时间: 2分钟)
        String redisKey = "captcha:" + uuid;
        stringRedisTemplate.opsForValue().set(redisKey, code, 2, TimeUnit.MINUTES);

        // 4. 返回给前端 (前端需要 UUID 传回后端进行比对)
        Map<String, String> map = new HashMap<>();
        map.put("uuid", uuid);
        map.put("img", "data:image/png;base64," + imageBase64);

        return Result.success(map);
    }

    /**
     * 内部辅助方法：校验验证码
     */
    private void validateCaptcha(String captcha, String uuid) {
        if (!StringUtils.hasLength(captcha) || !StringUtils.hasLength(uuid)) {
            throw new RuntimeException("请输入验证码");
        }

        String redisKey = "captcha:" + uuid;
        String redisCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (redisCode == null) {
            throw new RuntimeException("验证码已失效，请刷新");
        }

        // 忽略大小写比对
        if (!redisCode.equalsIgnoreCase(captcha)) {
            throw new RuntimeException("验证码错误");
        }

        // 验证通过后立即删除，防止重复使用（防重放攻击）
        stringRedisTemplate.delete(redisKey);
    }
    @PostMapping("/send-code")
    public Result sendCode(@RequestParam @Email String email) {
        // 这里可以先简单校验一下邮箱是否已被注册

        userService.sendCode(email);
        return Result.success("验证码已发送，请注意查收");
    }
    @PostMapping("/register")
    public Result register(String username, String password, String email, String code,
                           String captcha, String captchaUuid) { // 增加邮箱校验
        // 校验验证码
        validateCaptcha(captcha, captchaUuid);
        User u = userService.findByUsername(username);
        if (u == null) {
            // 传入 email
            userService.register(username, password, email,code);
            return Result.success();
        } else {
            return Result.error("用户名已被占用");
        }
    }
    @PostMapping("/login")
    public Result<String> login(String username, String password,
                                String captcha, String captchaUuid) {
        // 1. 先校验图形验证码
        validateCaptcha(captcha, captchaUuid);

        // 1. 根据用户名查询用户
        User loginUser = userService.findByUsername(username);

        // 2. 判断用户是否存在
        if (loginUser == null) {
            return Result.error("用户名错误");
        }

        // 3. 校验密码

        if (BcryptUtil.match(password, loginUser.getPasswordHash())) {

            // 4. 密码正确，生成 JWT 令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            claims.put("role", loginUser.getRole());

            // 生成 Token
            String token = JwtUtil.genToken(claims);
            //把token存储到Redis中
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            ops.set(token,token,12, TimeUnit.HOURS);
            // 返回 Token 给前端
            return Result.success(token);
        }

        return Result.error("密码错误");
    }

    @PostMapping("/loginByEmail")
    public Result<String> loginByEmail(String email, String code) {
        if (code == null || code.length() != 6) {
            return Result.error("验证码格式错误");
        }
        String token = userService.loginByEmail(email, code);
        return Result.success(token);
    }

    @GetMapping("/info")
    public Result<UserVO> userInfo() {
        // 1. 从 ThreadLocal 获取当前登录用户的数据
        // 因为你的工具类是泛型的，这里强转成 Map 即可
        Map<String, Object> map = ThreadLocalUtil.get();

        // 拿到用户名
        String username = (String) map.get("username");

        // 2. 查询数据库
        User user = userService.findByUsername(username);
        UserVO uservo = new UserVO();
        BeanUtils.copyProperties(user, uservo);
        return Result.success(uservo);
    }

    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

    @PatchMapping("/password")
    public Result updatePassword(@RequestParam @Pattern(regexp = "^\\S{5,16}$") String oldPassword,
                                 @RequestParam @Pattern(regexp = "^\\S{5,16}$") String newPassword) {
        // 1. 获取当前登录用户
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        // 2. 根据用户名查询用户
        User loginUser = userService.findByUsername(username);
        if (loginUser == null) {
            return Result.error("用户不存在");
        }
        if (!BcryptUtil.match(oldPassword, loginUser.getPasswordHash())) {
            return Result.error("旧密码错误");
        }
        if (BcryptUtil.match(newPassword, loginUser.getPasswordHash())) {
            return Result.error("新密码不能与旧密码相同");
        }
        loginUser.setPasswordHash(BcryptUtil.encode(newPassword));
        userService.update(loginUser);
        return Result.success();

    }

    @PatchMapping("/avatar")
    public Result updateAvatar(@RequestParam @URL(message = "图片地址格式不正确") String avatarUrl) {
        // 1. 获取当前登录用户
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        // 2. 调用 Service 更新
        userService.updateAvatar(avatarUrl, username);
        return Result.success();
    }

}
