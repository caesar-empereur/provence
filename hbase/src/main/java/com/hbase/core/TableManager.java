package com.hbase.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.hbase.exception.OperationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import com.hbase.pool.ConnectionProvider;
import com.hbase.pool.hibernate.ConnectionPoolManager;
import com.hbase.reflection.HbaseEntity;

//import org.apache.hadoop.hbase.client.TableDescriptor;
//import org.apache.hadoop.hbase.client.TableDescriptorBuilder;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public class TableManager {
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    private Supplier<ConnectionProvider<Connection>> connectionProviderSupplier = ConnectionPoolManager::getInstance;
    
    private List<TableName> tableNames;
    
    private Admin admin = null;

    public void initTable(HbaseEntity hbaseEntity) {
        ConnectionProvider<Connection> connectionProvider = connectionProviderSupplier.get();

        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            TableName tableName = TableName.valueOf(hbaseEntity.getTableName());
            if (admin == null) {
                admin = connection.getAdmin();
            }
            if (tableNames == null || tableNames.size() == 0) {
                tableNames = Arrays.asList(admin.listTableNames());
            }
            if (tableNames.contains(tableName)) {
                return;
            }
            else {
                // 只需要处理 column family, column 在 Put 的时候指定
                List<FamilyColumn> familyColumnList = hbaseEntity.getFamilyColumnList();
                Set<String> familyList = familyColumnList.stream()
                                                         .map(FamilyColumn::getFamilyName)
                                                         .collect(Collectors.toSet());
                TableDescriptorBuilder.ModifyableTableDescriptor tableDescriptor =
                                                                                 (TableDescriptorBuilder.ModifyableTableDescriptor) TableDescriptorBuilder.newBuilder(tableName)
                                                                                                                                                          .build();
                for (String family : familyList) {
                    ColumnFamilyDescriptor columnFamilyDescriptor =
                                                                  ColumnFamilyDescriptorBuilder.of(family);
                    tableDescriptor.setColumnFamily(columnFamilyDescriptor);
                }
                //添加一个 family,用来存储 rowkey 的字段信息
                tableDescriptor.setColumnFamily(ColumnFamilyDescriptorBuilder.of("rowkey"));
                admin.createTable(tableDescriptor);
            }
        }
        catch (IOException e) {
            log.error(e.getMessage());
            throw new OperationException(e.getMessage());
        }
        connectionProvider.recycleConnection(connection);
    }
    
    public void closeAdmin() {
        if (admin != null) {
            try {
                admin.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
