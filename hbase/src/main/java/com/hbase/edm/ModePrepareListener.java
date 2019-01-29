package com.hbase.edm;

import com.hbase.core.Htable;
import com.hbase.core.TableInitDelegate;
import com.hbase.core.TableManager;
import com.hbase.exception.InitException;
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

    private static final TableManager TABLE_MANAGER = new TableManager();

    @Override
    public void onEvent(ModelPrepareEvent event) {
        Collection<Htable> htables = event.getSource();
        log.info("开始表结构初始化");
        CountDownLatch count = new CountDownLatch(htables.size());
        Map<Htable, Boolean> results = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(htables.size());
        htables.forEach(table -> {
            Future<Boolean>
                    result = executorService.submit(() -> TABLE_MANAGER.initTable(table, count::countDown));
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
        TABLE_MANAGER.closeAdmin();
        log.info("表结构初始化完成");
        for (Map.Entry<Htable, Boolean> entry : results.entrySet()){
            if (!entry.getValue()){
                throw new InitException(entry.getKey().getTableName().get());
            }
        }
        executorService.shutdown();
    }
}
