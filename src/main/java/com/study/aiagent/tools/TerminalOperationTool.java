package com.study.aiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.Charset;

@Component
public class TerminalOperationTool implements MyTool{

    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder;
            String charset;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                builder = new ProcessBuilder("cmd.exe", "/c", command);
                charset = "GBK";  // Windows 中文版默认 GBK
            } else {
                builder = new ProcessBuilder("bash", "-c", command);
                charset = "UTF-8";  // Linux/Mac 默认 UTF-8
            }

            Process process = builder.start();

            // 使用正确的字符集
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName(charset)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), Charset.forName(charset)))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    output.append("[ERROR] ").append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString();
    }
}

