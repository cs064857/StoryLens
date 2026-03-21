package com.shijiawei.storylens.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.storylens.entity.CharacterWeapon;
import com.shijiawei.storylens.mapper.CharacterWeaponMapper;
import com.shijiawei.storylens.service.inter.CharacterWeaponService;
import org.springframework.stereotype.Service;

@Service
public class CharacterWeaponServiceImpl extends ServiceImpl<CharacterWeaponMapper, CharacterWeapon> implements CharacterWeaponService {
}
