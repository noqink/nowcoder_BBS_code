package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                      "/user/setting","/user/upload","/user/updatePassword",
                        "/discuss/add", "/comment/add/**", "/message/**",
                        "like", "follow", "unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER, AUTHORITY_MODERATOR, AUTHORITY_ADMIN
                )
                .anyRequest().permitAll();

        // 权限冲突处理 普通请求(返回html)和异步请求(返回json)
        http.exceptionHandling()
                // 未登录处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        // 分普通和异步请求
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)){
                            // 异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "用户未登录!"));
                        }else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                // 登录处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        // 分普通和异步请求
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)){
                            // 异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "用户权限不足"));
                        }else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
        // SpringSecurity底层默认拦截/logout请求,并运行其logout逻辑
        // SpringSecurity为Filter filter在controller之前
        // 想要运行自己的/logout 覆盖其默认逻辑 欺骗
        http.logout().logoutUrl("/securitylogout");
    }
}
