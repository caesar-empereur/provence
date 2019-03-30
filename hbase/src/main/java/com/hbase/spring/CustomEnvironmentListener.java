package com.hbase.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import com.hbase.config.ConnectionConfig;
import com.hbase.exception.ConfigurationException;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/26.
 */
@Component
public class CustomEnvironmentListener implements EnvironmentPostProcessor {
    
    public static ConnectionConfig connectionConfig;
    
    public static Boolean ENABLED = Boolean.FALSE;
    
    private static final String HADOOP_DIR_KEY = "hbase.hadoop.dir";
    
    private static final String QUORUM_KEY = "hbase.quorum";
    
    private static final String ENABLE_KEY = "hbase.enabled";
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        if (!environment.containsProperty(QUORUM_KEY)
            || StringUtils.isBlank(environment.getProperty(QUORUM_KEY))) {
            throw new ConfigurationException("没有配置 hbase 地址");
        }
        if (!environment.containsProperty(ENABLE_KEY)
            || StringUtils.isBlank(environment.getProperty(ENABLE_KEY))) {
            throw new ConfigurationException("没有配置 hbase 地址");
        }
        ENABLED = environment.getProperty(ENABLE_KEY, Boolean.class);
        connectionConfig = new ConnectionConfig();
        connectionConfig.setQuorum(environment.getProperty(QUORUM_KEY));
        connectionConfig.setHadoopDir(environment.getProperty(HADOOP_DIR_KEY));
    }
}
