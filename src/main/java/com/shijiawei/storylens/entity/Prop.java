package com.shijiawei.storylens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prop")
public class Prop {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long novelId;
    private String name;
    private String appearanceDescription;
    private String imageUrl;
    
    @TableLogic
    private Integer isDeleted;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
