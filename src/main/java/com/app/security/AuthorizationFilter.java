package com.app.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by pc on 2018/3/23.
 */
public class AuthorizationFilter implements Filter {
    
    private Log log = LogFactory.getLog(this.getClass());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }
    
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException,
                                            ServletException {
        log.info("权限过滤");
        HttpServletResponse httpServletResponse =
                                                (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader("auth");
        if (StringUtils.isBlank(token)) {
            Cookie[] cookies = httpServletRequest.getCookies();
            for (Cookie cookie : cookies) {
                if ("auth".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
            if (StringUtils.isBlank(token)) {
                httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }
        
        if (token.equals("123")) {
            chain.doFilter(request, response);
        }
        else {
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
    
    @Override
    public void destroy() {
        
    }
}
