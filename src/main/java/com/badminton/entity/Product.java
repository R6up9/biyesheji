package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    /** 1-器材, 2-服装, 3-配件 */
    private Integer type;

    private BigDecimal price;

    private Integer stock;

    private String image;

    private String description;

    /** 0-下架, 1-上架 */
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", price=" + price +
                ", stock=" + stock +
                ", image='" + image + '\'' +
                ", description='" + (description != null ? description.substring(0, Math.min(20, description.length())) : "null") + '\'' +
                ", status=" + status +
                '}';
    }
}
