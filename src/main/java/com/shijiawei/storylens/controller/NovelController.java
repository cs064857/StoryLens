package com.shijiawei.storylens.controller;

import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.shijiawei.storylens.entity.Novel;
import com.shijiawei.storylens.service.inter.NovelService;
import com.shijiawei.storylens.utils.R;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/novel")
public class NovelController {

    @Resource
    private NovelService novelService;


    /**
     * 上傳Epub或Txt至Minio中
     * @param file 文件
     * @return
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Map<String, Object>> uploadNovel(@RequestParam("file") MultipartFile file) {
        return novelService.uploadNovel(file);
    }

    /**
     * 初始化小說設定(單獨生成人物、場景、道具、武器)
     * @param novelId 小說ID
     * @return
     */
    @PostMapping("/init/{novelId}")
    public R<Void> initNovel(@PathVariable("novelId") Long novelId) {
        // TODO:呼叫 NovelService或AI相關的Service解析小說內容，
        // 並單獨生成人物、場景、道具、武器等實體資料。
        return R.ok();
    }

    /**
     * 分頁取得所有小說
     * @param current 頁碼，預設為1
     * @param size 每頁筆數，預設10
     * @return
     */
    @GetMapping("/page")
    public R<Page<Novel>> getNovelPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return novelService.getNovelPage(current, size);
    }
}
