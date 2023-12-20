-- 创建库
CREATE DATABASE IF NOT EXISTS multifind_hub;

-- 切换库
USE multifind_hub;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user
(
    id           BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    userAccount  VARCHAR(256)                           NOT NULL COMMENT '账号',
    userPassword VARCHAR(512)                           NOT NULL COMMENT '密码',
    unionId      VARCHAR(256)                           NULL COMMENT '微信开放平台id',
    mpOpenId     VARCHAR(256)                           NULL COMMENT '公众号openId',
    userName     VARCHAR(256)                           NULL COMMENT '用户昵称',
    userAvatar   VARCHAR(1024)                          NULL COMMENT '用户头像',
    userProfile  VARCHAR(512)                           NULL COMMENT '用户简介',
    userRole     VARCHAR(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin/ban',
    createTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete     TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_unionId (unionId)
    ) COMMENT '用户' DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 帖子表
CREATE TABLE IF NOT EXISTS t_post
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    title      VARCHAR(512)                       NULL COMMENT '标题',
    content    TEXT                               NULL COMMENT '内容',
    tags       VARCHAR(1024)                      NULL COMMENT '标签列表（json 数组）',
    thumbNum   INT      DEFAULT 0                 NOT NULL COMMENT '点赞数',
    favourNum  INT      DEFAULT 0                 NOT NULL COMMENT '收藏数',
    userId     BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_userId (userId)
    ) COMMENT '帖子' DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
CREATE TABLE IF NOT EXISTS t_post_thumb
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    postId     BIGINT                             NOT NULL COMMENT '帖子 id',
    userId     BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_postId (postId),
    INDEX idx_userId (userId)
    ) COMMENT '帖子点赞' DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- https://t.zsxq.com/0emozsIJh

-- 帖子收藏表（硬删除）
CREATE TABLE IF NOT EXISTS t_post_favour
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    postId     BIGINT                             NOT NULL COMMENT '帖子 id',
    userId     BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_postId (postId),
    INDEX idx_userId (userId)
    ) COMMENT '帖子收藏' DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;