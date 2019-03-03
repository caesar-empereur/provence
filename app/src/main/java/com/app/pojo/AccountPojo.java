package com.app.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
@Data
public class AccountPojo extends StringId{

    private String username;

    private Double balance;
}
