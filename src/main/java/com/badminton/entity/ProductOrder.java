package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_product_order")
public class ProductOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Integer userId;

    /** 用户姓名 */
    private String userName;

    /** 商品ID */
    private Integer productId;

    /** 商品名称 */
    private String productName;

    /** 商品图片 */
    private String productImage;

    /** 购买数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal price;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 订单状态: 0-待付款, 1-已付款, 2-已发货, 3-已完成, 4-已取消 */
    private Integer status;

    /** 收货地址 */
    private String address;

    /** 联系电话 */
    private String phone;

    /** 收货人 */
    private String receiver;

    /** 备注 */
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
