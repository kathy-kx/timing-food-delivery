# Timing Food Delivery Project

The Timing Food Delivery project is inspired by the "Regi Takeout" tutorial from Bilibili. It is built using **Spring Boot** and **MyBatis Plus** and provides features such as user ordering, restaurant management, and order processing. 
This project includes comprehensive comments and expanded functionalities that were not implemented in the original tutorial to help learners better understand the system.

## 1. Project Overview

### 1.1 Admin Features

The backend module is a comprehensive management platform designed for restaurant administrators. The main features include:

- Admin Login
- Employee Management
  - Add new employees
  - Edit employee information
  - Enable/disable employees
- Category Management
  - Add dish/set meal categories
  - Edit/delete categories
- Dish Management
  - Add or update dish information
  - Delete, enable, or disable dishes (supports batch operations)
- Set Meal Management
  - Add or update set meal information
  - Delete, enable, or disable set meals (supports batch operations)
- Order Management
  - View orders
  - Dispatch orders

Admin Login Interface:
![Admin Login](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/1.png)

Dish Management Interface:
![Dish Management](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/2.png)

Order Management Interface:
![Order Management](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/3.png)

---

### 1.2 User Features

The frontend is a mobile application for users. The key features include:

- **User Registration and Login**
- **Address Management**
  - Add, edit, and delete addresses
  - Set default address
- **Shopping Cart**
  - Select specifications (e.g., preferences for spiciness, sweetness, or temperature)
  - Increase or decrease dish quantity
  - Remove dishes
- **Order Placement and Checkout**
- **Order Management**
  - View order status
  - Repeat a previous order

**User Interface:**
![Frontend Features](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/4.png)

---

### **1.3 Tech Stack**

- Spring Boot  
- MyBatis Plus  
- MySQL  
- SSM (Spring + SpringMVC + MyBatis)  
- Redis  

---

## **2. Version Details**

### **Version v1.0**  

The initial version includes the basic functionalities outlined in sections **1.1** and **1.2**.

Key highlights:

- Use **Redis** to cache verification codes (valid for 5 minutes) and dish data.

---

### **Version v1.1**  

This version introduces **master-slave replication** and **read-write separation**. This greatly improves the system's query performance.

- The **master database** handles transactional operations (insert, update, delete).
- The **slave database** handles query operations.

---

## **3. Project Deployment**

### **v1.0 Deployment Steps**

1. Download the [v1.0 version](https://github.com/kathy-kx/timing-food-delivery/releases/tag/release_v1.0) from GitHub.

2. Edit the **`application.yml`** file in **`src/main/resources/`** to configure your database connection.

   Example:

   ```yaml
   spring:
     datasource:
       druid:
         driver-class-name: com.mysql.cj.jdbc.Driver
         url: jdbc:mysql://localhost:3306/food_delivery_system?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
         username: ${username}
         password: ${password}
   ```

   Also update the file upload path:

   ```yaml
   timing_food_delivery:
     path: 
   ```

3. Start your **local Redis service**. If you haven’t installed Redis yet, follow the instructions here: [Installing Redis on Mac](https://blog.csdn.net/realize_dream/article/details/106227622).

   **Start Redis service:**

   - Option 1:  
     ```bash
     redis-server
     ```

   - Option 2 (recommended):  
     ```bash
     /opt/homebrew/opt/redis/bin/redis-server /opt/homebrew/etc/redis.conf
     ```

     Ensure **`daemonize`** is set to **`yes`** in the Redis config file (redis.conf) to run the service in the background.

   **Connect to Redis:**
   ```bash
   redis-cli -h 127.0.0.1 -p 6379
   ```

4. Run the project locally.

   - **Admin Interface:** [http://localhost:8080/backend/page/login.html](http://localhost:8080/backend/page/login.html)  
   - **User Interface:** [http://localhost:8080/front/page/login.html](http://localhost:8080/front/page/login.html)

---

### **v1.1 Deployment Steps**

1. Download the [v1.1 version](https://github.com/kathy-kx/timing-food-delivery/releases/tag/release_v1.1) from GitHub.

2. Prepare **two servers** with MySQL installed and running. Connect with Navicat. For detailed steps, refer to the notes in section **1.1.2**: [Master-Slave Replication Guide](https://kathy-kx.github.io/2023/11/18/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E4%BC%98%E5%8C%9602-%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6-Nginx/).

3. Configure **master-slave replication**. Detailed steps are in sections **1.1.3** and **1.1.4**: [Read-Write Separation Guide](https://kathy-kx.github.io/2023/11/18/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E4%BC%98%E5%8C%9602-%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6-Nginx/).

4. Modify the **`application.yml`** file to configure the **master** and **slave** databases.

   Example:

   ```yaml
spring:
  shardingsphere:
    datasource:
      names:
        master,slave 
      master:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.50.158:3306/food_delivery_system?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: ${username}
        password: ${password}
      
      slave:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.50.99:3306/food_delivery_system?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: ${username}
        password: ${password}
    masterslave:
      load-balance-algorithm-type: round_robin 
      name: dataSource
      master-data-source-name: master
      slave-data-source-names: slave
    props:
      sql:
        show: true 
  main:
    allow-bean-definition-overriding: true
   ```

   Also update the file upload path:

   ```yaml
   timing_food_delivery:
     path: 
   ```
   
5. Start the **local Redis service** (refer to step 3 in v1.0 deployment).

6. Access the project:

   - **Admin Interface:** [http://localhost:8080/backend/page/login.html](http://localhost:8080/backend/page/login.html)  
   - **User Interface:** [http://localhost:8080/front/page/login.html](http://localhost:8080/front/page/login.html)

---

## **4. Resources**

### **Notes**  
I’ve compiled detailed notes and guides on my personal blog: [Food Delivery System Notes](https://kathy-kx.github.io/categories/Food-Delivery-System/).

### **SQL Files**  
[SQL Script](https://github.com/kathy-kx/timing-food-delivery/blob/main/resources/food_delivery_system.sql)

### **Dish Images**  
[Dish Image Resources](https://github.com/kathy-kx/timing-food-delivery/tree/main/resources/%E5%9B%BE%E7%89%87%E8%B5%84%E6%BA%90)