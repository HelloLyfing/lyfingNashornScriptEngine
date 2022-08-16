package com.lyfing.demo.cond;

import lombok.Setter;

import javax.script.*;

/**
 * java实现的仿script实现，类名格式为：Cond_ + 条件名称，例如 Cond_DateLimit
 * @author lyfing
 */
public abstract class BaseConditionScript extends CompiledScript {

    @Setter
    private ScriptEngine scriptEngine;

    public abstract boolean doEval(Bindings bindings) throws ScriptException;

    @Override
    public Object eval(ScriptContext context) throws ScriptException {
        throw new RuntimeException("不该走到的分支");
    }

    @Override
    public ScriptEngine getEngine() {
        throw new RuntimeException("不该走到的分支");
    }

    @Override
    public Object eval() throws ScriptException {
        Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        return doEval(bindings);
    }

    @Override
    public Object eval(Bindings bindings) throws ScriptException {
        return doEval(bindings);
    }

}
