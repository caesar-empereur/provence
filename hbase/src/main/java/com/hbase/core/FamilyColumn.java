package com.hbase.core;

import lombok.Data;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/5.
 */
@Data
public class FamilyColumn {

    private String columnName;

    private String familyName;

    private Class columnType;

}
