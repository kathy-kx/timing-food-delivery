package com.kxzhu.timing_food_delivery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kxzhu.timing_food_delivery.common.BaseContext;
import com.kxzhu.timing_food_delivery.common.R;
import com.kxzhu.timing_food_delivery.entity.AddressBook;
import com.kxzhu.timing_food_delivery.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * 处理请求/addressBook：填写表单后，点"保存地址"，将填写的地址信息(json形式{consignee: "abc", phone: "13323234343", sex: "0", detail: "avdfd", label: "公司"})封装到addressBook中，发请求到服务器。
     * 需要保存到address_book表
     * 1、先获取到此登录用户的id，跟封装的addressBook对象（表中这一条数据）对应上
     * 2、将json数据（封装在addressBook对象中）保存到address_book表
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());//先登录成功，就可以使用BaseContext.getCurrentId()获得当前登录用户id。
        // BaseContext是我们基于ThreadLocal封装的工具类，用于保存和获取当前登录用户的id.
        //log.info("addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     * 处理请求/addressBook/default，
     * ajax请求携带参数：(需要设为默认地址的那个)地址id，使用AddressBook实体对象接收参数，实际上只传过来地址id
     */
    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        //log.info("addressBook:{}", addressBook);
        //在address_book表中执行update操作：先将address_book表中，当前登录用户的 所有地址id对应的 is_default字段 都改成0
        //SQL: update address_book set is_default = 0 where user_id = ?
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());//BaseContext.getCurrentId()是当前登录用户的id
        wrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(wrapper); //将当前登录用户的 所有地址信息的 is_default字段 都改成0

        addressBook.setIsDefault(1);//将当前要改的这个addressBook的is_default字段 改成1
        //SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 根据地址id 查询地址，以回显到修改地址页面
     * 处理请求/addressBook/${id}
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);//从address_book表中查出地址id对应的那一条数据(addressBook对象)
        if (addressBook != null) {//查到了该地址id对应的一条数据
            return R.success(addressBook);//返回给前端，回显到页面
        } else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public R<AddressBook> getDefault() {
        // 想要执行的SQL: select * from address_book where user_id = ? and is_default = 1
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (null == addressBook) {
            return R.error("没有找到该对象");
        } else {
            return R.success(addressBook);
        }
    }

    /**
     * 查询指定（当前）用户的全部地址
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());//获得当前登录用户的id，并和封装的addressBook对象（表中这一条数据）对应上
        //log.info("addressBook:{}", addressBook);

        //条件构造器
        //SQL:select * from address_book where user_id = ? order by update_time desc
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        return R.success(addressBookService.list(queryWrapper));//将从address_book表中查出的结果，返回给前端
    }

    /**
     * 修改地址信息
     * 请求/addressBook,修改某地址id对应的那一条数据
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        // update address_book set 所有字段 where id = ? (id = addressBook.getId())
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    /**
     * 删除地址
     * 处理请求/addressBook?ids=1634529822791176194
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String[] ids){//此页面只会有一个ids，数组中只有一个元素。为了通用性，所以写成数组。
        for(String id : ids){
            addressBookService.removeById(id);
        }
        return R.success("删除成功");
    }
}
