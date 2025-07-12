/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
module jfx.network.tools.nnjskz {
    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.antdesignicons;
    requires com.alibaba.fastjson2;
    requires java.desktop;
    requires org.fxmisc.richtext;
    requires javafx.fxml;
    requires org.fxmisc.undo;
    requires wellbehavedfx;


    opens cn.nnjskz.jfx to javafx.fxml;
    exports cn.nnjskz.jfx;
    exports cn.nnjskz.jfx.controller;
    opens cn.nnjskz.jfx.controller to javafx.fxml;
    opens cn.nnjskz.jfx.model to javafx.base;
}