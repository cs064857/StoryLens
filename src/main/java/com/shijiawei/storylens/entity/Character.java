package com.shijiawei.storylens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("`character`")
public class Character {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long novelId;
    private String name;
    private String gender;
    private String age;
    private String appearanceDescription;
    private String imageUrl;
    
    @TableLogic
    private Integer isDeleted;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
