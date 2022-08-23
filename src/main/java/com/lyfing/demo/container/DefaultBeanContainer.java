package com.lyfing.demo.container;

import com.lyfing.demo.util.Constants;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import java.time.Duration;
import java.util.Objects;

/**
 * 默认实现仅为测试用例准备，请酌情使用
 * @author lyfing
 */
public class DefaultBeanContainer extends BeanContainerAdapter {

    private static Logger log = LoggerFactory.getLogger(DefaultBeanContainer.class);

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        if (Objects.equals(Constants.SCRIPT_ENGINE_POOL_CONFIG, name)) {
            return (T) createScriptEnginePoolConfig();
        }
        return null;
    }

    /**
     * 默认实现，仅供参考
     * @return
     */
    private GenericObjectPoolConfig<ScriptEngine> createScriptEnginePoolConfig() {
        GenericObjectPoolConfig<ScriptEngine> poolConfig = new GenericObjectPoolConfig<>();
        // 最大空闲数
        poolConfig.setMaxIdle(100);
        // 最小空闲数, 池中只有一个空闲对象的时候，池会在创建一个对象，并借出一个对象，从而保证池中最小空闲数为1
        poolConfig.setMinIdle(50);
        // 最大池对象总数
        poolConfig.setMaxTotal(200);
        // 在获取对象的时候检查有效性, 默认false
        poolConfig.setTestOnBorrow(false);
        // 在归还对象的时候检查有效性, 默认false
        poolConfig.setTestOnReturn(false);
        // 在空闲时检查有效性, 默认false
        poolConfig.setTestWhileIdle(false);
        // 借出对象时最大等待时间
        poolConfig.setMaxWait(Duration.ofMillis(2000));
        return poolConfig;
    }

}
