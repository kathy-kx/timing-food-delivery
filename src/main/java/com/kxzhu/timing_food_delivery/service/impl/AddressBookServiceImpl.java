package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.entity.AddressBook;
import com.kxzhu.timing_food_delivery.mapper.AddressBookMapper;
import com.kxzhu.timing_food_delivery.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
