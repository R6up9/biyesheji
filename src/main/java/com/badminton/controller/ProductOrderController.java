package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badminton.common.Result;
import com.badminton.entity.Product;
import com.badminton.entity.ProductOrder;
import com.badminton.mapper.ProductMapper;
import com.badminton.mapper.ProductOrderMapper;
import com.badminton.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class ProductOrderController {

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PaymentService paymentService;

    /**
     * 客户端：创建订单（购买商品）
     */
    @PostMapping("/client/order")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> params,
                                                    HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("x-user-id");
        String userName = (String) request.getAttribute("x-username");

        System.out.println("===== createOrder called =====");
        System.out.println("userId: " + userId + ", userName: " + userName);
        System.out.println("params: " + params);

        if (userId == null) {
            return Result.error("请先登录");
        }

        Integer productId = (Integer) params.get("productId");
        Integer quantity = (Integer) params.get("quantity");
        String address = (String) params.get("address");
        String phone = (String) params.get("phone");
        String receiver = (String) params.get("receiver");
        String remark = (String) params.get("remark");

        if (productId == null || quantity == null || quantity <= 0) {
            return Result.error("参数错误");
        }

        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            return Result.error("商品不存在或已下架");
        }

        if (product.getStock() < quantity) {
            return Result.error("库存不足");
        }

        String orderNo = generateOrderNo();
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));

        // 检查余额并扣款
        BigDecimal currentBalance = paymentService.getBalance(userId);
        if (currentBalance.compareTo(totalAmount) < 0) {
            return Result.error("余额不足！当前余额：" + currentBalance + "，请联系管理员充值");
        }

        boolean deductSuccess = paymentService.deductBalance(userId, totalAmount);
        if (!deductSuccess) {
            return Result.error("扣款失败，请联系管理员");
        }

        ProductOrder order = new ProductOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setUserName(userName != null ? userName : "用户" + userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setProductImage(product.getImage());
        order.setQuantity(quantity);
        order.setPrice(product.getPrice());
        order.setTotalAmount(totalAmount);
        order.setStatus(1); // 已付款
        order.setAddress(address);
        order.setPhone(phone);
        order.setReceiver(receiver);
        order.setRemark(remark);

        productOrderMapper.insert(order);

        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);

        System.out.println("Order created: " + orderNo + ", amount: " + totalAmount);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", orderNo);
        data.put("totalAmount", totalAmount);
        data.put("remainingBalance", paymentService.getBalance(userId));

        return Result.success(data);
    }

    /**
     * 客户端：查询我的订单
     */
    @GetMapping("/client/order/list")
    public Result<Page<ProductOrder>> myOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {

        Integer userId = (Integer) request.getAttribute("x-user-id");
        if (userId == null) {
            return Result.error("请先登录");
        }

        Page<ProductOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductOrder::getUserId, userId);

        if (status != null) {
            wrapper.eq(ProductOrder::getStatus, status);
        }

        wrapper.orderByDesc(ProductOrder::getCreateTime);

        return Result.success(productOrderMapper.selectPage(page, wrapper));
    }

    /**
     * 客户端：取消订单
     */
    @PutMapping("/client/order/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Integer orderId,
                                     HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("x-user-id");
        if (userId == null) {
            return Result.error("请先登录");
        }

        ProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        if (order.getStatus() != 0 && order.getStatus() != 1) {
            return Result.error("只能取消待付款或已付款订单");
        }

        // 如果是已付款订单，需要退款
        if (order.getStatus() == 1) {
            boolean refundSuccess = paymentService.refundBalance(userId, order.getTotalAmount());
            if (!refundSuccess) {
                return Result.error("退款失败，请联系管理员");
            }
        }

        order.setStatus(4);
        productOrderMapper.updateById(order);

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStock(product.getStock() + order.getQuantity());
            productMapper.updateById(product);
        }

        return Result.success();
    }

    /**
     * 客户端：确认收货（完成订单）
     */
    @PutMapping("/client/order/{orderId}/complete")
    public Result<Void> completeOrder(@PathVariable Integer orderId,
                                       HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("x-user-id");
        if (userId == null) {
            return Result.error("请先登录");
        }

        ProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        if (order.getStatus() != 2) {
            return Result.error("只能确认已发货订单");
        }

        order.setStatus(3);
        productOrderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 管理端：查询所有订单
     */
    @GetMapping("/admin/order/list")
    public Result<Page<ProductOrder>> orderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo) {

        Page<ProductOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductOrder> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(ProductOrder::getStatus, status);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(ProductOrder::getOrderNo, orderNo.trim());
        }

        wrapper.orderByDesc(ProductOrder::getCreateTime);

        return Result.success(productOrderMapper.selectPage(page, wrapper));
    }

    /**
     * 管理端：更新订单状态（发货等）
     */
    @PutMapping("/admin/order/{orderId}/status")
    public Result<Void> updateOrderStatus(@PathVariable Integer orderId,
                                           @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        if (status == null) {
            return Result.error("请指定订单状态");
        }

        ProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        order.setStatus(status);
        productOrderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 管理端：获取订单详情
     */
    @GetMapping("/admin/order/{orderId}")
    public Result<ProductOrder> getOrderDetail(@PathVariable Integer orderId) {
        ProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + timestamp + random;
    }
}
