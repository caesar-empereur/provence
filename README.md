## 说明文档

#### 功能说明
```
这是一个使用方式类似 jpa 对 hbase 数据库操作的小工具
```

#### 解决了什么问题
* 现在的springboot-starter 没有集成 hbase,操作hbase没有官方的方便的方式
* 第三方工具如phoenix需要部署到hbase服务器中
* kundera 集成在应用当中，接口方法比较完善，但是在实体类中不能配置 column-family

#### 这个工具的特点
* 采用类似JPA的操作方法对 hbase 数据库进行增删改操作

* 使用简单，只需要注解加上接口方法，不需要实现
* 需要运行在 springboot 的环境中
* 无需单独的配置文件，只需要在springboot 的配置文件中加上几个项
* 该模块没有依赖springboot的任何jar，不会有jar包冲突


#### 工程需要的环境

* gradle4.3
* JDK1.8

#### 代码check下来后编译jar包的方式
```
进入工程的根目录，也就是gradle.build的目录, 执行 gradle clean bootWar命令
编译工程, 生成依赖的 jar 包和可启动的 war 包
```
