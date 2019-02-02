package com.hbase.core;

import com.hbase.reflection.HbaseEntityInformation;
import lombok.Data;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/5.
 */
@Data
public class HbaseRepositoryInfo<T, R, ID> {

    private Class<R> repositoryClass;

    private HbaseEntityInformation<T, ID> entityInformation;
}
