package com.hbase.edm;

import com.hbase.core.Htable;
import com.hbase.core.TableInitDelegate;
import com.hbase.core.TableManager;
import com.hbase.exception.InitException;
import com.hbase.reflection.HbaseEntityInformation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Created by yang on 2019/1/27.
 */
public class ModePrepareListener implements EventListener<ModelPrepareEvent> {

    private static final Log log = LogFactory.getLog(TableInitDelegate.class);

    private static final Supplier<TableManager> TABLE_MANAGER_SUPPLIER = TableManager::new;

    @Override
    public void onEvent(ModelPrepareEvent event) {
        Collection<HbaseEntityInformation> htables = event.getSource();
        log.info("开始表结构初始化");

        TableManager tableManager = TABLE_MANAGER_SUPPLIER.get();

        CountDownLatch count = new CountDownLatch(htables.size());
        Map<HbaseEntityInformation, Boolean> results = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(htables.size());
        htables.forEach(entityInfo -> {
            Future<Boolean>
                    result = executorService.submit(() -> tableManager.initTable(entityInfo, count::countDown));
            try {
                results.put(entityInfo, result.get());
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
        tableManager.closeAdmin();
        log.info("表结构初始化完成");
        for (Map.Entry<HbaseEntityInformation, Boolean> entry : results.entrySet()){
            if (!entry.getValue()){
                throw new InitException(entry.getKey().getTableName());
            }
        }
        executorService.shutdown();
    }
}
