package com.study.aiagent.agent;

import lombok.extern.slf4j.Slf4j;

/**
 * ReAct模式的代理抽象类，reason+acting
 */
@Slf4j
public abstract class ReActAgent extends BaseAgent{
    /**
     * 进行思考
     *
     * @return 思考结果
     */
    public abstract boolean think();

    /**
     * 执行操作
     *
     * @return 执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤
     *
     * @return 执行结果
     */
    @Override
    public String step() {
        try {
            boolean isAct = think();
            if (isAct) {
                return act();
            }
            return "Finish thinking, need not to acting";
        } catch (Exception e) {
            log.error("Execute step cache an exception: {}", e);
            return "Execute step failed: " + e.getMessage();
        }
    }
}
