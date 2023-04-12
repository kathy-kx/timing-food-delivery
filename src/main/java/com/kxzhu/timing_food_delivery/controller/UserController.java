package com.kxzhu.timing_food_delivery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.kxzhu.timing_food_delivery.common.BaseContext;
import com.kxzhu.timing_food_delivery.common.R;
import com.kxzhu.timing_food_delivery.entity.User;
import com.kxzhu.timing_food_delivery.service.UserService;
import com.kxzhu.timing_food_delivery.utils.SMSUtils;
import com.kxzhu.timing_food_delivery.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @ClassName UserController
 * @Description 管理user表
 * @Author zhukexin
 * @Date 2023-03-10 13:47
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * 用户点"获取验证码"后，处理ajax请求/user/sendMsg，data为phone电话号
     * 参数：可以用String phone接收，此处用更大的User user接收，含有同名的phone参数。通用性更强
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){//填写的手机号非空
            //生成随机4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);

            //调用阿里云提供的短信服务API，发送验证码短信。为了隐私，以及因为测试专用模板只能使用固定的4-6位纯数字，变量传不进去，所以这里不发短信，只在控制台模拟，自己看code填写进去
            //SMSUtils.sendMessage(code);

            //将生成的验证码保存到session，一会进行校验
            session.setAttribute(phone, code);//以手机号的string形式为键，验证码为值
            return R.success("手机验证码短信发送成功");
        }
        return R.error("手机短信发送失败");
    }

    /**
     * 移动端用户登录
     * 前端带着参数code & phone，请求/user/login
     * 参数可以用UserDto（其中扩展了String code属性）。也可以声明Map类型的map。
     * 参数传来的是json形式：{phone=13444433333, code=1233}，服务器通过在形参中使用@RequestBody注解，可以获取json格式的请求参数（map）
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session){
        //log.info("map: {}", map.toString());
        String phone = map.get("phone"); //获取手机号
        String code = map.get("code"); //获取验证码
        //从session中获取 刚才sendMsg()时session中保存的 该手机号对应的验证码
        Object codeInSession = session.getAttribute(phone);
        //进行验证码比对（页面提交的验证码和Session中保存的验证码比对）
        if( codeInSession != null && codeInSession.equals(code) ){
            //如果能比对成功，登录成功。则需要将用户的id放进session。
            //但是如果验证码填写正确，但是没有此用户，则说明是新用户，需要注册。（所以需要先判断是否是新用户）

            //用手机号到user表中查询：
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if(user == null){//当前phone不在user表中，查不到该phone，说明当前登录的是新用户，需要自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1); //状态 0:禁用，1:正常
                //新加代码：目的是在注册时，给user表的name字段赋值为："用户"+userId的后六位
                //log.info("user_id为：{}", user.getId());
                user.setId(IdWorker.getId());
                user.setName("用户" + (user.getId() + 1000000L) % 1000000L);//假如倒数第六位是0，需要保证结果是六位数，可以在取模前先扩展到至少六位数。
                userService.save(user);
            }
            session.setAttribute("user", user.getId());//将用户的id放进session。userService.save(user);之后，会自动生成user的主键id
            return R.success(user);
        }
        return R.error("登录失败");
    }

    /**
     * 退出登录
     * 在/front/page/user.html页面点击“退出登录”，发送请求到/user/loginout，method= post。
     * 需要服务端处理，从session中清掉用户id。
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest httpServletRequest){
        httpServletRequest.getSession().removeAttribute("user");
        //log.info("ThreadLocal里还有吗：{}", BaseContext.getCurrentId());//还有
        return R.success("登出成功");
    }
}
