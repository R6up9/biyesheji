package com.badminton.controller;

import com.badminton.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/user")
public class MenuController {

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("id", "1");
        info.put("username", "admin");
        info.put("email", "admin@maoqiu.com");
        info.put("phone", "13800000000");
        info.put("avatar", "");
        return Result.success(info);
    }

    @GetMapping("/menu")
    public Result<List<Map<String, Object>>> getMenu() {
        List<Map<String, Object>> menuList = new ArrayList<>();

        menuList.add(createMenu("home", "/home/index", "/home/index", "首页", "HomeFilled", true, null));
        menuList.add(createMenu("account", "/account", null, "账号管理", "UserFilled", false, Arrays.asList(
                createChildMenu("accountList", "/account/list", "/account/list/index", "管理员列表")
        )));
        menuList.add(createMenu("member", "/member", null, "用户管理", "User", false, Arrays.asList(
                createChildMenu("memberList", "/member/list", "/member/list/index", "会员列表")
        )));
        menuList.add(createMenu("coach", "/coach", null, "教练管理", "Medal", false, Arrays.asList(
                createChildMenu("coachList", "/coach/list", "/coach/list/index", "教练列表")
        )));
        menuList.add(createMenu("court", "/court", null, "场地管理", "Location", false, Arrays.asList(
                createChildMenu("courtList", "/court/list", "/court/list/index", "场地列表")
        )));
        menuList.add(createMenu("course", "/course", null, "课程管理", "Calendar", false, Arrays.asList(
                createChildMenu("courseList", "/course/list", "/course/list/index", "课程列表")
        )));
        menuList.add(createMenu("product", "/product", null, "商品管理", "ShoppingCart", false, Arrays.asList(
                createChildMenu("productList", "/product/list", "/product/list/index", "商品列表")
        )));
        menuList.add(createMenu("activity", "/activity", null, "活动管理", "Tickets", false, Arrays.asList(
                createChildMenu("activityList", "/activity/list", "/activity/list/index", "活动列表")
        )));
        menuList.add(createMenu("dataScreen", "/dataScreen", "/dataScreen/index", "数据大屏", "DataLine", true, null));

        return Result.success(menuList);
    }

    @GetMapping("/buttons")
    public Result<Map<String, Object>> getButtons() {
        Map<String, Object> buttons = new HashMap<>();
        return Result.success(buttons);
    }

    private Map<String, Object> createMenu(String name, String path, String component, String title, String icon, boolean isAffix, List<Map<String, Object>> children) {
        Map<String, Object> menu = new LinkedHashMap<>();
        menu.put("path", path);
        menu.put("name", name);
        menu.put("component", component);
        menu.put("meta", createMeta(title, icon, isAffix));
        if (children != null) {
            menu.put("children", children);
        }
        return menu;
    }

    private Map<String, Object> createChildMenu(String name, String path, String component, String title) {
        Map<String, Object> child = new LinkedHashMap<>();
        child.put("path", path);
        child.put("name", name);
        child.put("component", component);
        child.put("meta", createMeta(title, "", false));
        return child;
    }

    private Map<String, Object> createMeta(String title, String icon, boolean isAffix) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("title", title);
        meta.put("icon", icon);
        meta.put("isLink", "");
        meta.put("isHide", false);
        meta.put("isFull", false);
        meta.put("isAffix", isAffix);
        meta.put("isKeepAlive", true);
        return meta;
    }
}
