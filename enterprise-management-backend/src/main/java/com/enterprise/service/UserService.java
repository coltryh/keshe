package com.enterprise.service;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 用户 Service 接口
 */
public interface UserService {

    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password);

    /**
     * 用户注册
     */
    void register(String username, String password, String email);

    /**
     * 根据 ID 查询用户
     */
    User findById(Long id);

    /**
     * 分页查询用户列表
     */
    Map<String, Object> findByPage(PageRequest pageRequest);

    /**
     * 创建用户
     */
    void create(User user);

    /**
     * 更新用户
     */
    void update(User user);

    /**
     * 删除用户
     */
    void deleteById(Long id);
}
