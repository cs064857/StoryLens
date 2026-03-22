package com.shijiawei.storylens.utils;



import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;

import com.shijiawei.storylens.domain.FileInfo;
import com.shijiawei.storylens.domain.ListResult;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class MinioTool {

    private final MinioClient minioClient;

    public MinioTool(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void shutdown() {
        try {
            if (minioClient != null) {
                minioClient.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("無法關閉連接", e);
        }
    }

    /********************* 桶操作 **********/
    /**
     * 判斷桶是否存在
     *
     * @param bucketName 桶名
     * @return true/false
     */
    public Boolean isBucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("檢查桶是否存在時發生錯誤: " + e.getMessage());
            return false;
        }
    }

    /**
     * 判斷桶列表是否存在
     *
     * @param bucketNameList 桶名列表
     * @return true/false
     */
    public List<ListResult> isBucketExists(List<String> bucketNameList) {
        return bucketNameList.stream()
                .map(item -> new ListResult(item, isBucketExists(item)))
                .collect(Collectors.toList());
    }

    /**
     * 創建桶
     *
     * @param bucketName 桶名
     * @return true/false
     */
    public Boolean createBucket(String bucketName) {
        try {
            if (isBucketExists(bucketName)) {
                log.info("桶" + bucketName + "已存在");
                return false;
            } else {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("創建桶" + bucketName + "成功");
            }
        } catch (Exception e) {
            log.error("創建桶時發生錯誤: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 創建桶列表
     *
     * @param bucketNameList 桶名列表
     * @return true/false
     */
    public List<ListResult> createBucket(List<String> bucketNameList) {
        return bucketNameList.stream()
                .map(item -> new ListResult(item, createBucket(item)))
                .collect(Collectors.toList());
    }

    /**
     * 刪除桶
     *
     * @param bucketName 桶名
     * @return true/false
     */
    public Boolean deleteBucket(String bucketName) {
        try {
            if (isBucketExists(bucketName)) {
                minioClient.removeBucket(RemoveBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("刪除桶" + bucketName + "成功");
                return true;
            } else {
                log.info("桶" + bucketName + "不存在");
                return false;
            }
        } catch (Exception e) {
            log.error("刪除桶時發生錯誤: " + e.getMessage());
            return false;
        }

    }

    /**
     * 刪除桶列表
     *
     * @param bucketNameList 桶名列表
     * @return true/false
     */
    public List<ListResult> deleteBucket(List<String> bucketNameList) {
        return bucketNameList.stream()
                .map(item -> new ListResult(item, deleteBucket(item)))
                .collect(Collectors.toList());
    }

    /**
     * 設置桶標籤
     *
     * @param bucketName 桶名
     * @param tags       標籤
     */
    public Boolean setBucketTags(String bucketName, Map<String, String> tags) {
        try {
            if (!isBucketExists(bucketName)) {
                log.info("桶" + bucketName + "不存在");
                return false;
            }
            minioClient.setBucketTags(SetBucketTagsArgs.builder()
                    .bucket(bucketName)
                    .tags(tags)
                    .build());
            log.info("設置桶" + bucketName + "標籤成功");
            return true;
        } catch (Exception e) {
            log.error("設置桶標籤時發生錯誤: " + e.getMessage());
            return false;
        }

    }

    /**
     * 獲取桶標籤
     *
     * @param bucketName 桶名
     */
    public Map<String, String> getBucketTags(String bucketName) {
        try {
            return minioClient.getBucketTags(GetBucketTagsArgs.builder()
                    .bucket(bucketName)
                    .build()).get();
        } catch (Exception e) {
            log.error("獲取桶標籤時發生錯誤: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 列出所有桶
     *
     * @return 桶名列表
     */
    public List<String> listBuckets() {
        try {
            return minioClient.listBuckets().stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("列出桶時發生錯誤: " + e.getMessage());
            return Collections.emptyList();
        }

    }

    /********** 對象操作 **********/
    /**
     * 判斷對象是否存在
     */
    public Boolean isObjectExists(String bucketName, String objectName) {
        try {
            // 獲得對象的元數據。
            StatObjectArgs.Builder argsBuilder = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName);
            minioClient.statObject(argsBuilder.build());
        } catch (Exception e) {
            log.error("獲取對象失敗: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 查詢單文件
     *
     * @param bucketName
     * @param objectName
     * @return ObjectStat 文件信息
     * 解釋：不提供bucketName，默認使用當前yml配置的桶
     */
    public StatObjectResponse getObject(String bucketName, String objectName) {
        try {
            // 獲得對象的元數據。
            StatObjectArgs.Builder argsBuilder = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName);
            StatObjectResponse objectStat = minioClient.statObject(argsBuilder.build());
            return objectStat;
        } catch (Exception e) {
            log.error("獲取對象失敗: " + e.getMessage());
        }
        return null;
    }

    /**
     * 查詢文件列表
     *
     * @param bucketName
     * @param prefix     前綴
     * @param maxKeys    最大數量
     * @param recursive  是否遞歸
     * @return
     */
    public List<Item> listObjects(String bucketName, String prefix, int maxKeys, Boolean recursive) {
        try {
            if (!isBucketExists(bucketName)) {
                log.info("桶不存在，無法列出對象");
                return Collections.emptyList();
            }
            return fetchObjects(bucketName, prefix, maxKeys, recursive);
        } catch (Exception e) {
            log.error("列出所有對象時發生錯誤: " + e);
            return Collections.emptyList();
        }
    }
    public List<Item> listObjects(String bucketName, String prefix, int maxKeys) {
        return listObjects(bucketName, prefix, maxKeys, true);
    }
    public List<Item> listObjects(String bucketName, String prefix) {
        return listObjects(bucketName, prefix, 0, true);
    }
    public List<Item> listObjects(String bucketName) {
        return listObjects(bucketName, null, 0, true);
    }
    private List<Item> fetchObjects(String bucketName, String prefix, int maxKeys, Boolean recursive) {
        List<Item> objectList = new ArrayList<>();
        try {
            ListObjectsArgs.Builder builder = ListObjectsArgs.builder().bucket(bucketName);

            // 設置前綴、最大數量和遞歸選項
            if (prefix != null) {
                builder.prefix(prefix);
            }
            if (maxKeys > 0) {
                builder.maxKeys(maxKeys);
            }
            if (recursive) {
                builder.recursive(recursive);
            }
            builder.includeUserMetadata(true);

            // 列出對象
            Iterable<Result<Item>> results = minioClient.listObjects(builder.build());
            for (Result<Item> result : results) {
                objectList.add(result.get());
            }
        } catch (Exception e) {
            log.error("列出對象時發生錯誤: " + e.getMessage());
        }
        return objectList;
    }

    /**
     * 查詢多個文件
     *
     * @param bucketName
     * @param objectNameList
     * @return
     */
    public List<StatObjectResponse> listObjects(String bucketName, List<String> objectNameList) {
        if (!isBucketExists(bucketName) || objectNameList.isEmpty()) {
            log.info("桶不存在或對象列表為空");
            return Collections.emptyList();
        }
        List<StatObjectResponse> collect = objectNameList.stream()
                .map(item -> getObject(bucketName, item))
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * 刪除單/多文件/某個目錄下的所有文件，由於minio刪除操作拿不到返回值，只能用Boolen表示是否成功
     *
     * @param bucketName
     * @param objectName
     * @return true/false
     */
    public Boolean deleteObject(String bucketName, String objectName) {
        try {
            if (isObjectExists(bucketName, objectName) == null) {
                log.info("對象不存在，無法刪除");
                return false;
            }
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("刪除對象失敗: " + e.getMessage());
        }
        return false;
    }
    public Boolean deleteObject(String bucketName, List<String> objectNames) {
        if (!isBucketExists(bucketName) || CollectionUtil.isEmpty(objectNames)) {
            log.error("桶不存在或對象列表為空，無法刪除");
            return false;
        }

        try {
            List<DeleteObject> deleteObjects = objectNames.stream().map(DeleteObject::new).collect(Collectors.toList());
            RemoveObjectsArgs build = RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(deleteObjects)
                    .build();

            minioClient.removeObjects(build);
            return true; // 此處返回true，即使部分刪除失敗
        } catch (Exception e) {
            log.error("刪除對象失敗: " + e.getMessage(), e);
        }
        return false;
    }
    public Boolean deleteObjectDir(String bucketName, String dir) {
        try {
            if (!isBucketExists(bucketName)) {
                log.info("桶不存在，無法刪除");
            }
            List<Item> items = listObjects(bucketName, dir);
            List<String> objectNames = new ArrayList<>();
            for (Item item : items) {
                objectNames.add(item.objectName());
            }
            if (CollectionUtil.isEmpty(objectNames)) {
                log.info("目錄下無文件，無法刪除");
                return false;
            }
            return deleteObject(bucketName, objectNames);
        } catch (Exception e) {
            log.error("刪除目錄失敗: " + e.getMessage());
        }
        return false;
    }

    /**
     * 複製文件到指定桶，複製多文件到指定桶，複製桶（不可與源桶一致）
     * 單文件限制大小5g
     *
     * @param sourceBucketName 源桶名
     * @param sourceObjectName 源文件名
     * @param targetBucketName 目標桶名
     * @param targetObjectName 目標文件名
     * @return true/false
     */
    public Boolean copyObject(String sourceBucketName, String sourceObjectName, String targetBucketName, String targetObjectName) {
        try {
            if (isObjectExists(sourceBucketName, sourceObjectName) == null) {
                log.info("源對象不存在");
                return false;
            }
            if (!isBucketExists(targetBucketName)) {
                log.info("目標桶不存在");
                return false;
            }
            CopySource copySource = CopySource.builder().bucket(sourceBucketName).object(sourceObjectName).build();
            CopyObjectArgs copyObjectArgs = CopyObjectArgs.builder()
                    .source(copySource)
                    .bucket(targetBucketName)
                    .object(targetObjectName)
                    .build();
            minioClient.copyObject(copyObjectArgs);
            return true;
        } catch (Exception e) {
            log.error("複製對象失敗: " + e.getMessage());
        }
        return false;
    }
    public List<ListResult> copyObjectList(String sourceBucketName, List<String> sourceObjectName, String targetBucketName) {
        List<ListResult> resultList = new ArrayList<>();
        sourceObjectName.forEach(item -> resultList.add(new ListResult(item, copyObject(sourceBucketName, item, targetBucketName, item))));
        return resultList;
    }
    public List<ListResult> copyBucket(String sourceBucketName, String targetBucketName) {
        List<ListResult> resultList = new ArrayList<>();
        listObjects(sourceBucketName).stream()
                .map(Item::objectName)
                .forEach(item -> resultList.add(new ListResult(item, copyObject(sourceBucketName, item, targetBucketName, item))));
        return resultList;
    }

    /**
     * 複製某個目錄到其他目錄，目錄下所有文件都複製到目標目錄下
     */
    public List<ListResult> copyObjectDir(String sourceBucketName, String sourceDir, String targetBucketName, String targetDir) {
        try {
            if (!isBucketExists(sourceBucketName)) {
                log.info("源桶不存在");
                return Collections.emptyList();
            }
            if (!isBucketExists(targetBucketName)) {
                log.info("目標桶不存在");
                return Collections.emptyList();
            }
            List<Item> items = listObjects(sourceBucketName, sourceDir);
            List<ListResult> resultList = new ArrayList<>();
            for (Item item : items) {
                String objectName = item.objectName();
                String targetObjectName = targetDir + objectName.substring(sourceDir.length());
                resultList.add(new ListResult(objectName, copyObject(sourceBucketName, objectName, targetBucketName, targetObjectName)));
            }
            return resultList;
        } catch (Exception e) {
            log.error("複製目錄失敗: " + e.getMessage());

        }
        return Collections.emptyList();
    }

    /********** 上傳操作 **********/
    /**
     * 簡單上傳，MultipartFile類型上傳單文件
     *
     * @param bucketName 桶名
     * @param objectName 對象名
     * @param file       為MultipartFile類型
     * @return true/false
     */
    public Boolean uploadFile(String bucketName, String objectName, MultipartFile file,Map<String, String> userMetadata) {
        try (InputStream fis = file.getInputStream()) {
            String md5 = DigestUtil.md5Hex(fis);
            userMetadata.put("File-Md5", md5);
            PutObjectArgs build = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .userMetadata(userMetadata)
                    .build();
            minioClient.putObject(build);
            log.info("上傳文件" + objectName + "成功");
            return true;
        } catch (Exception e) {
            log.error("上傳文件時發生錯誤: " + e.getMessage());
        }
        return false;
    }
    public Boolean uploadFile(String bucketName, String objectName, MultipartFile file){
        return uploadFile(bucketName, objectName, file, new HashMap<>());
    }

    /**
     * 簡單上傳，InputStream類型上傳單文件
     *
     * @param bucketName  桶名
     * @param objectName  對象名
     * @param inputStream 輸入流
     * @param contentType 文件類型
     * @return true/false
     */
    public Boolean uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType,Map<String, String> userMetadata) {
        try {
            String md5 = DigestUtil.md5Hex(inputStream);
            userMetadata.put("File-Md5", md5);
            PutObjectArgs build = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(contentType)
                    .userMetadata(userMetadata)
                    .build();
            minioClient.putObject(build);
            log.info("上傳文件" + objectName + "成功");
            return true;
        } catch (Exception e) {
            log.error("上傳文件時發生錯誤: " + e.getMessage());
        }
        return false;
    }
    public Boolean uploadFile(String bucketName, String objectName, InputStream inputStream) {
        return uploadFile(bucketName, objectName, inputStream, "application/octet-stream", new HashMap<>());
    }

    /**
     * 簡單上傳，本地文件類型上傳單文件
     *
     * @param bucketName 桶名
     * @param objectName 對象名
     * @param filePath   文件路徑
     * @param partSize   分片大小
     * @return true/false
     */
    public Boolean uploadFile(String bucketName, String objectName, String filePath, Long partSize,Map<String, String> userMetadata) {
        if (partSize < 5 * 1024 * 1024) {
            log.error("分片大小不能小於5MB");
            return false;
        }
        try (InputStream fis = Files.newInputStream(Paths.get(filePath))) {
            String md5 = DigestUtil.md5Hex(fis);
            userMetadata.put("File-Md5", md5);

            UploadObjectArgs build = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .userMetadata(userMetadata)
                    .filename(filePath, partSize)
                    .build();
            minioClient.uploadObject(build);
            log.info("上傳文件" + objectName + "成功");
            return true;
        } catch (Exception e) {
            log.error("上傳文件時發生錯誤: " + e.getMessage());
        }
        return false;
    }
    public Boolean uploadFile(String objectName, String bucketName, String filePath) {
        return uploadFile(objectName, bucketName, filePath, 5 * 1024 * 1024L, new HashMap<>());
    }

    /**
     * 簡單上傳，文件URL類型上傳單文件
     *
     * @param bucketName 桶名
     * @param objectName 對象名
     * @param fileUrl    文件URL
     * @return true/false
     */
    public Boolean uploadUrlFile(String bucketName, String objectName, String fileUrl, Map<String, String> userMetadata) {
        try (InputStream uploadInputStream = new URL(fileUrl).openStream()) {
            // 獲取文件大小
            HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
            connection.setRequestMethod("GET");
            long fileSize = connection.getContentLengthLong();

            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(uploadInputStream, fileSize, -1)
                    .contentType("application/octet-stream");

            // 讀取流會造成流的指針位置改變，導致無法獲取流的MD5值，因此需要重新讀取流
            InputStream md5InputStream = new URL(fileUrl).openStream();
            String md5 = DigestUtil.md5Hex(md5InputStream);
            userMetadata.put("File-Md5", md5);
            builder.userMetadata(userMetadata);

            minioClient.putObject(builder.build());
            log.info("上傳文件" + objectName + "成功");
            return true;
        } catch (Exception e) {
            log.error("上傳文件時發生錯誤: " + e.getMessage());
        }
        return false;
    }
    public Boolean uploadUrlFile(String bucketName, String objectName, String fileUrl) {
        return uploadUrlFile(bucketName, objectName, fileUrl, new HashMap<>());
    }

    /**
     * 追加新內容上傳，適用於問本，不計算md5
     *
     * @param bucketName
     * @param targetObjectName
     * @param file
     * @return true/false
     */
    public Boolean appendUpload(String bucketName, String targetObjectName, MultipartFile file) {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(targetObjectName)
                .build());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 讀取現有對象內容並寫入輸出流
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            // 追加新文件內容，分批次寫入
            outputStream.write("\n".getBytes());
            try (InputStream fileInputStream = file.getInputStream()) {
                while ((len = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            }

            // 使用新的內容直接上傳到Minio，無需刪除舊對象
            try (InputStream finalInputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(targetObjectName)
                        .stream(finalInputStream, finalInputStream.available(), -1)
                        .contentType(file.getContentType())
                        .build());
            }

            return true;
        } catch (Exception e) {
            log.error("追加上傳文件時發生錯誤: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 獲取上傳ID
     *
     * @return 上傳ID 年月日/時分秒/uuid
     */
    public String getUploadId(String timezone) {
        cn.hutool.core.date.DateTime dateTime = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of(timezone));
        String datePart = DateUtil.format(dateTime, "yyyy-MM-dd");
        String timePart = DateUtil.format(dateTime, "HH-mm-ss");
        String uuid = IdUtil.fastUUID();
        return datePart + "/" + timePart + "/" + uuid;
    }
    public String getUploadId() {
        return getUploadId("Asia/Shanghai");
    }

    // 簡單獲取文件信息
    public FileInfo createMinioFileInfo(String filePath, Long partSize, String bucketName, String objectName, String uploadId) {
        Path path = Paths.get(filePath);
        try (InputStream fis = Files.newInputStream(path)) {
            String fileMd5 = DigestUtil.md5Hex(fis);
            // 計算總分片數，並計算每個分片的MD5
            long fileSize = Files.size(path);
            long totalNum = fileSize / partSize + (fileSize % partSize > 0 ? 1 : 0);
            List<FileInfo.PartInfo> parts = new ArrayList<>();
            for (int i = 1; i <= totalNum; i++) {
                long start = (i - 1) * partSize;
                long end = i * partSize - 1;
                if (i == totalNum) {
                    end = fileSize - 1;
                }
                // 計算bytes[]
                byte[] bytes = new byte[(int) (end - start + 1)];
                String partMd5 = DigestUtil.md5Hex(bytes);
                FileInfo.PartInfo partInfo = new FileInfo.PartInfo();
                partInfo.setCurrentNum(StrUtil.toString(i));
                partInfo.setPartMd5(partMd5);
                parts.add(partInfo);
            }

            FileInfo fileInfo = new FileInfo();
            fileInfo.setBucketName(bucketName);
            fileInfo.setObjectName(objectName);
            fileInfo.setUploadId(uploadId);
            fileInfo.setTotalNum(StrUtil.toString(totalNum));
            fileInfo.setFileMd5(fileMd5);
            fileInfo.setParts(parts);
            return fileInfo;
        } catch (Exception e) {
            log.error("計算分片MD5時發生錯誤: " + e.getMessage());
        }
        return null;
    }

    /**
     * 上傳分片，僅支持分片小於5mb的分片
     *
     * @param tmpPath       分片上傳的臨時路徑，若不指定，則默認在tmpFilePart/uploadId/下
     * @param fileInfo 文件詳細信息
     * @param files         分片文件列表
     * @param Md5           是否開啟md5校驗，不開啟為普通分片上傳，開啟為斷點續傳上傳
     * @return true/false
     * minioFileInfo和file，需要使用RequestPart注解
     * fileInfo 需要content-type為application/json
     */
    public Boolean multipartUpload(String tmpPath, FileInfo fileInfo, List<MultipartFile> files , Map<String, String> userMetadata, Boolean Md5) {
        List<FileInfo.PartInfo> parts = fileInfo.getParts();
        if (CollectionUtil.isEmpty(parts) || CollectionUtil.isEmpty(files) || parts.size() != files.size()) {
            log.error("分片信息與分片文件數量不匹配");
            return false;
        }
        for (int i = 0; i < files.size(); i++) {
            try (InputStream inputStream = files.get(i).getInputStream()) {
                String tempPartName = tmpPath + "/" + fileInfo.getUploadId() + "/part-" + parts.get(i).getCurrentNum();
                if (Md5) {
                    String filePartMd5 = DigestUtil.md5Hex(inputStream);
                    String partMd5 = parts.get(i).getPartMd5();

                    // 檢驗分片MD5
                    if (!filePartMd5.equals(partMd5)) {
                        log.error("{}文件的分片{} MD5 異常，請檢查", fileInfo.getObjectName(), parts.get(i).getCurrentNum());
                        return false;
                    }

                    // 檢驗分片是否已上傳

                    if (isObjectExists(fileInfo.getBucketName(), tempPartName)) {
                        log.info("{}文件的分片{}已存在，無需上傳", fileInfo.getObjectName(), parts.get(i).getCurrentNum());
                        continue;
                    }
                }
                log.info("開始上傳 {} 文件的分片{}", fileInfo.getObjectName(), parts.get(i).getCurrentNum());
                userMetadata.put("Upload-Id", fileInfo.getUploadId());
                userMetadata.put("Total-Num", fileInfo.getTotalNum());
                userMetadata.put("Current-Num", parts.get(i).getCurrentNum());
                userMetadata.put("Part-Md5", parts.get(i).getPartMd5());

                PutObjectArgs builder = PutObjectArgs.builder()
                        .bucket(fileInfo.getBucketName())
                        .object(tempPartName)
                        .stream(files.get(i).getInputStream(), files.get(i).getSize(), -1)
                        .userMetadata(userMetadata)
                        .contentType(files.get(i).getContentType())
                        .build();

                minioClient.putObject(builder);
                log.info("分片{}上傳成功", parts.get(i).getCurrentNum());
            } catch (Exception e) {
                log.error(StrUtil.format("{}文件的分片{}上傳文件時發生錯誤: {}", fileInfo.getObjectName(), parts.get(i).getCurrentNum(), e.getMessage()));
                return false;
            }
        }
        log.info("{}文件的分片上傳完成", fileInfo.getObjectName());
        return true;
    }
    public Boolean multipartUpload(FileInfo fileInfo, List<MultipartFile> files) {
        return multipartUpload("tmpFilePart", fileInfo, files,new HashMap<>(), false);
    }
    public Boolean multipartUpload(FileInfo fileInfo, List<MultipartFile> files, Boolean Md5) {
        return multipartUpload("tmpFilePart", fileInfo, files, new HashMap<>(),Md5);
    }

    /**
     * 合併分片上傳
     *
     * @param tmpPath       分片上傳的臨時路徑，若不指定，則默認在tmpFilePart/uploadId/下
     * @param fileInfo 文件詳細信息
     * @param Md5           是否開啟md5校驗，不開啟為普通分片合併，開啟可啟動秒傳
     *                      開啟md5多一次api調用，但可以保證上傳的完整性
     * @return
     */
    public Boolean composeMultipartUpload(String tmpPath, FileInfo fileInfo, Map<String, String> userMetadata, Boolean Md5) {
        try {
            List<Item> items = listObjects(fileInfo.getBucketName(), tmpPath + "/" + fileInfo.getUploadId());

            // 檢查目標對象是否已經存在
            if (Md5) {
                StatObjectResponse object = getObject(fileInfo.getBucketName(), fileInfo.getObjectName());
                String calculatedEtag = object.userMetadata().get("etag");
                if (object.etag().equals(calculatedEtag)) {
                    log.info("文件已存在且MD5匹配，無需合併");
                    return true; // 直接返回，表示無需再次合併,秒傳
                }
            }

            // 查詢分片對象
            if (Convert.toInt(fileInfo.getTotalNum()) != items.size()) {
                log.error(String.format("分片數量不一致，合併失敗，提供分片數量：%s，查詢到分片數量：%s", fileInfo.getTotalNum(), items.size()));
                return false;
            }
            List<String> partMd5List = fileInfo.getParts().stream()
                    .map(item -> item.getPartMd5())
                    .collect(Collectors.toList());
            String calculatedEtag = calculateETag(partMd5List, partMd5List.size());
            userMetadata.put("ETag", calculatedEtag);
            log.info("合併對象不存在，開始合併分片");

            // 構建ComposeObjectArgs
            List<ComposeSource> sources = items.stream()
                    .map(item -> ComposeSource.builder()
                            .bucket(fileInfo.getBucketName())
                            .object(item.objectName())
                            .build())
                    .collect(Collectors.toList());

            ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                    .bucket(fileInfo.getBucketName())
                    .object(fileInfo.getObjectName())
                    .sources(sources)
                    .userMetadata(userMetadata)
                    .build();
            // 合併分片
            minioClient.composeObject(composeObjectArgs);
            // 刪除臨時文件
            if (deleteObject(fileInfo.getBucketName(), items.stream().map(Item::objectName).collect(Collectors.toList()))) {
                log.info("合併分片成功，刪除臨時分片成功");
            }
            return true;
        } catch (Exception e) {
            log.error("合併分片上傳文件時發生錯誤: " + e.getMessage());
        }
        return false;
    }
    public Boolean composeMultipartUpload(FileInfo fileInfo, Boolean Md5) {
        return composeMultipartUpload("tmpFilePart", fileInfo, new HashMap<>(), Md5);
    }
    public Boolean composeMultipartUpload(FileInfo fileInfo) {
        return composeMultipartUpload("tmpFilePart", fileInfo, new HashMap<>(), false);
    }

    /**
     * 計算ETag
     * 公式為ETag = md5(part1md5+part2md5+...)-n
     *
     * @param partMd5List 分片的md5列表
     * @param n           分片數量
     * @return ETag
     * 解釋：Etag為文件內容的校驗碼，用於校驗文件完整性
     */
    public String calculateETag(List<String> partMd5List, int n) {
        // 連接所有分片的MD5字節數組
        byte[] combinedMd5 = new byte[16 * n];
        for (int i = 0; i < n; i++) {
            byte[] partMd5 = HexUtil.decodeHex(partMd5List.get(i));
            System.arraycopy(partMd5, 0, combinedMd5, i * partMd5.length, partMd5.length);
        }

        // 計算合併後的MD5值
        byte[] finalMd5 = DigestUtil.md5(combinedMd5);

        // 轉換為十六進制字符串
        String hexMd5 = HexUtil.encodeHexStr(finalMd5);

        // 最終的ETag格式
        return hexMd5 + "-" + n;
    }
    /****************************************/

    /**
     * 獲取檔案的 InputStream
     */
    public InputStream getObjectInputStream(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 獲取檔案內容字串 (以 UTF-8 讀取)
     */
    public String getObjectAsString(String bucketName, String objectName) throws Exception {
        try (InputStream inputStream = getObjectInputStream(bucketName, objectName)) {
            return cn.hutool.core.io.IoUtil.readUtf8(inputStream);
        }
    }

    /********************* 下載操作 **********/
    /**
     * 簡單下載，直接下載單文件
     *
     * @param objectName 文件名
     * @param bucketName 桶名
     * @param response   響應
     * @return true/false
     */
    public void downloadFile(String bucketName, String objectName, HttpServletResponse response) {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
             OutputStream outputStream = response.getOutputStream()) {

            // 設置響應頭
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + objectName + "\"");

            // 將inputStream的數據複製到outputStream
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (Exception e) {
            log.error("下載文件時發生錯誤: " + e.getMessage(), e);
        }
    }

    /**
     * 下載文件到本地指定位置
     *
     * @param objectName 文件名
     * @param bucketName 桶名
     * @param filePath   本地路徑，複雜路徑需要用URLUtil.encode(filePath, CharsetUtil.CHARSET_UTF_8)處理才能當參數傳遞
     * @param override   是否覆蓋本地文件
     * @return true/false
     * 解釋：不提供bucketName，默認使用當前yml配置的桶
     */
    public Boolean downloadFile(String filePath, String bucketName, String objectName, Boolean override) {
        try {
            DownloadObjectArgs build = DownloadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(filePath)
                    .overwrite(override)
                    .build();
            minioClient.downloadObject(build);
            log.info("下載文件成功");
            return true;
        } catch (Exception e) {
            log.error("下載文件時發生錯誤: " + e.getMessage());
        }
        return false;
    }
    public Boolean downloadFile(String filePath, String bucketName,String objectName) {
        return downloadFile(filePath, bucketName, objectName, false);
    }

    /**
     * 斷點下載，根據請求頭的range下載
     * <img src="images/range.png"/>
     *
     * @param bucketName
     * @param objectName
     * @param request
     * @param response
     */
    public void checkpointDownload(String bucketName, String objectName, HttpServletRequest request, HttpServletResponse response) {
        long objectSize = 0;
        if (getObject(bucketName, objectName) != null) {
            objectSize = getObject(bucketName, objectName).size();
        } else {
            log.error("對象不存在: " + objectName);
        }
        String rangeHeader = request.getHeader("Range");
        try {
            long start = 0;
            long end = Long.MAX_VALUE;

            if (rangeHeader != null) {
                Pattern pattern = Pattern.compile("bytes=(\\d*)-(\\d*)");
                Matcher matcher = pattern.matcher(rangeHeader);
                if (matcher.matches()) {
                    String startPart = matcher.group(1);
                    String endPart = matcher.group(2);

                    if (!startPart.isEmpty()) {
                        start = Long.parseLong(startPart);
                    }
                    if (!endPart.isEmpty()) {
                        end = Long.parseLong(endPart);
                    }
                    if (end > objectSize - 1) {
                        end = objectSize - 1;
                    }
                }
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            }

            response.setHeader("Content-Type", "application/octet-stream");
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + objectSize);

            try (InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .offset(start)
                            .length(end - start + 1)
                            .build());
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            log.error("下載文件時發生錯誤: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    /****************************************/


}
