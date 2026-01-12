package com.enterprise.dao;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper 接口（ORM 对象关系映射）
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据 ID 查询用户
     */
    User findById(@Param("id") Long id);

    /**
     * 查询用户列表（分页）
     */
    List<User> findByPage(PageRequest pageRequest);

    /**
     * 查询用户总数
     */
    int countTotal(PageRequest pageRequest);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") Long id);
}
