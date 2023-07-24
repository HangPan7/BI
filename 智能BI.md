# 智能BI

> 作者 ：cleverBoy

## 项目介绍

本项目是基于React+Spring Boot+RabbitMQ+AIGC的智能BI数据分析平台，原项目来自[知识星球编程导航](https://yupi.icu/)。

访问地址：http://42.193.236.88/

> AIGC ：Artificial Intelligence Generation Content(AI 生成内容)

区别于传统的BI，数据分析者只需要导入最原始的数据集，输入想要进行分析的目标，就能利用AI自动生成一个符合要求的图表以及分析结论。此外，还会有图表管理、异步生成、AI对话等功能。只需输入分析目标、原始数据和原始问题，利用AI就能一键生成可视化图表、分析结论和问题解答，大幅降低人工数据分析成本。

**优势：** 让不会数据分析的用户也可以通过输入目标快速完成数据分析，大幅节约人力成本，将会用到 AI 接口生成分析结果

**缺点：** 后端受限于ai字数限制，分析的数据不能过长

> 该文档主要是后端接口本项目使用了Swagger + Knife4j 接口文档启动后可以点击该[接口文档](http://localhost:8080/api/doc.html)直接测试;
>
> 前端代码可根据接口自行修改,该项目前端过于丑陋。

## 准备工作

```text
JDK >= 1.8 (推荐1.8版本)
Mysql >= 8.0.20
Maven >= 3.9.2
rabbitMQ >= 3.7.17
redis >= 7.0.0
```

## 运行系统

### 编辑`resources`目录下的`application.yml`

* 配置MySQL

````yaml
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://数据库地址：端口号/my_db?serverTimezone=UTC&useUnicode=true&useSSL=false
    username: 数据库用户名
    password: 该用户密码
````

* 配置redis

  ````yaml
    redis:
      database: 选择存在哪个redis数据库（1-15）
      host: redis IP地址
      port: redis 端口号（默认）
      timeout: 5000
      password: redis密码（没设置密码就将这个删掉）
  ````

* 配置rabbitMQ

  ````yaml
  rabbitmq:
      host: rabbitMQ的IP
      port: rabbitMQ的端口号（默认5672）
      username: 用户名
      password: 密码
  ````

* 端口号

  ````yaml
  server:
    port: 8080 (设置自己想要运行的端口)
  ````

* 配置AI    本项目中选择的是[鱼聪明](https://www.yucongming.com/)

  * 优点
    1. 国产不需要梯子
    2. 可以方便做自己的模型预设 

  * 缺点
    1. 有次数限制

  ````yaml
  #进入鱼聪明ai后可以找到access-key和secret-key填入（注册账号后在“我的”中）
  yuapi:
    client:
      access-key: 
      secret-key: 
  ````

  * 也可以选择ChatGTP(需要魔法，按字数收费),可以导入下面SDK方便操作，需要到[openAI]([API Reference - OpenAI API](https://platform.openai.com/account/api-keys)) 官网申请秘钥

    ````xml
    <!-- https://mvnrepository.com/artifact/com.theokanning.openai-gpt3-java/service -->
    <dependency>
        <groupId>com.theokanning.openai-gpt3-java</groupId>
        <artifactId>service</artifactId>
        <version>0.12.0</version>
    </dependency>
    ````

### 创建SQL

执行sql/create_table.sql

 ### 手动运行com/springbootinit/bizmq/BiInitMQMain.java

自己运行里面的main方法将该方法中的下面代码，按照自己配置文件中的修改，该方法用户自己创建任务队列

````java
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setPassword("密码");
        connectionFactory.setUsername("用户名");
        connectionFactory.setHost("IP");
        connectionFactory.setPort(端口号);
````

## 项目架构图

### 基础架构

基础架构：客户端输入分析诉求和原始数据，向业务后端发送请求。业务后端利用AI服务处理客户端数据，保持到数据库，并生成图表。处理后的数据由业务后端发送给AI服务，AI服务生成结果并返回给后端，最终将结果返回给客户端展示。

[![img](https://user-images.githubusercontent.com/94662685/248857523-deff2de3-c370-4a9a-9628-723ace5ab4b3.png)](https://user-images.githubusercontent.com/94662685/248857523-deff2de3-c370-4a9a-9628-723ace5ab4b3.png)

### 优化项目架构-异步化处理

优化流程（异步化）：客户端输入分析诉求和原始数据，向业务后端发送请求。业务后端将请求事件放入消息队列，并为客户端生成取餐号，让要生成图表的客户端去排队，消息队列根据I服务负载情况，定期检查进度，如果AI服务还能处理更多的图表生成请求，就向任务处理模块发送消息。

任务处理模块调用AI服务处理客户端数据，AI 服务异步生成结果返回给后端并保存到数据库，当后端的AI工服务生成完毕后，可以通过向前端发送通知的方式，或者通过业务后端监控数据库中图表生成服务的状态，来确定生成结果是否可用。若生成结果可用，前端即可获取并处理相应的数据，最终将结果返回给客户端展示。在此期间，用户可以去做自己的事情。 [![image](https://user-images.githubusercontent.com/94662685/248858431-6dbf41e0-adfe-40cf-94da-f3db6c73b69d.png)](https://user-images.githubusercontent.com/94662685/248858431-6dbf41e0-adfe-40cf-94da-f3db6c73b69d.png)

## 项目技术栈和特点

### 后端

1. Spring Boot 2.7.2
2. Spring MVC
3. MyBatis + MyBatis Plus 数据访问（开启分页）
4. Spring Boot 调试工具和项目处理器
5. Spring AOP 切面编程
6. Spring 事务注解
7. Redis：Redisson限流控制
8. MyBatis-Plus 数据库访问结构
9. IDEA插件 MyBatisX ： 根据数据库表自动生成
10. RabbitMQ：消息队列
11. AI SDK：鱼聪明AI接口开发
12. JDK 线程池及异步化
13. Swagger + Knife4j 项目文档
14. Easy Excel：表格数据处理、Hutool工具库 、Apache Common Utils、Gson 解析库、Lombok 注解

### 前端

1. React 18
2. Umi 4 前端框架
3. Ant Design Pro 5.x 脚手架
4. Ant Design 组件库
5. OpenAPI 代码生成：自动生成后端调用代码（来自鱼聪明开发平台）
6. EChart 图表生成

### 数据存储

- MySQL 数据库

### 项目特性

- Spring Session Redis 分布式登录
- 全局请求响应拦截器（记录日志）
- 全局异常处理器
- 自定义错误码
- 封装通用响应类
- Swagger + Knife4j 接口文档
- 自定义权限注解 + 全局校验
- 全局跨域处理
- 多环境配置

### 项目功能

- 用户登录、注册、注销
- 图表创建、删除、查询、查看原数据

### 单元测试

- JUnit5 单元测试、业务功能单元测试

## 项目BUG

- AI生成的内容导致查询图表出现报错，由于AIGC得出的结果不一定是JSON数据，导致前端JSON数据格式解析失败

## 后续项目改造

- 增加聊天功能
- 加入接口调用次数
- 判断ai输出格式是否正确