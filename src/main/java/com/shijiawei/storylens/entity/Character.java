package com.shijiawei.storylens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("`character`")
public class Character {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long novelId;
    private String name;
    private String gender;
    private String age;
    private String appearanceDescription;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
