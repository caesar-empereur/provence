package com.hbase.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import com.hbase.pool.ConnectionProvider;
import com.hbase.pool.hibernate.ConnectionPoolManager;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public class TableManager {
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    private ConnectionProvider connectionProvider = ConnectionPoolManager.getInstance();
    
    private List<TableName> tableNames;
    
    private Admin admin = null;
    
    public Boolean initTable(Htable htable, InitFinishedCallback callback) {
        boolean succeed;
        Connection connection = connectionProvider.getConnection();
        TableName tableName = TableName.valueOf(htable.getTableName());
        try {
            if (admin == null) {
                admin = connection.getAdmin();
            }
            if (tableNames == null || tableNames.size() == 0) {
                tableNames = Arrays.asList(admin.listTableNames());
            }
            if (tableNames.contains(tableName)) {
                succeed = true;
            }
            TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(tableName).build();
            
            Map<String, Set<String>> columnWithFamily = htable.getColumnsWithFamily();
            if (columnWithFamily != null && columnWithFamily.size() >= 0
                && tableDescriptor instanceof TableDescriptorBuilder.ModifyableTableDescriptor) {
                for (Map.Entry<String, Set<String>> entry : columnWithFamily.entrySet()) {
                    ColumnFamilyDescriptor columnFamilyDescriptor =
                                                                  ColumnFamilyDescriptorBuilder.of(entry.getKey());
                    ((TableDescriptorBuilder.ModifyableTableDescriptor) tableDescriptor).setColumnFamily(columnFamilyDescriptor);
                }
            }
            admin.createTable(tableDescriptor);
            succeed = true;
        }
        catch (IOException e) {
            log.error(e);
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
