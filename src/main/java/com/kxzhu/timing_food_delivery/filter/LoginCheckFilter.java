package com.kxzhu.timing_food_delivery.filter;

import com.alibaba.fastjson.JSON;
import com.kxzhu.timing_food_delivery.common.BaseContext;
import com.kxzhu.timing_food_delivery.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static javax.swing.text.html.CSS.getAttribute;

/**
 * @ClassName LoginCheckFilter
 * @Description 登录检查过滤器（也可以用拦截器interceptor实现，此项目中用过滤器实现）
 * 检查用户是否已经完成登录
 * @Author zhukexin
 * @Date 2023-02-15 14:58
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
//@WebFilter替代web.xml配置过滤器。filterName指定过滤器名称（随意）；urlPatterns指定拦截的请求：/* 工程路径下所有的请求都拦截
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();//专门用于路径比较

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();
        //log.info("拦截到请求：{}", requestURI);

        //2、判断本次请求是否需要处理
        //不需要处理的请求路径：
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",  // /backend下的页面、静态资源其实可以看，但是不给看页面上的数据（动态请求）
                "/front/**",
                "/user/sendMsg", //移动端 发送验证短信
                "/user/login" //移动端 登录
        };
        boolean check = check(urls, requestURI);

        //3、如果不需要处理，则直接放行
        if(check){
            //log.info("本次请求{}无需处理", requestURI); // {}是占位符，由后面的参数填充
            chain.doFilter(request, response);  //放行
            return;//放行之后，后面代码不需要执行了，直接return结束
        }

        //4-1、判断登录状态，如果已登录，则直接放行
        // 并且把登录用户的id拿到，放到threadLocal中
        HttpSession session = request.getSession();
        if(session.getAttribute("employee") != null){  //判断后台员工登录状态
            //已登录
            //log.info("用户已登录，用户id为：{}", session.getAttribute("employee")); //session中，"employee"记录的是登录用户的id

            /*
            为了实现"公共字段自动填充"功能。
            我们可以在LoginCheckFilter的doFilter方法中获取当前登录用户id，
            并调用ThreadLocal的set方法来设置当前线程的线程局部变量的值（用户id)，
            然后在MyMetaObjectHandler的updateFill方法中调用ThreadLocal的get方法来获得当前线程所对应的线程局部变量的值(用户id)
            */
            Long empId = (Long) session.getAttribute("employee");
            BaseContext.setCurrentId(empId);    //实际上是调用：threadLocal.set(id);

            chain.doFilter(request, response);  //放行
            return;
        }
        //4-2、判断登录状态，如果已登录，则直接放行
        // 并且把登录用户的id拿到，放到threadLocal中
        //此处有问题：如果用户登录了，也可以进入管理后台。后期可以优化
        if(session.getAttribute("user") != null){  //判断前台用户登录状态
            //已登录
            //log.info("用户已登录，用户id为：{}", session.getAttribute("employee")); //session中，"employee"记录的是登录用户的id
            /*
            为了实现"公共字段自动填充"功能。
            我们可以在LoginCheckFilter的doFilter方法中获取当前登录用户id，
            并调用ThreadLocal的set方法来设置当前线程的线程局部变量的值（用户id)，
            然后在MyMetaObjectHandler的updateFill方法中调用ThreadLocal的get方法来获得当前线程所对应的线程局部变量的值(用户id)
            */
            Long userId = (Long) session.getAttribute("user");
            BaseContext.setCurrentId(userId);    //实际上是调用：threadLocal.set(id);

            chain.doFilter(request, response);  //放行
            return;
        }
        else {
            //5、如果未登录 向浏览器响应数据 返回未登录结果
            //log.info("用户未登录");
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
            /*通过输出流向浏览器响应数据，给R.error传回msg ——"NOTLOGIN"。
            在request.js页面的前端响应拦截器处，会拦截服务器给的响应。如果code==0，且msg显示NOTLOGIN，说明用户未登录，会跳转到登录页面
            */
            return; //不放行，结束方法
        }
    }

    /**
     * 检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI){
        for(String url : urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true; //匹配上了
            }
        }
        return false; //匹配不上
    }
}
