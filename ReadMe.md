# 背景
由于业务需要，公司App的用户中心的功能入口需要定制化可见，比如App版本>=xxx、已登录、来源于渠道A、用户等级为VIP的才可以看到某个功能入口。业务初期为了快速上线，自然是一堆if、else堆叠出来，但后期新增、调整、维护、变更各种条件组合和功能入口的时候很不方便，于是有了切换为通过规则引擎配置的方式来配置这些功能入口的想法。

规则引擎网上也是一堆资料，不过Java本身已经具备直接执行JavaScript脚本的能力，再加上JavaScript脚本的编写、验证、学习曲线相对平缓一些，而且也没有大规模使用过这个规则引擎，所以选择了使用Java8内置的`Nashorn`引擎 + JavaScript来实现简单、直观的规则引擎。

# 使用说明
## 本项目代码中包含了5种元素

1）条件(condition，或者叫条件脚本)，它接收入参，并有一个bool结果，满足or不满足；详见condition目录下的各种*.js文件组成的条件；    
2）条件服务(ConditionJudgeService)，它负责对各种组合条件的解析和判断，也只有一个bool结果，满足or不满足，see `ConditionJudgeService.judgeMergedCondition`；      
3）条件脚本服务(ConditionScriptService)，它负责对`条件`(第一个元素)生命周期管理，比如对各个脚本条件的查找、加载、初始化等；  
4）`ScriptEngine`，也就是可以执行JavaScript代码并给出返回值的Nashorn引擎；        
5）执行引擎服务(`ScriptEngineService`)，它负责对`ScriptEngine`生命周期进行管理。    

## 补充说明
1）由于ScriptEngine是一种需要池化的资源（每次临时new性能较低所以池化了），在并发环境下使用时可能会有上下文污染的问题，所以需要使用时最好通过`ConditionJudgeService.doWithScriptEngine`进行调用，该方法会自动管理上下文，保证并发调用上下文的正确性；    
2）`条件`除了用JavaScript实现，其实也可以用Java代码实现，详见`Cond_DateLimit`条件脚本。    

另外测试用例`ConditionJudgeServiceTest`包含了所有使用姿势，请移步测试用例查看。

本项目代码已经经过了线上验证，但没有继续深入的压测数据，需要的话可以自己写测试用例跑一下。