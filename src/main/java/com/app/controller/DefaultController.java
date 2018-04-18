package com.app.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by pc on 2018/3/22.
 */
@Api(description = "默认接口")
@RestController
@RequestMapping("/guest")
public class DefaultController {

    Log LOG = LogFactory.getLog(this.getClass());

    @ApiOperation(value = "日志")
    @RequestMapping(value = "/log",method = RequestMethod.GET)
    public void log(){
        LOG.info("guest");
    }
}
