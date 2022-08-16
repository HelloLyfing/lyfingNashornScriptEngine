package com.lyfing.demo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.annotation.PostConstruct;
import javax.script.*;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * script engine service
 */
public class ScriptEngineService {

    private GenericObjectPool<ScriptEngine> scriptEnginePool = null;

    /**
     * 准备或复用脚本执行引擎，同一业务线程内(HTTP请求线程内、Dubbo请求线程内)会复用同一个scriptEngine
     */
    public ScriptEngine prepareScriptEngine(Map<String, Object> contextData) {
        ScriptEngine scriptEngine = null;

        try {
            scriptEngine = getOne();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        contextData.keySet().forEach((key) -> {
            bindings.put(key, convertScriptArg(contextData.get(key)));
        });

        return scriptEngine;
    }

    public void resetScriptEngine(ScriptEngine engine) {
        // 做一下上下文的清理
        engine.setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);

        returnOne(engine);
    }

    public Object convertScriptArg(Object condInput) {
        if (condInput == null) {
            return null;
        }

        if (condInput instanceof String
                || condInput instanceof Boolean
                || condInput instanceof Number
                || condInput instanceof Future // 是的，future也可以作为入参传入，再由对应脚本进行get()操作
                || condInput instanceof JSON) {
            return condInput;
        }

        // 任何其他结构都转成json格式
        return JSON.parseObject(JSON.toJSONString(condInput));
    }

    public ScriptEngine getOne() throws Exception {
        return scriptEnginePool.borrowObject();
    }

    public void returnOne(ScriptEngine engine) {
        scriptEnginePool.returnObject(engine);
    }

    @PostConstruct
    public void init() {
        /**
         * 此处配置业务方可以定制化改造
         */
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

        scriptEnginePool = new GenericObjectPool<>(new InnerPoolableObjectFactory(), poolConfig);
    }

    public static class InnerPoolableObjectFactory extends BasePooledObjectFactory<ScriptEngine> {

        /**
         * 创建一个对象实例
         */
        @Override
        public ScriptEngine create() throws Exception {
            ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
            return scriptEngine;
        }

        /**
         * 包裹创建的对象实例，返回一个pooledObject
         */
        @Override
        public PooledObject<ScriptEngine> wrap(ScriptEngine obj) {
            return new DefaultPooledObject<>(obj);
        }

    }
}
