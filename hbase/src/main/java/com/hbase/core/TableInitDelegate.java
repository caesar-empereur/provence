package com.hbase.core;

import com.hbase.exception.InitException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public final class TableInitDelegate {

//    private static final Log log = LogFactory.getLog(TableInitDelegate.class);
//
//    private static final TableManager tableManager = new TableManager();
    
    public static void initHbaseTable(Collection<Htable> htables) {
//        log.info("开始表结构初始化");
//        CountDownLatch count = new CountDownLatch(htables.size());
//        Map<Htable, Boolean> results = new HashMap<>();
//        ExecutorService executorService = Executors.newFixedThreadPool(htables.size());
//        htables.forEach(table -> {
//            Future<Boolean> result = executorService.submit(() -> tableManager.initTable(table, count::countDown));
//            try {
//                results.put(table, result.get());
//            }
//            catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//        });
//        try {
//            count.await();
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        tableManager.closeAdmin();
//        log.info("表结构初始化完成");
//        for (Map.Entry<Htable, Boolean> entry : results.entrySet()){
//            if (!entry.getValue()){
//                throw new InitException(entry.getKey().getTableName().get());
//            }
//        }
    }
    
}
