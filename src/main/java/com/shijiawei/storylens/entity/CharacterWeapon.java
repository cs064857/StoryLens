package com.shijiawei.storylens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("character_weapon")
public class CharacterWeapon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long characterId;
    private Long weaponId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
