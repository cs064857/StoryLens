package com.shijiawei.storylens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("novel")
public class Novel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String author;
    private String description;
    private String filePath;
    private String fileType;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
