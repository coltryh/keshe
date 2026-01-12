package com.enterprise.controller;

import com.enterprise.dto.LoginRequest;
import com.enterprise.dto.RegisterRequest;
import com.enterprise.dto.Result;
import com.enterprise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller（MVC 控制器层）
 * 提供 RESTful API 接口
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginRequest loginRequest) {
        try {
            return Result.success("登录成功", userService.login(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            ));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest registerRequest) {
        try {
            userService.register(
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.getEmail()
            );
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
