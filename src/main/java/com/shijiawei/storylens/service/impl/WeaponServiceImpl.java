package com.shijiawei.storylens.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.storylens.entity.Weapon;
import com.shijiawei.storylens.mapper.WeaponMapper;
import com.shijiawei.storylens.service.inter.WeaponService;
import org.springframework.stereotype.Service;

@Service
public class WeaponServiceImpl extends ServiceImpl<WeaponMapper, Weapon> implements WeaponService {
}
