package com.lyfing.demo.service;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.script.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 条件判断service
 */
public class ConditionJudgeService {

    private static Logger log = LoggerFactory.getLogger(ConditionJudgeService.class);

    private static final Map<ScriptEngine, AtomicInteger> scriptEngineCounterMap = new ConcurrentHashMap<>();

    /**
     * 需要借助容器的自动注入，比如Spring注入
     */
    @Resource
    private ConditionScriptService conditionScriptService;

    /**
     * 需要借助容器的自动注入，比如Spring注入
     */
    @Resource
    private ScriptEngineService scriptEngineService;

    /**
     * 由于需要做scriptEngine的事前、事后处理，所以把这部分内容包装起来，把需要执行的业务逻辑放到bizLogic中
     */
    public void doWithScriptEngine(Map<String, Object> contextData, Consumer<ScriptEngine> bizLogic) {
        long tsStart = System.currentTimeMillis();

        ScriptEngine scriptEngine = scriptEngineService.prepareScriptEngine(contextData);
        scriptEngineCounterMap.put(scriptEngine, new AtomicInteger(0));

        try {
            bizLogic.accept(scriptEngine);
        } finally {
            scriptEngineService.resetScriptEngine(scriptEngine);

            long elapsed = System.currentTimeMillis() - tsStart;
            if (elapsed > 10) {
                log.info("time-cost: {}ms, script eval count->{}", elapsed, scriptEngineCounterMap.get(scriptEngine).get());
            }
        }
    }

    /**
     * 对于组合条件的统一判断入口，比如
     *     platformInclude: 1,2 && versionGE: 2.3.12
     *     userLevelGE: 2 || userLevelGE: 1
     *     platformInclude: 1,2
     * 复杂的组合条件，可以在本方法中改进并实现，比如 并 或 ()的逻辑组合
     */
    public boolean judgeMergedCondition(String mergeCondition, ScriptEngine scriptEngine) {
        if (mergeCondition.contains("&&")) {
            String[] conditionArr = mergeCondition.split("&&");
            for (String oneCond : conditionArr) {
                if (!judgeCondition(oneCond.trim(), scriptEngine)) {
                    return false;
                }
            }
            return true;
        } else if (mergeCondition.contains("||")) {
            String[] conditionArr = mergeCondition.split("\\|\\|");
            for (String oneCond : conditionArr) {
                if (judgeCondition(oneCond.trim(), scriptEngine)) {
                    return true;
                }
            }
            return false;
        } else {
            return judgeCondition(mergeCondition.trim(), scriptEngine);
        }
    }

    public boolean judgeCondition(String condStr, ScriptEngine scriptEngine) {
        if (condStr.startsWith("!")) {
            // 去掉"!"符号，并进行取反匹配
            return !judgeCondition(condStr.substring(1).trim(), scriptEngine);
        }

        String condName = null, condInput = null;
        int separatorIdx = condStr.indexOf(":");
        if (separatorIdx > 0) {
            condName = condStr.substring(0, separatorIdx).trim();
            condInput = condStr.substring(separatorIdx + 1).trim();
        } else {
            condName = condStr.trim();
        }
        return judgeCondition(condName, condInput, scriptEngine);
    }

    /**
     *
     * @param condName 比如platformInclude.js脚本，该condition的作用是判断当前请求platform是否在给定condInput中
     * @param condInput 脚本的参数输入，一般写在配置项里，比如需要判断platform是否为1或2，则condInput为 1,2
     * @return
     */
    public boolean judgeCondition(String condName, String condInput, ScriptEngine scriptEngine) {
        CompiledScript compiledScript = conditionScriptService.getCompiledScript(condName, scriptEngine);
        Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        try {
            // 动态配置input
            bindings.put(ContextDataKey.input, scriptEngineService.convertScriptArg(condInput));

            Object result = compiledScript.eval();
            return (boolean) result;
        } catch (ScriptException e) {
            log.error("script执行异常，condName->{}", condName, e);
            throw new RuntimeException("script执行异常:" + condName);
        } finally {
            try {
                // 统计一下调用次数
                scriptEngineCounterMap.get(scriptEngine).incrementAndGet();
            } catch (Throwable t) {
                log.error("script counter执行异常，condName->{}, bindings->{}", condName, JSON.toJSONString(bindings), t);
            }
        }
    }

    public abstract class ContextDataKey {
        /**
         * 这些变量名，在js代码中可以直接引用和使用
         */
        public static final String gjUa = "gjUa";
        public static final String platform = "platform";
        public static final String version = "version"; // 比如2.5.6，格式要求：必须是有两个点号的三段式
        public static final String userInfo = "userInfo"; // see UserInfoBO
        public static final String input = "input"; // 给定配置内容，字符串类型，比如版本号 >= 2.3.1，则input = 2.3.1

    }

}
