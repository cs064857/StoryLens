package com.shijiawei.storylens.service.inter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.storylens.entity.Novel;
import com.shijiawei.storylens.utils.R;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface NovelService extends IService<Novel> {
    
    R<Map<String, Object>> uploadNovel(MultipartFile file);

    String readNovelFromMinio(Long novelId) throws Exception;

    R<Page<Novel>> getNovelPage(long current, long size);
}
