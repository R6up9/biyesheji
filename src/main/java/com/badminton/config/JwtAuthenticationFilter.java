package com.badminton.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.entity.Member;
import com.badminton.entity.User;
import com.badminton.mapper.MemberMapper;
import com.badminton.mapper.UserMapper;
import com.badminton.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            token = request.getHeader("x-access-token");
        } else {
            token = token.substring(7);
        }
        if (token != null && !token.isEmpty()) {
            try {
                if (!jwtUtils.isTokenExpired(token)) {
                    Integer userId = jwtUtils.getUserIdFromToken(token);
                    Integer role = jwtUtils.getRoleFromToken(token);
                    String roleStr = role == 0 ? "ADMIN" : (role == 2 ? "COACH" : "MEMBER");
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleStr)));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 将用户信息存入request属性，供控制器使用
                    request.setAttribute("x-user-id", userId);
                    User user = userMapper.selectById(userId);
                    if (user != null) {
                        // 尝试从 member 表中获取 realName
                        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(Member::getUserId, userId);
                        Member member = memberMapper.selectOne(wrapper);
                        if (member != null && member.getRealName() != null) {
                            request.setAttribute("x-username", member.getRealName());
                        } else {
                            request.setAttribute("x-username", user.getUsername());
                        }
                    }
                }
            } catch (Exception e) {
                // Invalid token, continue without authentication
            }
        }
        filterChain.doFilter(request, response);
    }
}
