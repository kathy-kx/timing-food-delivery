# timing-food-delivery
有时外卖项目源自B站黑马程序员的瑞吉外卖，由Spring Boot + Mybatis Plus构建，提供用户点餐、餐厅管理、订单处理等功能。有时外卖在原课程基础上，增加了完整的注释，补充了课堂未实现的功能，希望帮助大家学习。

## 1. 项目预览

### 1.1 后台功能

后台模块是为餐厅提供的综合管理平台。包含的功能有：

- 管理员登录
- 员工管理
    - 添加员工
    - 编辑员工信息
    - 禁用/启用员工
- 分类管理
    - 新增菜品/套餐分类
    - 编辑/删除分类
- 菜品管理
    - 新增、修改菜品信息
    - 删除、启售、停售菜品（支持批量操作）
- 套餐管理
    - 新增、修改套餐信息
    - 删除、启售、停售套餐（支持批量操作）
- 订单管理
    - 查询订单
    - 派送订单

![管理员登录](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/1.png)

![菜品管理](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/2.png)

![订单管理](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/3.png)

### 1.2 前台功能

前台是为用户提供的移动端应用。包含的功能有：

- 用户登录注册
- 地址管理
    - 新增、修改、删除地址
    - 设为默认地址
- 将菜品加入购物车
    - 选择规格（忌口、辣度、甜度、温度）
    - 增减数量
    - 删除菜品
- 下订单与结算
- 订单管理
    - 查看订单状态
    - 再来一单

![前台功能](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/4.png)

### 1.3 技术栈

- SpringBoot
- MyBatis Plus
- MySQL
- SSM
- Redis


## 2. 版本简介

### v1.0版本

实现基本功能。见1.1节和1.2节。

使用Redis缓冲验证码（有效期设置为5分钟）、菜品数据。

### v1.1版本

实现主从复制、读写分离

主库负责处理事务性的增删改操作，从库负责处理查询操作。使得整个系统的查询性能得到极大的改善。


## 3. 项目部署

### **v1.0版本**

1. 下载[v1.0分支](https://github.com/kathy-kx/timing-food-delivery/tree/v1.0)的代码。

2. 修改`src/main/resources/application.yml`文件中的数据库信息

需要改动：

```yaml
spring:
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/food_delivery_system?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
      username: ${username}
      password: ${password}
```

以及文件上传路径

```yaml
timing_food_delivery:
  path: 
```

3. 启动本地Redis服务。如未安装和配置，步骤可参考：[Mac安装Redis](https://blog.csdn.net/realize_dream/article/details/106227622)

启动redis服务：

方式一：`redis-server`

方式二：启动服务并显式加载配置文件（推荐，将redis.conf配置文件中的daemonize no修改成yes后，可以后台运行redis）
    `/opt/homebrew/opt/redis/bin/redis-server /opt/homebrew/etc/redis.conf`

连接redis服务： `redis-cli -h 127.0.0.1 -p 6379`

4. 在本地运行项目。

后台访问地址：[localhost:8080/backend/page/login.html](http://localhost:8080/backend/page/login.html)

前台访问地址：[localhost:8080/front/page/login.html](http://localhost:8080/front/page/login.html)

### **v1.1版本**

1. 下载main分支代码
   
2. 提前准备好两台服务器，分别安装MySQL并启动服务，并连接navicat。具体步骤参考笔记1.1.2节：[主从复制实现方法笔记](https://kathy-kx.github.io/2023/11/18/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E4%BC%98%E5%8C%9602-%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6-Nginx/)

3. 配置主库和从库。具体步骤参考笔记1.1.3&1.1.4节：[主从复制实现读写分离方法笔记](https://kathy-kx.github.io/2023/11/18/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E4%BC%98%E5%8C%9602-%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6-Nginx/)

4. 修改`src/main/resources/application.yml`文件中的数据库信息

需要修改：

主数据源和从数据源的url(分别改为主库和从库的ip)、username和password

```yaml
spring:
#sharding jdbc框架 配置读写分离规则
  shardingsphere:
    datasource:
      names:
        master,slave # 名字自定即可。需要在master-data-source-name、slave-data-source-names中对应上
      # 主数据源
      master:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.50.158:3306/food_delivery_system?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: ${username}
        password: ${password}
      # 从数据源
      slave:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.50.99:3306/food_delivery_system?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: ${username}
        password: ${password}
    masterslave:
      # 读写分离配置
      load-balance-algorithm-type: round_robin # 从库负载均衡策略：轮询
      # 最终的数据源名称
      name: dataSource
      # 主库数据源名称
      master-data-source-name: master
      # 从库数据源名称列表，多个逗号分隔
      slave-data-source-names: slave
    props:
      sql:
        show: true # 开启SQL显示，默认false。可以在控制台输出sql
  # 允许bean定义覆盖
  main:
    allow-bean-definition-overriding: true
```

以及文件上传路径

```yaml
timing_food_delivery:
  path: 
```

5. 启动本地Redis服务。

如未安装和配置，步骤可参考：[Mac安装Redis](https://blog.csdn.net/realize_dream/article/details/106227622)

启动redis服务：

方式一：`redis-server`

方式二：启动服务并显式加载配置文件（推荐，将redis.conf配置文件中的daemonize no修改成yes后，可以后台运行redis）
`/opt/homebrew/opt/redis/bin/redis-server /opt/homebrew/etc/redis.conf`

连接redis服务： `redis-cli -h 127.0.0.1 -p 6379`

6. 访问

后台访问地址：[localhost:8080/backend/page/login.html](http://localhost:8080/backend/page/login.html)

前台访问地址：[localhost:8080/front/page/login.html](http://localhost:8080/front/page/login.html)


## 4. 相关资料

### 笔记

我也将详细的笔记、步骤等梳理在个人博客中，可以参考：[外卖项目笔记合集](https://kathy-kx.github.io/categories/Food-Delivery-System/)

### SQL文件

[sql文件](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/food_delivery_system.sql)
