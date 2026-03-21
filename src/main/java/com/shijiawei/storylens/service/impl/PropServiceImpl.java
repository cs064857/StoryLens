package com.shijiawei.storylens.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.storylens.entity.Prop;
import com.shijiawei.storylens.mapper.PropMapper;
import com.shijiawei.storylens.service.inter.PropService;
import org.springframework.stereotype.Service;

@Service
public class PropServiceImpl extends ServiceImpl<PropMapper, Prop> implements PropService {
}
