package com.study.aiagent.tools.group;

import com.study.aiagent.tools.MyTool;

/**
 * 系统与通信类工具分组标记接口。
 * 实现此接口的工具会被自动分配给 SystemCommAgent。
 * 新增终端命令/邮件/通知类工具时，只需实现此接口，无需修改 SubAgentFactory。
 */
public interface SystemCommToolGroup extends MyTool {
}
