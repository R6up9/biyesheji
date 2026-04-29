package com.badminton.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.entity.User;
import com.badminton.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "admin");
        User admin = userMapper.selectOne(wrapper);
        if (admin != null) {
            String correctHash = passwordEncoder.encode("123456");
            admin.setPassword(correctHash);
            userMapper.updateById(admin);
            System.out.println("=================================================");
            System.out.println("Admin password fixed. Hash: " + correctHash);
            System.out.println("Please login with admin / 123456");
            System.out.println("=================================================");
        } else {
            User newAdmin = new User();
            newAdmin.setUsername("admin");
            newAdmin.setPassword(passwordEncoder.encode("123456"));
            newAdmin.setRole(0);
            newAdmin.setStatus(1);
            userMapper.insert(newAdmin);
            System.out.println("=================================================");
            System.out.println("Admin account created. Please login with admin / 123456");
            System.out.println("=================================================");
        }
    }
}
