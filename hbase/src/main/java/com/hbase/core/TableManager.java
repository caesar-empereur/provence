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

    public Boolean initTable(Htable htable, InitFinishedCallback callback) {
        Admin admin = null;
        Connection connection = connectionProvider.getConnection();
        TableName tableName = TableName.valueOf(htable.getTableName());
        try {
            admin = connection.getAdmin();
            if (tableNames == null || tableNames.size()==0){
                tableNames = Arrays.asList(admin.listTableNames());
            }
            if (tableNames.contains(tableName)){
                callback.finish();
                return true;
            }
            TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(tableName).build();

            Map<String, Set<String>> columnWithFamily = htable.getColumnsWithFamily();
            if (columnWithFamily != null && columnWithFamily.size() >= 0
                && tableDescriptor instanceof TableDescriptorBuilder.ModifyableTableDescriptor) {
                for (Map.Entry<String, Set<String>> entry : columnWithFamily.entrySet()) {
                    ColumnFamilyDescriptor columnFamilyDescriptor = ColumnFamilyDescriptorBuilder.of(entry.getKey());
                    ((TableDescriptorBuilder.ModifyableTableDescriptor) tableDescriptor).setColumnFamily(columnFamilyDescriptor);
                }
            }
            admin.createTable(tableDescriptor);
        }
        catch (IOException e) {
            log.error(e);
            return false;
        }
        finally {
            try {
                if (admin != null) {
                    admin.close();
                }
                connectionProvider.recycleConnection(connection);
            }
            catch (IOException e) {
                log.error(e);
            }
        }
        callback.finish();
        return true;
    }
}
