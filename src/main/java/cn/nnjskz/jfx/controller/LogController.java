/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.controller;

import cn.nnjskz.jfx.utils.ThemeManager;
import javafx.fxml.FXML;
import org.fxmisc.richtext.InlineCssTextArea;

public class LogController {
    @FXML
    private InlineCssTextArea logContent;


    public void setContent(StringBuilder content) {
        if (logContent != null) {
            String textColor = ThemeManager.isDarkMode() ? "#fbfbfb" : "#1c1c1e";
            logContent.append(content.toString(),"-fx-fill: " + textColor);
        }
    }

    public void onClose(){
        logContent.clear();
        logContent.replaceText("");
    }
}
