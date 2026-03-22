package com.shijiawei.storylens.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.storylens.config.MinioProperties;
import com.shijiawei.storylens.entity.Novel;
import com.shijiawei.storylens.mapper.NovelMapper;
import com.shijiawei.storylens.service.inter.NovelService;
import com.shijiawei.storylens.utils.MinioTool;
import com.shijiawei.storylens.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NovelServiceImpl extends ServiceImpl<NovelMapper, Novel> implements NovelService {

    @Resource
    private MinioTool minioTool;

    @Resource
    private MinioProperties minioProperties;

    @Override
    public R<Map<String, Object>> uploadNovel(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("檔案不得為空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("缺少檔案名稱");
            }

            String extension = "";
            String title = originalFilename;
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex + 1).toLowerCase();
                title = originalFilename.substring(0, dotIndex);
            }

            if (!"txt".equals(extension) && !"epub".equals(extension)) {
                throw new IllegalArgumentException("僅支援 TXT 與 EPUB 格式的檔案");
            }

            String bucketName = minioProperties.getBucketName();
            if (!minioTool.isBucketExists(bucketName)) {
                minioTool.createBucket(bucketName);
            }

            String objectName = minioTool.getUploadId() + "." + extension;

            boolean isUploaded = minioTool.uploadFile(bucketName, objectName, file);
            if (!isUploaded) {
                throw new RuntimeException("上傳檔案至 Minio 失敗");
            }

            String filePath = "/" + bucketName + "/" + objectName;

            Novel novel = new Novel();
            novel.setTitle(title);
            novel.setFileType(extension);
            novel.setFilePath(filePath);

            this.save(novel);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "上傳成功");
            result.put("novel", novel);
            return R.ok(result);
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("上傳小說失敗: {}", e.getMessage(), e);
            return R.error("上傳小說失敗: " + e.getMessage());
        }
    }

    @Override
    public String readNovelFromMinio(Long novelId) throws Exception {
        Novel novel = this.getById(novelId);
        if (novel == null) {
            throw new IllegalArgumentException("找不到對應的小說");
        }

        String filePath = novel.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("該小說尚未上傳檔案");
        }

        String bucketName = minioProperties.getBucketName();
        String prefix = "/" + bucketName + "/";
        String objectName;
        if (filePath.startsWith(prefix)) {
            objectName = filePath.substring(prefix.length());
        } else {
            objectName = filePath;
        }

        return minioTool.getObjectAsString(bucketName, objectName);


    }



    @Override
    public R<Page<Novel>> getNovelPage(long current, long size) {
        try {
            Page<Novel> page = new Page<>(current, size);
            return R.ok(this.page(page));
        } catch (Exception e) {
            log.error("取得小說分頁列表失敗: {}", e.getMessage(), e);
            return R.error("取得小說分頁列表失敗: " + e.getMessage());
        }
    }
}
