package com.kxzhu.timing_food_delivery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kxzhu.timing_food_delivery.common.R;
import com.kxzhu.timing_food_delivery.entity.Category;
import com.kxzhu.timing_food_delivery.entity.Employee;
import com.kxzhu.timing_food_delivery.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName CategoryController
 * @Description
 *
 *
 * @Author zhukexin
 * @Date 2023-02-20 17:04
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 保存分类（新增菜品/套餐分类）
     * 过程：
     * 1、在"分类管理"页面点击"新增菜品分类"，填写名称、排序，点"确定"后，在浏览器中可以看到，发送了ajax请求，将新增分类输入的信息以json形式提交给服务器。
     * 其中data是json形式，{"name":"drinks","type":"1","sort":"2"}，url为http://localhost:8080/category，method为POST。
     * 2、服务端controller接收页面提交的数据，调用service保存数据；
     * 3、service调用mapper操作数据库，保存数据
     * 4、controller返回响应给前端，前端接收到code=1，表示成功，显示"分类添加成功！"
     * 注意：如果添加的分类已经存在，违反了数据表中name字段的唯一性约束，则会抛SQLIntegrityConstraintViolationException异常，
     * 在我们先前写的GlobalExceptionHandler里处理，给前端页面返回" 'xxx'已存在"的错误信息
     *
     * @param category
     * @return
     */
    @PostMapping    //请求是/category，此处不用再写("")
    public R<String> save(@RequestBody Category category){
        //发送ajax请求发送json数据（见浏览器的request body），携带数据(name, sort, type)，所以用@RequestBody获取
        log.info("catagory: {}", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 展示分页数据（分页查询）
     * 过程：请求到/category/list.html页面时，会自动执行vue的钩子函数created()，执行init()。其中执行getCategoryPage()方法，传入参数page和pageSize，
     * 该方法中发送ajax请求到/category/page（get方式），将params参数(page和pageSize)以?xxx=xxx&xxx=xxx的形式，拼接到请求地址url后。
     * 由本方法page(page,pageSize)处理。
     * 控制器方法处理后（调用service、调用mapper），给前端响应。前端此时需要records和total等，所以返回R<page对象>
     * 前端通过ElementUI显示
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        //参数：请求中带着参数(http://localhost:8080/category/page?page=1&pageSize=10)发过来，声明和请求参数同名的形参，就可以接收

        //构造分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //需要排序：
        //构造条件构造器：
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件：按照category表里的sort字段 升序排列，即sort越小越靠前(orderBy)
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo, queryWrapper); //处理好会封装到Page类型的pageInfo中，pageInfo中含有数据列表records、总记录数total等信息

        return R.success(pageInfo);//把pageInfo信息传回前端，list.html中的getMemberList()方法可以拿到Page对象里的records、total等信息
    }

    /**
     * 根据id删除分类
     * 过程：list.html页面点"删除"，执行deleteHandle(id)，其中给出"确认删除"的提示后，如果点击"确定"，就执行deleCategory(id)，传入id。
     * 其中发送ajax请求到/category（delete方式），params以name=value&name=value的方式发送请求参数，请求参数都会被拼接到请求地址url后。请求为http://localhost:8080/category?ids=1627842099162492930 method=DELETE
     * 该请求由本方法处理，调用categoryService.removeById(ids)，并给前端响应
     * 前端得到响应后，判断code是否为1（成功），显示"删除成功"，执行handleQuery()，即执行init()，获取分页信息
     *
     * 删除时，需要检查：待删除的分类是否已经关联了菜品或套餐
     *
     * @param id
     * @return
     */
    @DeleteMapping("")
    public R<String> delete(Long id){
        //参数：请求参数是?xxx=xxx&xxx=xxx形式（category.js中发ajax请求，params: { ids }），则可以直接在参数中声明变量，来获取请求参数
        log.info("删除分类，id为：{}",id);
        //categoryService.removeById(id);//removeById()是由mybatis plus提供的
        categoryService.remove(id);//自己写的service层remove方法，先判断当前分类是否关联菜品/套餐
        return R.success("分类信息删除成功");
    }

    /**
     * 根据id修改分类信息
     * @param category
     * @return
     */
    @PutMapping("")
    public R<String> update(@RequestBody Category category){
        //参数：前端发来json形式的请求参数({"id":"1397844263642378242","name":"湘菜2","sort":1})，用@RequestBody注解，接收请求体中的参数
        //且json形式的请求参数都是实体类Category的属性，可以封装到实体类
        categoryService.updateById(category);
        return R.success("分类信息修改成功");
    }

    /**
     * 根据条件查询分类数据 为list
     * 请求是 http://localhost:8080/category/list?type=1
     * 在添加菜品的时候，需要为菜品添加一个分类。需要在下拉菜单中显示 所有已经创建好的分类。
     * type=1 菜品分类；type=2 套餐分类
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //参数：为什么传来的请求带参数，但是形参用Category接收？
        //用 String type来接收也可以
        //也可以用实体类接收，通用性更强，易于扩展。
        // 只要实体类Category的属性名type 和 传输过来的请求参数 name名字("type")一致，就可以通过 实体类类型的形参 获取请求参数

        //从表中查询所有的分类：select * from category where type = ? (type使用参数传来的type值)
        //构造条件构造器：
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件：根据type查询
        //具体条件用::的写法 Category::getType。具体的值是传过来的参数category.getType()
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());//先判断了一下type不为空
        //添加排序条件：按照category表里的sort字段 升序排列，即sort越小越靠前(orderBy)，再按UpdateTime升序排列，即越晚添加，越靠前
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);//将查询出的list作为data，封装在R对象中
    }

}
