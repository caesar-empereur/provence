package com.hbase.repository;

import com.hbase.reflection.HbaseEntity;
import lombok.Data;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/5.
 *
 * T entity
 * R Repository
 * Rk Rowkey
 */
@Data
public class HbaseRepositoryInfo<T, R, RK> {

    private Class<R> repositoryClass;

    private HbaseEntity<T, RK> hbaseEntity;
}
