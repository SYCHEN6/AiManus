package com.study.aiagent.tools.group;

import com.study.aiagent.tools.MyTool;

/**
 * 文件类工具分组标记接口。
 * 实现此接口的工具会被自动分配给 FileAgent。
 * 新增文件/PDF/下载类工具时，只需实现此接口，无需修改 SubAgentFactory。
 */
public interface FileToolGroup extends MyTool {
}
