package com.shijiawei.storylens.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.storylens.entity.Character;
import com.shijiawei.storylens.mapper.CharacterMapper;
import com.shijiawei.storylens.service.inter.CharacterService;
import org.springframework.stereotype.Service;

@Service
public class CharacterServiceImpl extends ServiceImpl<CharacterMapper, Character> implements CharacterService {
}
