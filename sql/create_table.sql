# 数据库初始化
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>
use mysql ;
select host,user,grant_priv,super_priv from user;
select * from mysql.user;
update mysql.user set Insert_priv='Y',Update_priv='Y',Delete_priv='Y',Create_priv='Y',Drop_priv='Y' where user = 'root' and host = '%';
flush privileges;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' ;
-- 创建库
create database if not exists my_db;

-- 切换库
use my_db;

-- 用户表
-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                                                                                     not null comment '账号',
    userPassword varchar(512)                                                                                     not null comment '密码',
    userName     varchar(256)  default 'cleverBoy'                                                                null comment '用户昵称',
    userAvatar   varchar(1024) default 'https://tupian.qqw21.com/article/UploadPic/2021-4/202141120475135553.jpg' null comment '用户头像',
    userProfile  varchar(512)                                                                                     null comment '用户简介',
    userRole     varchar(256)  default 'user'                                                                     not null comment '用户角色：user/admin/ban',
    createTime   datetime      default CURRENT_TIMESTAMP                                                          not null comment '创建时间',
    updateTime   datetime      default CURRENT_TIMESTAMP                                                          not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint       default 0                                                                          not null comment '是否删除'
)
    comment '用户' collate = utf8mb4_unicode_ci;

create index idx_unionId
    on user (userAccount);


-- auto-generated definition
create table chart
(
    id          bigint auto_increment comment 'id'
        primary key,
    goal        text                                   null comment '分析目标',
    chartData   text                                   null comment '图标数据',
    chartType   varchar(256)                           null comment '图标类型',
    genResult   text                                   null comment '生成分析结论',
    userId      bigint                                 not null comment '创建用户 id',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0                 not null comment '是否删除',
    chartName   varchar(128)                           null comment '图表名称',
    genChart    text                                   null comment '生成的图表数据',
    status      varchar(128) default 'wait'            not null comment 'wait ,running ,succeed ,failed',
    execMessage text                                   null comment '执行信息'
)
    comment '帖子' collate = utf8mb4_unicode_ci;
