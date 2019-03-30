package com.app.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * Created by yingyang on 15/11/19.
 */
@Data
public class StringId implements Serializable {

    private String id;

    private Date createTime;
}
