#hbase-model

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
