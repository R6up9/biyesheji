package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badminton.common.Result;
import com.badminton.entity.Product;
import com.badminton.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/product")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        
        System.out.println("ProductController.list called: pageNum=" + pageNum + ", pageSize=" + pageSize + ", name=" + name + ", status=" + status + ", type=" + type);
        
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Product::getName, name.trim());
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        if (type != null) {
            wrapper.eq(Product::getType, type);
        }
        
        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> resultPage = productMapper.selectPage(page, wrapper);
        
        System.out.println("Query result: total=" + resultPage.getTotal() + ", records size=" + resultPage.getRecords().size());
        for (Product product : resultPage.getRecords()) {
            System.out.println("Product: " + product.getName() + ", image: " + product.getImage());
        }
        
        return Result.success(resultPage);
    }

    @GetMapping("/all")
    public Result<java.util.List<Product>> all() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.orderByDesc(Product::getCreateTime);
        return Result.success(productMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Integer id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Product product) {
        System.out.println("ProductController.add received: " + product);
        System.out.println("  - name: " + product.getName());
        System.out.println("  - image: " + product.getImage());
        
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return Result.error("商品名称不能为空");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return Result.error("请设置商品价格");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            return Result.error("请设置商品库存");
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        
        productMapper.insert(product);
        System.out.println("Product inserted, id: " + product.getId());
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        System.out.println("ProductController.update received: " + product);
        System.out.println("  - id: " + product.getId());
        System.out.println("  - image: " + product.getImage());
        
        if (product.getId() == null) {
            return Result.error("商品ID不能为空");
        }
        productMapper.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        productMapper.deleteById(id);
        return Result.success();
    }
}
