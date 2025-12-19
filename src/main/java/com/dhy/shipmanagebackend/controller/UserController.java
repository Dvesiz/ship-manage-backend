package com.dhy.shipmanagebackend.controller;


import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.service.UserService;
import com.dhy.shipmanagebackend.utils.JwtUtil;
import com.dhy.shipmanagebackend.utils.Md5Util;
import com.dhy.shipmanagebackend.utils.ThreadLocalUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Email;

import java.util.HashMap;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/send-code")
    public Result sendCode(@RequestParam @Email String email) {
        // 这里可以先简单校验一下邮箱是否已被注册
        // ... (可选逻辑)

        userService.sendCode(email);
        return Result.success("验证码已发送，请注意查收");
    }
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username,
                           @Pattern(regexp = "^\\S{5,16}$") String password,
                           @Email String email,
                           @NotBlank(message = "验证码不能为空")String code) { // 增加邮箱校验

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
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username,
                                @Pattern(regexp = "^\\S{5,16}$") String password) {

        // 1. 根据用户名查询用户
        User loginUser = userService.findByUsername(username);

        // 2. 判断用户是否存在
        if (loginUser == null) {
            return Result.error("用户名错误");
        }

        // 3. 校验密码
        // Md5Util.checkPassword(用户输入的明文, 数据库里的密文)
        if (Md5Util.checkPassword(password, loginUser.getPasswordHash())) {

            // 4. 密码正确，生成 JWT 令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            claims.put("role", loginUser.getRole());

            // 生成 Token
            String token = JwtUtil.genToken(claims);

            // 返回 Token 给前端
            return Result.success(token);
        }

        return Result.error("密码错误");
    }

    @GetMapping("/info")
    public Result<User> userInfo() {
        // 1. 从 ThreadLocal 获取当前登录用户的数据
        // 因为你的工具类是泛型的，这里强转成 Map 即可
        Map<String, Object> map = ThreadLocalUtil.get();

        // 拿到用户名
        String username = (String) map.get("username");

        // 2. 查询数据库
        User user = userService.findByUsername(username);

        // 3. 抹掉密码，不返回给前端
        user.setPasswordHash("******");

        return Result.success(user);
    }

    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }
}
