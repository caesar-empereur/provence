package com.hbase.repository;

import com.hbase.reflection.HbaseEntity;
import lombok.Data;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/5.
 */
@Data
public class HbaseRepositoryInfo<T, R, ID> {

    private Class<R> repositoryClass;

    private HbaseEntity<T, ID> entityInformation;
}
