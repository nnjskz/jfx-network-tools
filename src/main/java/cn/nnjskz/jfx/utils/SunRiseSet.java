/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.utils;

public class SunRiseSet {
    /** 检查系统是否为深色模式，支持 macOS 和 Windows */
    public static Boolean isSystemInDarkMode() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            try {
                Process process = Runtime.getRuntime().exec(
                        new String[] { "defaults", "read", "-g", "AppleInterfaceStyle" }
                );
                process.waitFor();
                return process.exitValue() == 0;
            } catch (Exception e) {
                return false;
            }
        } else if (os.contains("win")) {
            try {
                Process process = Runtime.getRuntime().exec(
                        new String[] {
                                "reg", "query",
                                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                                "/v", "AppsUseLightTheme"
                        }
                );
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream())
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("AppsUseLightTheme")) {
                        String[] parts = line.trim().split("\\s+");
                        String value = parts[parts.length - 1];
                        return "0x0".equals(value) || "0".equals(value);
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

}
