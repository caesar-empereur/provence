package com.app.model.jpa;

import com.app.pojo.OrderPojo;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/26.
 */
@Data
@Entity
@Table(name = "orders")
public class Order extends OrderPojo {

    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @Column(length = 36)
    private String id;

    private Date createTime;
}
