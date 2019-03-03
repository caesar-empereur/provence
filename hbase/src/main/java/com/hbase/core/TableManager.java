package com.hbase.core;

import com.hbase.pool.ConnectionProvider;
import com.hbase.pool.hibernate.ConnectionPoolManager;
import com.hbase.reflection.HbaseEntityInformation;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
@Slf4j
public class TableManager {
    
//    private final Log log = LogFactory.getLog(this.getClass());
    
    private Supplier<ConnectionProvider<Connection>> connectionProviderSupplier = ConnectionPoolManager::getInstance;
    
    private List<TableName> tableNames;
    
    private Admin admin = null;

    public Boolean initTable(HbaseEntityInformation htable, InitFinishedCallback callback) {
        ConnectionProvider<Connection> connectionProvider = connectionProviderSupplier.get();

        boolean succeed;
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            TableName tableName = TableName.valueOf(htable.getTableName());
            if (admin == null) {
                admin = connection.getAdmin();
            }
            if (tableNames == null || tableNames.size() == 0) {
                tableNames = Arrays.asList(admin.listTableNames());
            }
            if (tableNames.contains(tableName)) {
                succeed = true;
            } else {
                //只需要处理 column family, column 在 Put 的时候指定
                TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(tableName).build();
//                Map<String, Set<String>> columnWithFamily = htable.getColumnsWithFamily();
//                if (columnWithFamily != null && columnWithFamily.size() >= 0
//                    && tableDescriptor instanceof TableDescriptorBuilder.ModifyableTableDescriptor) {
//                    for (Map.Entry<String, Set<String>> entry : columnWithFamily.entrySet()) {
//                        ColumnFamilyDescriptor columnFamilyDescriptor =
//                                                                      ColumnFamilyDescriptorBuilder.of(entry.getKey());
//                        ((TableDescriptorBuilder.ModifyableTableDescriptor) tableDescriptor).setColumnFamily(columnFamilyDescriptor);
//                    }
//                }
                admin.createTable(tableDescriptor);
                succeed = true;
            }
        }
        catch (IOException e) {
            log.error(e.getMessage());
            succeed = false;
        }
        connectionProvider.recycleConnection(connection);
        callback.finish();
        return succeed;
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
