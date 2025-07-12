/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {
    /**
     * 时间格式常量
     */
    public static final String COMMON_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String COMMON_PATTERN_TYPE2 = "yyyy/MM/dd HH:mm:ss";
    public static final String SHORT_PATTERN = "yyyy-MM-dd";
    public static final String SHORT_PATTERN_TYPE2 = "yyyy/MM/dd";
    public static final String LONG_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String SUP_SHORT_PATTERN = "yyyyMMdd";
    public static final String SUP_LONG_PATTERN = "yyyyMMddHHmmss";
    public static final String YEAR_MONTH = "yyyyMM";
    public static final String CN_SHORT_PATTERN = "yyyy年MM月dd日";
    public static final String DDMM_PATTERN = "ddMM";

    /**
     * 1、将 String 解析为 date
     *
     * @param dateString 待解析的日期字符串
     * @param pattern    日期字符串的时间格式
     * @return
     */
    public static Date trans2Date(String dateString, String pattern) {
        String fmt = (pattern != null && !pattern.isEmpty()) ? pattern : COMMON_PATTERN;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(fmt);
        LocalDateTime ldt = LocalDateTime.parse(dateString, dtf);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 2、日期字符串格式化输出
     *
     * @param dateString  待格式化的日期字符串
     * @param fromPattern 待格式化日期字符串的格式
     * @param toPattern   格式化后的日期格式
     * @return
     */
    public static String formatDate2String(String dateString, String fromPattern, String toPattern) {
        String toFmt = (toPattern != null && !toPattern.isEmpty()) ? toPattern : COMMON_PATTERN;
        DateTimeFormatter dtfFrom = DateTimeFormatter.ofPattern(fromPattern);
        DateTimeFormatter dtfTo = DateTimeFormatter.ofPattern(toFmt);
        LocalDateTime ldt = LocalDateTime.parse(dateString, dtfFrom);
        return ldt.format(dtfTo);
    }

    /**
     * 3、Date类型日期转字符串格式化输出
     *
     * @param date    待格式化的日期
     * @param pattern 格式化后的格式
     * @return
     */
    public static String formatDate2String(Date date, String pattern) {
        String fmt = (pattern != null && !pattern.isEmpty()) ? pattern : COMMON_PATTERN;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(fmt);
        Instant instant = date.toInstant();
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return ldt.format(dtf);
    }

    /**
     * 4、根据时间戳格式化输出指定时间格式
     *
     * @param timestamp 时间戳（毫秒）
     * @param pattern   格式化后的格式
     * @return 格式化后的时间字符串
     */
    public static String formatDate2String(long timestamp, String pattern) {
        String fmt = (pattern != null && !pattern.isEmpty()) ? pattern : COMMON_PATTERN;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(fmt);
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return ldt.format(dtf);
    }
}
