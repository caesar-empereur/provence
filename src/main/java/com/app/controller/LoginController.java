package com.app.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.app.vo.LoginResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by pc on 2018/4/10.
 */
@Api(description = "登录接口")
@RestController
@RequestMapping("/login")
public class LoginController {
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    @ApiOperation(value = "日志")
    @RequestMapping(value = "/username", method = RequestMethod.POST)
    public LoginResponse login(@RequestParam("name") String name,
                               @RequestParam("password") String password,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if (StringUtils.isAnyBlank(name, password)) {
            throw new RuntimeException("用户名或者密码错误");
        }
        LoginResponse loginResponse = new LoginResponse();
        if (name.equals("hehe") && password.equals("123456")) {
            Cookie[] cookies = request.getCookies();
            if (cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if ("auth".equals(cookie.getName())) {
                        cookie.setMaxAge(0);
                    }
                }
            }
            response.addCookie(new Cookie("auth", "123"));
            loginResponse.setSucceed(true);
            loginResponse.setToken("sdsd");
            return loginResponse;
        }
        throw new RuntimeException("用户名或者密码错误");
    }
}
