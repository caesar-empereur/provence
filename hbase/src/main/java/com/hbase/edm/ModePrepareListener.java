package com.hbase.edm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

import com.hbase.reflection.HbaseEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hbase.core.TableManager;
import com.hbase.exception.InitException;

/**
 * Created by yang on 2019/1/27.
 */
public class ModePrepareListener implements EventListener<ModelPrepareEvent> {

    private static final Log log = LogFactory.getLog(ModePrepareListener.class);

    private static final Supplier<TableManager> TABLE_MANAGER_SUPPLIER = TableManager::new;

    @Override
    public void onEvent(ModelPrepareEvent event) {
        Collection<HbaseEntity> htables = event.getSource();
        log.info("开始表结构初始化");

        TableManager tableManager = TABLE_MANAGER_SUPPLIER.get();

        CountDownLatch count = new CountDownLatch(htables.size());
        Map<HbaseEntity, Boolean> results = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(htables.size());

        try {
            for (HbaseEntity entityInfo : htables) {
                Future<Boolean> result = executorService.submit(() -> tableManager.initTable(entityInfo,
                                                                                           count::countDown));
                results.put(entityInfo, result.get());
            }
            count.await();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        tableManager.closeAdmin();
        executorService.shutdown();
        
        log.info("表结构初始化完成");
        for (Map.Entry<HbaseEntity, Boolean> entry : results.entrySet()){
            if (!entry.getValue()){
                throw new InitException(entry.getKey().getTableName());
            }
        }
    }
}
