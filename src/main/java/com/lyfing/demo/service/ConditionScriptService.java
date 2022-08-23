package com.lyfing.demo.service;

import com.alibaba.fastjson.JSON;
import com.lyfing.demo.cond.BaseConditionScript;
import com.lyfing.demo.util.CommonUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.PostConstruct;
import javax.script.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author lyfing
 */
public class ConditionScriptService {

    private static Logger log = LoggerFactory.getLogger(ConditionScriptService.class);

    private static Map<Pair<String /* scriptName */, ScriptEngine>, CompiledScript> cachedCompiledScript
            = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws Exception {
        log.info("加载并输出所有condition名称 -> {}", JSON.toJSONString(loadAllScriptNames()));
    }

    public CompiledScript getCompiledScript(String name, ScriptEngine scriptEngine) {
        CompiledScript compiledScript = cachedCompiledScript.get(Pair.of(name, scriptEngine));
        if (compiledScript == null) {
            compiledScript = loadScriptAndInit(name, scriptEngine);
            cachedCompiledScript.put(Pair.of(name, scriptEngine), compiledScript);
        }

        return compiledScript;
    }

    /**
     * 系统启动初始化时，或者存储的scriptCode有变更时，可以调用该方法重新编译script
     */
    public CompiledScript reloadScript(String name, ScriptEngine scriptEngine) {
        CompiledScript compiledScript = loadScriptAndInit(name, scriptEngine);
        cachedCompiledScript.put(Pair.of(name, scriptEngine), compiledScript);
        return compiledScript;
    }

    /**
     * 获取所有登记在册的脚本名称
     */
    public List<String> loadAllScriptNames() {
        List<String> nameList = new ArrayList<>();
        nameList.addAll(listJsCodeNames());
        nameList.addAll(listJavaCodeNames());
        return nameList;
    }

    /**
     * @param name 必须保证名称的唯一性
     */
    private CompiledScript loadScriptAndInit(String name, ScriptEngine scriptEngine) {
        // 加载js代码
        String scriptCode = loadJsCodeByName(name);
        if (scriptCode != null && scriptCode.length() > 0) {
            try {
                return ((Compilable) scriptEngine).compile(scriptCode);
            } catch (ScriptException e) {
                throw new RuntimeException("script编译失败:" + scriptCode);
            }
        }

        // 加载java代码
        CompiledScript compiledScript = loadJavaCodeByName(name, scriptEngine);
        if (compiledScript != null) {
            return compiledScript;
        }

        throw new IllegalStateException("script加载失败:" + name);
    }

    private CompiledScript loadJavaCodeByName(String name, ScriptEngine scriptEngine) {
        // 首字母大写
        String clazzName = "Cond_" + CommonUtils.upper1st(name);

        Class parentClazz = BaseConditionScript.class;
        clazzName = parentClazz.getName().replace(parentClazz.getSimpleName(), clazzName);
        try {
            Class<? extends BaseConditionScript> scriptClass = (Class<? extends BaseConditionScript>) Class.forName(clazzName);
            BaseConditionScript baseScript = scriptClass.newInstance();
            baseScript.setScriptEngine(scriptEngine);
            return baseScript;
        } catch (ClassNotFoundException e) {
            log.info("script class not found, {}", clazzName);
        } catch (Throwable t) {
            log.info("loadJavaScriptByName failed->{}", clazzName, t);
        }
        return null;
    }

    private List<String> listJavaCodeNames() {
        Class scriptBaseClazz = BaseConditionScript.class;
        String javaPath = scriptBaseClazz.getName()
                .replace("." + scriptBaseClazz.getSimpleName(), "")
                .replaceAll("\\.", "/");

        /**
         * 寻找给定目录下的所有java实现
         */
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<String> nameList = new ArrayList<>();
        try {
            Resource[] resources = resourceResolver.getResources(String.format("classpath:%s/*", javaPath));
            Arrays.asList(resources).forEach((res) -> {
                if (res.getFilename() != null && res.getFilename().endsWith(".class")) {
                    String condName = res.getFilename().replace(".class", "").trim();
                    if (condName.startsWith("Cond_")) {
                        nameList.add(CommonUtils.lower1st(condName.replace("Cond_", "")));
                    }
                }
            });
        } catch (Throwable th) {
            log.info("loadScripts in local failed");
        }

        return nameList;
    }

    /**
     * db, 配置中心，redis 随便存哪里
     */
    private String loadJsCodeByName(String name) {
        // 先从本地加载
        String localPath = String.format("condition/%s.js", name);
        try (InputStream resource = new ClassPathResource(localPath).getInputStream()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (FileNotFoundException e) {
            log.info("script file not found, {}", localPath);
        } catch (Throwable th) {
            log.info("loadScript in local failed, {}", localPath, th);
        }

        // 从DB、配置中心、Redis中加载

        return null;
    }

    private List<String> listJsCodeNames() {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<String> nameList = new ArrayList<>();
        try {
            Resource[] resources = resourceResolver.getResources("classpath:condition/*.js");
            Arrays.asList(resources).forEach((res) -> {
                if (res.getFilename() != null && res.getFilename().endsWith(".js")) {
                    nameList.add(res.getFilename().replace(".js", "").trim());
                }
            });
        } catch (Throwable th) {
            log.info("loadScripts in local failed");
        }

        // 从DB、配置中心、Redis中加载

        return nameList;
    }

}
