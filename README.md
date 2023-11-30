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

## 2. 项目部署

### **v1.0版本**

1. 下载代码。
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

1. 如在本地运行，在IntelliJ IDEA中运行项目。

后台访问地址：[localhost:8080/backend/page/login.html](http://localhost:8080/backend/page/login.html)

前台访问地址：[localhost:8080/front/page/login.html](http://localhost:8080/front/page/login.html)

如需在服务器上部署，先将项目打成`jar`包，运行`java -jar packageName`

### **v1.1版本**

1. 下载代码。
2. 修改`src/main/resources/application.yml`文件中的数据库信息

需要修改：

主数据源和从数据源的url、username和password

1. 提前准备好两台服务器，分别安装MySQL并启动服务，并连接navicat。具体步骤参考笔记1.1.2节：[主从复制实现方法笔记]([https://kathy-kx.github.io/2023/11/18/瑞吉外卖优化02-主从复制-Nginx/](https://kathy-kx.github.io/2023/11/18/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E4%BC%98%E5%8C%9602-%E4%B8%BB%E4%BB%8E%E5%88%86%E7%A6%BB-Nginx/))
2. 配置主库和从库。具体步骤参考笔记1.1.3&1.1.4节：![主从复制实现读写分离方法笔记]([https://kathy-kx.github.io/2023/11/18/瑞吉外卖优化02-主从复制-Nginx/](https://kathy-kx.github.io/2023/11/18/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E4%BC%98%E5%8C%9602-%E4%B8%BB%E4%BB%8E%E5%88%86%E7%A6%BB-Nginx/))
3. 打开redis数据库
4. 访问方法**待更新**

## 3. 版本简介

### v1.0版本

主要功能见1.1节和1.2节。

使用Redis缓冲验证码（有效期设置为5分钟）、菜品数据。

### v1.1版本

实现主从复制、读写分离

## 4. 相关资料

### 笔记

我也将详细的笔记、步骤等梳理在个人博客中，可以参考：![github Blog外卖项目](https://kathy-kx.github.io/categories/Food-Delivery-System/)

### SQL文件

![sql文件地址待更新]()
