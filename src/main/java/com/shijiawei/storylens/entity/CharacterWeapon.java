package com.shijiawei.storylens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("character_weapon")
public class CharacterWeapon {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long characterId;
    private Long weaponId;
    
    @TableLogic
    private Integer isDeleted;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
