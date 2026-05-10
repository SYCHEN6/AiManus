package com.study.aiagent.agent.multi;

import com.study.aiagent.tools.group.FileToolGroup;
import com.study.aiagent.tools.group.ResearchToolGroup;
import com.study.aiagent.tools.group.SystemCommToolGroup;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 子 Agent 工厂。
 *
 * Spring 按分组接口自动收集各工具 Bean，ToolCallbacks.from() 直接将 Bean 转为 ToolCallback[]。
 * 扩展时只需让新工具 Bean 实现对应的分组接口，此工厂无需任何改动。
 */
@Component
public class SubAgentFactory {

    private final ChatModel chatModel;
    private final List<ResearchToolGroup> researchBeans;
    private final List<FileToolGroup> fileBeans;
    private final List<SystemCommToolGroup> systemCommBeans;

    public SubAgentFactory(
            @Qualifier("dashScopeChatModel") ChatModel chatModel,
            List<ResearchToolGroup> researchBeans,
            List<FileToolGroup> fileBeans,
            List<SystemCommToolGroup> systemCommBeans) {
        this.chatModel = chatModel;
        this.researchBeans = researchBeans;
        this.fileBeans = fileBeans;
        this.systemCommBeans = systemCommBeans;
    }

    public ResearchAgent createResearchAgent() {
        return new ResearchAgent(toToolCallbacks(researchBeans), chatModel);
    }

    public FileAgent createFileAgent() {
        return new FileAgent(toToolCallbacks(fileBeans), chatModel);
    }

    public SystemCommAgent createSystemCommAgent() {
        return new SystemCommAgent(toToolCallbacks(systemCommBeans), chatModel);
    }

    private static ToolCallback[] toToolCallbacks(List<?> beans) {
        return ToolCallbacks.from(beans.toArray());
    }
}
