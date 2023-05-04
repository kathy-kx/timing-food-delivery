package com.kxzhu.timing_food_delivery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kxzhu.timing_food_delivery.common.R;
import com.kxzhu.timing_food_delivery.entity.Employee;
import com.kxzhu.timing_food_delivery.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

/**
 * @ClassName EmployeeController
 * @Description 响应客户端页面发来的请求，controller处理完之后给页面结果
 * 把结果封装到R类对象中
 * @Author zhukexin
 * @Date 2023-02-14 10:38
 */
@Slf4j  //输出日志
@RestController //@ResponseBody + @Controller
@RequestMapping("/employee") // 请求地址/employee/** 由本类处理
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录功能
     * 处理请求：/employee/login，method=post
     * @param employee 登录时，浏览器发送post请求给服务器，其中带着员工登录信息，封装到Employee对象中。
     *                 服务器可通过在形参使用@RequestBody获取json格式的请求参数。
     * @param request 便于获取session，将employee对象的id存一份到session，表示登录成功，便于获取登录用户？？
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request){
        //形参解释：
        //发送给服务器的请求 带来了json形式的参数{"username":"admin","password":"123456"}，所以要用@RequestBody接收。
        //将参数封装成employee对象。注意参数名必须和Employee类的属性名一致

        //根据登录逻辑图，按步骤实现：
        //1、将页面提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username来查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();//包装查询对象
        queryWrapper.eq(Employee::getUsername, employee.getUsername());//添加查询条件。？？
        // Employee::getUsername是静态方法引用。类名::方法名，表示调用Employee类的getUsername方法(而不造对象)
        // 这一句判断在数据库中遍历出的用户名 是否和前端页面用户输入的用户名(封装在employee中)相同，相同的话即代表数据库中有这个值，反之则无
        Employee emp = employeeService.getOne(queryWrapper);//从数据库查出唯一的数据（表中username字段是UNIQUE的，所以用getOne就可以）

        //3、如果没有查询到则返回失败结果
        if(emp == null){
            return R.error("登录失败"); //不要提示"用户名不存在"，否则有用户名遍历的漏洞风险
        }

        //4、比对密码，将从前端页面获取的密码md5加密后 与 数据库查出来的密码(也是加密的)进行比对，如果不一致则返回提示信息
        if(! emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        //5、查看员工状态，如果已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        //6、登录成功，将用户id存入Session并返回成功结果
        HttpSession session = request.getSession();
        session.setAttribute("employee", emp.getId());//将查出来的emp对象的id 存一份到session，表示该用户登录成功，便于后面获取登录用户
        return R.success(emp);
    }

    /**
     * 退出登录
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //关于参数：因为需要操作session，所以参数写一个request

        //清理session中保存的当前登录员工的id
        HttpSession session = request.getSession();
        session.removeAttribute("employee"); //登录时，放进session "employee"，保存登录成功的员工id
        return R.success("退出成功");
    }

    /**
     * 添加员工
     * @param employee 待被添加的员工
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee, HttpServletRequest request){//前端传来json形式的请求参数，形参加上@RequestBody
        //String是因为，保存员工时，前端只需要code这个数据，不需要data，所以用一个简单的String即可

        //log.info("新增员工，员工信息：{}", employee.toString());

        //设置初始密码(需要md5加密处理)
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //employee.setCreateTime(LocalDateTime.now());//没使用mybatisplus的"自动填充公共字段"功能之前，自己设置创建/更新时间
        //employee.setUpdateTime(LocalDateTime.now());//没使用mybatisplus的"自动填充公共字段"功能之前，自己设置创建/更新时间

        //status在入库时默认值1，此处不用设置

        //没使用mybatisplus的"自动填充公共字段"功能之前，自己设置创建/更新人（
        //从session中取出当前操作的员工。（先有一个员工登录成功，由这个员工点"添加员工"，调用save(employee)，添加的是新的员工
        //Long empId = (Long) request.getSession().getAttribute("employee"); //getAttribute()返回的是Object型，需要向下转型为Long
        //employee.setCreateUser(empId); //操作人
        //employee.setUpdateUser(empId);

        // 使用mybatisplus的"自动填充公共字段"功能之后：不在这里设置了
        // 在自定义的元数据对象处理器MyMetaObjectHandler中设置metaObject.setValue("createTime", LocalDateTime.now());

        employeeService.save(employee);//调用employeeService的save()，其实是继承父接口（mybatis plus定义的）的save()

        return R.success("新增员工成功");
    }

    /**
     * 员工信息的分页查询
     * @param page 第几页
     * @param pageSize 多少条
     * @param name 查询员工姓名
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        /*
        返回类型：R，泛型不能是Employee。因为需要传给浏览器records、total数据，以显示分页页面（见list.html），这里使用Page
        页面需要什么数据，我们就得给页面什么数据。这里需要承装Page。
        参数：请求中带着参数(http://localhost:8080/employee/page?page=1&pageSize=10)发过来，声明和请求参数同名的形参
        跳页码时，请求参数带着page和pageSize；在搜索框搜索员工姓名时，还另外带着name请求参数
        */
        //log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name); //参数传递没问题

        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        //构造条件构造器：如果name传过来，还需要过滤条件（where name= ?）
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件：只要name不为空，就在Employee表中 根据name字段查询，要查的值是传来的参数name
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);//like方法的参数：执行条件，字段，值。相当于：LIKE '%值%'
        //添加排序条件：按照Employee表里的更新时间 倒序排列，即最近更新的排在前面(orderBy)
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo, queryWrapper); //处理好会封装到Page类型的pageInfo中，pageInfo中含有数据列表records、总记录数total等信息

        return R.success(pageInfo);//把pageInfo信息传回前端，list.html中的getMemberList()方法可以拿到Page对象里的records、total等信息
    }

    /**
     * 根据id修改员工信息
     * 从修改员工页面，修改信息后，点击"确定"，向http://localhost:8080/employee发送put请求
     * @param employee
     * @return
     */
    @PutMapping("")
    public R<String> update(@RequestBody Employee employee, HttpServletRequest request){
        //返回类型：前端只需要code，所以返回R<String>类型的就可以
        //参数：由于前端传来的是json形式，需加上@RequestBody注解
        //log.info(employee.toString());//Employee(id=1625874579766513700, username=null, name=null, password=null, phone=null, sex=null, idNumber=null, status=0, createTime=null, updateTime=null, createUser=null, updateUser=null)

        //没使用mybatisplus的"自动填充公共字段"功能之前，自己设置创建/更新人/时间。
        // 使用mybatisplus的"自动填充公共字段"功能之后：不在这里设置了
        // 在自定义的元数据对象处理器MyMetaObjectHandler中设置metaObject.setValue("createTime", LocalDateTime.now());
        //还需要另外修改更新时间、更新操作员
        //employee.setUpdateTime(LocalDateTime.now());

        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setUpdateUser(empId);

        employeeService.updateById(employee);
        return R.success("修改成功");
    }

    /**
     * 根据id查询员工
     * 为了修改时，可以回显信息
     * 修改时，浏览器向服务器发送请求：http://localhost:8080/employee/1626479515129643009
     * 需要处理之后，传回code、data（含sex）、msg等数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);//把查出的employee转成json发回前端
        }else {
            return R.error("没有查询到该员工");
        }

    }


}
