package com.shijiawei.storylens.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: FileInfo
 * Description:
 *
 * @Create 2026/3/21 下午11:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {

    /**
     * 桶名稱
     */
    private String bucketName;

    /**
     * 文件名稱
     */
    private String objectName;

    /**
     * 原始文件md5值
     */
    private String fileMd5;

    /**
     * 需要上下傳文件id(s3使用)
     */
    private String uploadId;

    /**
     * 分片上傳的url
     */
    private String uploadUrl;


    /**
     * 總分片
     */
    private String totalNum;

    /**
     * 分片信息
     */
    private List<PartInfo> parts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PartInfo {
        /**
         * 分片文件md5值
         */
        private String partMd5;

        /**
         * 當前分片
         */
        private String currentNum;
    }

}