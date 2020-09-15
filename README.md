## 说明文档

#### **[功能说明](#)**
- 这是一个使用方式类似 jpa 对 hbase 数据库操作的小工具

#### **[解决了什么问题](#)**
* 现在的springboot-starter 没有集成 hbase,操作hbase没有官方的方便的方式
* 第三方工具如phoenix需要部署到hbase服务器中
* kundera 集成在应用当中，接口方法比较完善，但是在实体类中不能配置 column-family

#### **[这个工具的特点](#)**
* 采用类似JPA的操作方法对 hbase 数据库进行增删改操作

* 使用简单，只需要注解加上接口方法，不需要实现
* 需要运行在 springboot 的环境中
* 无需单独的配置文件，只需要在springboot 的配置文件中加上几个项
* 该模块没有依赖springboot的任何jar，不会有jar包冲突

#### **[使用步骤](#)** (按照app 模块里面的写法, master分支)
* 编写entity, 加上对应的注解
```
@HbaseTable(name = "order-record")
@Data
public class OrderRecord {
    @ColumnFamily(name = "product")
    private String productId;
}
```
* 编写查询接口
```
@com.hbase.annotation.HbaseRepository
public interface OrderRecordHbaseRepository extends HbaseRepository<OrderRecord, String>{
}
```
* 启动类加上注解
```
@ComponentScan({"com.app","com.hbase"})
@SpringBootApplication
@HbaseTableScan(modelPackage = "com.app", repositoryPackage = "com.app")
public class HbaseApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(HbaseApplication.class);
    }

}
```
* spring 的配置文件加上对应的配置项
```
hbase.hadoop.dir=D:\\dev\\app\\hadoop-common\\hadoop-common-2.2.0-bin-master
hbase.quorum=127.0.0.1,127.0.0.1
hbase.enabled=false
```

#### 工程需要的环境

* gradle4.3
* JDK1.8, springboot 2.0.7

#### **[模块说明](#)**
* 这是一个多模块的项目
* springboot 的依赖是写在父工程的脚本的
* hbase 模块就是这个工具的实现的提供方
* app 模块就是用到 hbase 模块的使用方，测试用例写在这里面

#### 代码check下来后编译jar包的方式
```
进入工程的根目录，也就是gradle.build的目录, 执行 gradle clean bootWar命令
编译工程, 生成依赖的 jar 包和可启动的 war 包
```

## **[实现原理](#)**
- 总体上是基于 springboot 的 bean 注入，注解扫描解析，FactoryBean 代理接口注入的适用
- 启动的时候扫描加了 @HbaseTable 的实体类 和 @HbaseRepository 注解的接口
- @HbaseTable 相当于JPA的 @Entity 实体, @HbaseRepository 相当于JPA 的 @Repository 查询接口
- 用一个 HbaseRepositoryFactoryBean(实现了FactoryBean接口的类)来封装需要注入的 bean 的信息
- HbaseRepositoryFactoryBean 的 getObject() 方法返回的是一个代理了 目标查询接口的bean
- getObject() 方法返回 ProxyFactory 构造的一个对象，代理目标是 SimpleHbaseRepository，代理接口是查询接口
- SimpleHbaseRepository 才是真正的实现curd功能的查询接口，该bean包含了一个查询接口关联的实体信息
- 最终注入的 bean 是 BeanDefinitionBuilder.rootBeanDefinition(HbaseRepositoryFactoryBean.class);
- 这种FactoryBean在容器中获取的时候是调用 getObject() 方法返回的bean对象
- 在有注入查询接口的地方，spring 返回的 bean 对象就是getObject() 方法返回的bean对象

## Hbase 基本信息
- Hbase 在大数据中的校色
    ![TCP可靠性保证](https://github.com/caesar-empereur/provence/blob/master/doc/tcp/TCP可靠性保证.png)
- 一个存储大Map的数据库
    ![一个存储大Map的数据库](https://github.com/caesar-empereur/provence/blob/master/doc/一个存储大Map的数据库.png)
- Hbase 查询设计
    ![hbase应用场景](https://github.com/caesar-empereur/provence/blob/master/doc/hbase应用场景.png)
- Hbase 关联查询
    ![Hbase关联查询](https://github.com/caesar-empereur/provence/blob/master/doc/Hbase关联查询.png)
- Hbase 存储原理
    ![Hbase存储原理](https://github.com/caesar-empereur/provence/blob/master/doc/Hbase存储原理.png)
- Hbase get, put 过程
    ![Hbase-get-put过程](https://github.com/caesar-empereur/provence/blob/master/doc/Hbase-get-put过程.png)
