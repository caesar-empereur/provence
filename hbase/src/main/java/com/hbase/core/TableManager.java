package com.hbase.core;

import com.hbase.pool.ConnectionProvider;
import com.hbase.pool.hibernate.ConnectionPoolManager;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public class TableManager {
    
    private ConnectionProvider connectionProvider = new ConnectionPoolManager();
    
    public Boolean initTable(Htable htable, InitFinishedCallback callback) {
        Admin admin = null;
        Connection connection = connectionProvider.getConnection();
        TableName tableName = TableName.valueOf(htable.getTableName());
        try {
            admin = connection.getAdmin();
            if (admin.isTableAvailable(tableName)) {
                callback.finish();
                return true;
            }
            HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
            if (!CollectionUtils.isEmpty(htable.getColumnsWithFamily())) {
                Map<String, Set<String>> columnWithFamily = htable.getColumnsWithFamily();
                for (Map.Entry<String, Set<String>> entry : columnWithFamily.entrySet()) {
                    HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(entry.getKey());
                    tableDescriptor.addFamily(hColumnDescriptor);
                }
            }
            admin.createTable(tableDescriptor);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (admin != null) {
                    admin.close();
                }
                connectionProvider.recycleConnection(connection);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        callback.finish();
        return true;
    }
}
