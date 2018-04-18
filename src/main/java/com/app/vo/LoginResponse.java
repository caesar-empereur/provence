package com.app.vo;

import io.swagger.annotations.ApiModel;

/**
 * Created by leon on 2018/4/11.
 */
@ApiModel(value = "登录返回")
public class LoginResponse {
    
    private boolean succeed;
    
    private String token;
    
    public boolean isSucceed() {
        return succeed;
    }
    
    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
}
