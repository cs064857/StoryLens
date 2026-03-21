package com.shijiawei.storylens.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.storylens.entity.Scene;
import com.shijiawei.storylens.mapper.SceneMapper;
import com.shijiawei.storylens.service.inter.SceneService;
import org.springframework.stereotype.Service;

@Service
public class SceneServiceImpl extends ServiceImpl<SceneMapper, Scene> implements SceneService {
}
