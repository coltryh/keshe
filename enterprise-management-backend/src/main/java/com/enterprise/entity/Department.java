package com.enterprise.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 部门实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    private Long id;
    private String name;
    private Long parentId;  // 父部门ID
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
