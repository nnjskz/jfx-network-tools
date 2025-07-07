/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.model;


import java.util.Map;

public class HistoryLogs {
    private Integer logSeq;
    private String logName;
    private Long logSize;

    public HistoryLogs(Integer logSeq, String logName, Long logSize) {
        this.logSeq = logSeq;
        this.logName = logName;
        this.logSize = logSize;
    }

    public HistoryLogs() {
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public Long getLogSize() {
        return logSize;
    }

    public void setLogSize(Long logSize) {
        this.logSize = logSize;
    }

    public Integer getLogSeq() {
        return logSeq;
    }

    public void setLogSeq(Integer logSeq) {
        this.logSeq = logSeq;
    }
}
