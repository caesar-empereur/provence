package com.hbase.core;

import com.hbase.exception.InitException;

import java.util.*;
import java.util.concurrent.*;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public final class TableInitDelegate {
    
    private static final TableManager tableManager = new TableManager();
    
    public static void initHbaseTable(Collection<Htable> htables) {
        CountDownLatch count = new CountDownLatch(htables.size());
        Map<Htable, Boolean> results = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(htables.size());
        htables.forEach(table -> {
            Future<Boolean> result = executorService.submit(() -> tableManager.initTable(table, count::countDown));
            try {
                results.put(table, result.get());
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        try {
            count.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Map.Entry<Htable, Boolean> entry : results.entrySet()){
            if (entry.getValue()){
                throw new InitException(entry.getKey().getTableName());
            }
        }
    }
    
}
