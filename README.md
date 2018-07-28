## 说明文档

* 功能说明
```
这是一个基于 springboot:2.0.0.M7版本的权限认证的模块
使用该模块编译后的jar包可实现基本的管理员普通用户权限过滤
```
* 工程需要的环境
```
gradle4.3, JDK1.8
```
* 代码check下来后编译jar包的方式
```
进入工程的根目录，也就是gradle.build的目录, 执行 gradle clean bootWar命令
编译工程, 生成依赖的 jar 包和可启动的 war 包
```

JPA-源码研究

EntityScan 注解在启动类的运行

EntityScanPackages bean 包含有 需要扫描的包的信息
然后将这个 bean 注册到 BeanDefinitionRegistry 中

重要类的分析

DefaultPersistenceUnitManager
.scanPackage(SpringPersistenceUnitInfo scannedUnit, String pkg)
.buildDefaultPersistenceUnitInfo()
.preparePersistenceUnitInfos()

MergingPersistenceUnitManager

JavaReflectionManager.toXClass()

ConcurrentBag
.requited() 回报, 发生在链接关闭的时候
.borrow() 借链接, 发生在获取连接的时候

