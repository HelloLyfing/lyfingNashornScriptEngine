package com.lyfing.demo.service;

import com.lyfing.demo.bo.UserInfoBO;
import com.lyfing.demo.container.DefaultBeanContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.util.*;

/**
 * 参考本测试用例，即可差不多了解所有实现
 */
@Slf4j
public class ConditionJudgeServiceTest {

    private ConditionJudgeService conditionJudgeService;

    @Test
    public void platformIncludeTest() {
        Map<String, Object> ctxData = new HashMap<>();
        ctxData.put(ConditionJudgeService.ContextDataKey.platform, 1);

        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("platformInclude", "1,6", scriptEngine);
            Assert.assertTrue(result);

            result = conditionJudgeService.judgeCondition("platformInclude", "5,8,9", scriptEngine);
            Assert.assertFalse(result);
        }));
    }

    @Test
    public void trueOrFalseTest() {
        Map<String, Object> ctxData = new HashMap<>();
        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("false", null, scriptEngine);
            Assert.assertFalse(result);

            result = conditionJudgeService.judgeCondition("false", null, scriptEngine);
            Assert.assertFalse(result);

            result = conditionJudgeService.judgeCondition("true", null, scriptEngine);
            Assert.assertTrue(result);
        }));
    }

    @Test
    public void isLoginTest() {
        Map<String, Object> ctxData = new HashMap<>();
        UserInfoBO userInfo = new UserInfoBO();

        userInfo.setUserId(1024L);
        ctxData.put(ConditionJudgeService.ContextDataKey.userInfo, userInfo);

        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("isLogin", null, scriptEngine);
            Assert.assertTrue(result);
        }));

        userInfo.setUserId(0L);
        ctxData.put(ConditionJudgeService.ContextDataKey.userInfo, userInfo);
        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("isLogin", null, scriptEngine);
            Assert.assertFalse(result);
        }));


        userInfo.setUserId(null);
        ctxData.put(ConditionJudgeService.ContextDataKey.userInfo, userInfo);
        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("isLogin", null, scriptEngine);
            Assert.assertFalse(result);
        }));

        ctxData.put(ConditionJudgeService.ContextDataKey.userInfo, null);
        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("isLogin", null, scriptEngine);
            Assert.assertFalse(result);
        }));
    }

    @Test
    public void isFromAppTest() {
        Map<String, Object> ctxData = new HashMap<>();
        ctxData.put(ConditionJudgeService.ContextDataKey.platform, 2);

        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("isFromApp", null, scriptEngine);
            Assert.assertTrue(result);
        }));

        ctxData.put(ConditionJudgeService.ContextDataKey.platform, 1);
        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("isFromApp", null, scriptEngine);
            Assert.assertFalse(result);
        }));
    }

    @Test
    public void isFromH5Test() {
        Map<String, Object> ctxData = new HashMap<>();
        ctxData.put(ConditionJudgeService.ContextDataKey.platform, 1);

        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("isFromH5", null, scriptEngine);
            Assert.assertTrue(result);
        }));

        ctxData.put(ConditionJudgeService.ContextDataKey.platform, 2);
        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("isFromH5", null, scriptEngine);
            Assert.assertFalse(result);
        }));
    }

    @Test
    public void mergeConditionTest() {
        UserInfoBO userInfo = new UserInfoBO();
        userInfo.setUserId(1024L);

        Map<String, Object> ctxData = new HashMap<>();
        ctxData.put(ConditionJudgeService.ContextDataKey.platform, 1);
        ctxData.put(ConditionJudgeService.ContextDataKey.userInfo, userInfo);
        ctxData.put(ConditionJudgeService.ContextDataKey.version, "2.2.9");

        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            String mergeCondition = "platformInclude: 1 && isLogin && versionGE: 2.2.8";
            boolean result = conditionJudgeService.judgeMergedCondition(mergeCondition, scriptEngine);
            Assert.assertTrue(result);

            mergeCondition = "platformInclude: 2 || !isLogin || versionGE: 2.2.8";
            result = conditionJudgeService.judgeMergedCondition(mergeCondition, scriptEngine);
            Assert.assertTrue(result);

            mergeCondition = "platformInclude: 2 || !isLogin || versionGE: 2.3.0";
            result = conditionJudgeService.judgeMergedCondition(mergeCondition, scriptEngine);
            Assert.assertFalse(result);
        }));
    }

    @Test
    public void versionGETest() {
        Map<String, Object> ctxData = new HashMap<>();
        ctxData.put(ConditionJudgeService.ContextDataKey.version, "2.2.9");

        conditionJudgeService.doWithScriptEngine(ctxData, (scriptEngine -> {
            boolean result = conditionJudgeService.judgeCondition("versionGE", "1.3.0", scriptEngine);
            Assert.assertTrue(result);

            result = conditionJudgeService.judgeCondition("versionGE", "2.2.9", scriptEngine);
            Assert.assertTrue(result);

            result = conditionJudgeService.judgeCondition("versionGE", "2.3.0", scriptEngine);
            Assert.assertFalse(result);

            result = conditionJudgeService.judgeCondition("versionGE", "2.2.10", scriptEngine);
            Assert.assertFalse(result);

            result = conditionJudgeService.judgeCondition("versionGE", "2.3.0", scriptEngine);
            Assert.assertFalse(result);
        }));
    }

    @Test
    public void judgeConditionMultiThreads() throws InterruptedException {
        Map<String, Object> ctxData = new HashMap<>();
        ctxData.put(ConditionJudgeService.ContextDataKey.platform, 1);
        ctxData.put(ConditionJudgeService.ContextDataKey.version, "2.2.9");

        List<Thread> threadList = new ArrayList<>();

        List<Boolean> successResList = new ArrayList<>();

        Thread thread;
        thread = new Thread(() -> {
            trueOrFalseTest();
            successResList.add(true);
        });
        threadList.add(thread);

        thread = new Thread(() -> {
            isFromAppTest();
            successResList.add(true);
        });
        threadList.add(thread);

        thread = new Thread(() -> {
            isFromH5Test();
            successResList.add(true);
        });
        threadList.add(thread);

        thread = new Thread(() -> {
            isLoginTest();
            successResList.add(true);
        });
        threadList.add(thread);

        thread = new Thread(() -> {
            platformIncludeTest();
            successResList.add(true);
        });
        threadList.add(thread);

        thread = new Thread(() -> {
            versionGETest();
            successResList.add(true);
        });
        threadList.add(thread);

        for (int i = 0; i < threadList.size(); i++) {
            threadList.get(i).setName("test-thread-" + i);
        }

        for (int i = 0; i < threadList.size(); i++) {
            threadList.get(i).start();
        }

        for (int i = 0; i < threadList.size(); i++) {
            threadList.get(i).join();
        }

        Assert.assertEquals(successResList.size(), threadList.size());
    }

    @Before
    public void before() {
        ConditionScriptService conditionScriptService = new ConditionScriptService();
        try {
            conditionScriptService.init();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        ScriptEngineService scriptEngineService = new ScriptEngineService();
        // 依赖注入
        ReflectionUtils.doWithFields(scriptEngineService.getClass(), (field) -> {
            if (Objects.equals("beanContainerAdapter", field.getName())) {
                field.setAccessible(true);
                field.set(scriptEngineService, new DefaultBeanContainer());
            }
        });
        // 初始化
        try {
            scriptEngineService.init();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        conditionJudgeService = new ConditionJudgeService();
        // 依赖注入
        ReflectionUtils.doWithFields(ConditionJudgeService.class, (field) -> {
            if (Objects.equals("conditionScriptService", field.getName())) {
                field.setAccessible(true);
                field.set(conditionJudgeService, conditionScriptService);
            }
            if (Objects.equals("scriptEngineService", field.getName())) {
                field.setAccessible(true);
                field.set(conditionJudgeService, scriptEngineService);
            }
        });
    }

}
