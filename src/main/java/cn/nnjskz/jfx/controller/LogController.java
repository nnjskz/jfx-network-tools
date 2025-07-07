/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class LogController {
    @FXML
    private TextArea logContent;

    public void setContent(StringBuilder content) {
        if (logContent != null) {
            logContent.setText(content.toString());
        }
    }
}
