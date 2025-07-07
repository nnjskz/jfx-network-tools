/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static cn.nnjskz.jfx.utils.ResourceBundleUtil.getProperty;

public class FileUtil {
    public final static String currentDir = getWritableAppDataDir();

    private static String getWritableAppDataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Paths.get(System.getenv("LOCALAPPDATA"), "JFXNetworkTools").toString();
        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "JFXNetworkTools").toString();
        } else {
            return Paths.get(System.getProperty("user.home"), ".jfx-network-tools").toString();
        }
    }

    /**
     * 获取历史连接文件内容
     * @param fileName 文件路径
     * @return map = host and port
     */
    public static Map<String,Integer> getConnHistoryFile(String fileName) throws IOException {
        Map<String,Integer> map = new HashMap<>();
        StringBuilder content = readFile(fileName);
        if (!content.isEmpty()) {
            String[] hosts = content.toString().split("\n");
            for (String h : hosts) {
                map.put(h.split(":")[0], Integer.valueOf(h.split(":")[1]));
            }
        }
        return map;
    }

    /**
     * 写入文件
     *
     * @param content 字符内容
     * @param fileName 文件名称
     * @param append 是否追加
     */
    public static void writeFile(String content, String fileName, boolean append) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(currentDir,fileName),
                StandardOpenOption.CREATE,
                append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.append(content);
            writer.newLine();
        }
    }

    /**
     * 获取历史日志列表
     * @return list
     */
    public static Map<String, Long> getHistoryLogList() throws IOException {
        var logPath = Paths.get(currentDir,getProperty.apply("logs.path"));
        Map<String, Long> result = new LinkedHashMap<>();

        if (!Files.exists(logPath)) {
            Files.createDirectories(logPath);
        } else if (!Files.isDirectory(logPath)) {
            return result;
        }

        try (var filesStream = Files.list(logPath)) {
            filesStream
                    .filter(Files::isRegularFile)
                    .sorted((f1, f2) -> {
                        try {
                            return Long.compare(
                                    Files.getLastModifiedTime(f2).toMillis(),
                                    Files.getLastModifiedTime(f1).toMillis());
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .forEach(path -> {
                        try {
                            result.put(path.getFileName().toString(), Files.size(path));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return result;
    }

    /**
     * 读取指定文件内容
     * @param fileName 路径+文件名
     * @return content 文件内容
     */
    public static StringBuilder readFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        Path filePath = Paths.get(currentDir, fileName);
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        try (BufferedReader buf = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = buf.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content;
    }

    /**
     * 删除指定文件
     * @param fileName 路径+文件名
     */
    public static Boolean deleteFile(String fileName) throws IOException {
        return Files.deleteIfExists(Paths.get(currentDir,fileName));
    }

}
