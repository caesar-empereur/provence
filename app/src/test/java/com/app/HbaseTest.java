package com.app;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/10/16.
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class HbaseTest {

    @SuppressWarnings("all")
    @Resource
    private OrderRecordHbaseRepository orderRecordHbaseRepository;

    @Test
    public void insert(){
        List<OrderRecord> orderRecordList = new ArrayList<>();
        for (int i=0;i<100;i++){
            OrderRecord orderRecord = new OrderRecord();
            orderRecord.setProductId(getUUID());
            orderRecord.setProductName("producr");
            orderRecord.setProductPrice(10.0);
            orderRecord.setPaymentId(getUUID());
            orderRecord.setPaymentAmount(20.0);
            orderRecord.setPaymentType("weixin");

            orderRecord.setOrderId(getUUID());
            orderRecord.setOrderDate(new Date().getTime());

            orderRecordList.add(orderRecord);
        }
        orderRecordHbaseRepository.saveAll(orderRecordList);
    }

    @Test
    public void get(){
        OrderRecord orderRecord = orderRecordHbaseRepository.findByRowkey("fcfb6351-9dd1-446e-92e0-13350a6f1929");
        System.out.println(JSON.toJSONString(orderRecord));
    }

    private String getUUID(){
        return UUID.randomUUID().toString();
    }
}
