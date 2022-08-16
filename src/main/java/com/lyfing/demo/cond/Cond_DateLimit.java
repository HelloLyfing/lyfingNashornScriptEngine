package com.lyfing.demo.cond;

import com.lyfing.demo.service.ConditionJudgeService;
import com.lyfing.demo.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.util.Date;

/**
 * @author lyfing
 */
@Slf4j
public class Cond_DateLimit extends BaseConditionScript {

    @Override
    public boolean doEval(Bindings bindings) throws ScriptException {
        /**
         * input格式: 开始时间,结束时间
         * 注意：二者必须同时存在
         * 时间格式: yyyy-MM-dd HH:mm:ss
         */
        String input = (String) bindings.get(ConditionJudgeService.ContextDataKey.input);

        if (StringUtils.isEmpty(input) || !input.contains(",")) {
            log.debug("input not valid->{}", input);
            return false;
        }

        String[] split = input.split(",");
        if (split.length != 2) {
            log.debug("input not valid->{}", input);
            return false;
        }

        Date beginDate = CommonUtils.string2DateTime(split[0].trim());
        Date endDate = CommonUtils.string2DateTime(split[1].trim());
        Date now = new Date();

        return beginDate.before(now) && now.before(endDate);
    }

}
