package com.lyfing.demo.service;

import com.alibaba.fastjson.JSON;
import com.lyfing.demo.container.BeanContainerAdapter;
import com.lyfing.demo.util.Constants;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.script.*;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * script engine service
 */
public class ScriptEngineService {

    @Resource
    private BeanContainerAdapter beanContainerAdapter;

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
        GenericObjectPoolConfig<ScriptEngine> poolConfig = beanContainerAdapter.getBean(
                Constants.SCRIPT_ENGINE_POOL_CONFIG, GenericObjectPoolConfig.class);

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
