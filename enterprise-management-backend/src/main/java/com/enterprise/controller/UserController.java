package com.enterprise.controller;

import com.enterprise.dto.PageRequest;
import com.enterprise.dto.Result;
import com.enterprise.entity.User;
import com.enterprise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 Controller
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（分页）
     * GET /api/users?pageNum=1&pageSize=10&keyword=xxx
     */
    @GetMapping
    public Result<?> getUserList(PageRequest pageRequest) {
        try {
            return Result.success(userService.findByPage(pageRequest));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户详情
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable Long id) {
        try {
            return Result.success(userService.findById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建用户
     * POST /api/users
     */
    @PostMapping
    public Result<?> createUser(@RequestBody User user) {
        try {
            userService.create(user);
            return Result.success("创建成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            userService.update(user);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
