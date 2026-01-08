/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.controller;

import cn.nnjskz.jfx.model.HistoryLogs;
import cn.nnjskz.jfx.utils.DateUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cn.nnjskz.jfx.utils.WindowUtil.showConfirmationDialog;
import static cn.nnjskz.jfx.utils.WindowUtil.showWindow;
import static cn.nnjskz.jfx.utils.FileUtil.*;
import static cn.nnjskz.jfx.utils.ResourceBundleUtil.getProperty;

public class HistoryLogsController {
    @FXML
    private TableView<HistoryLogs> historyLogsTable;
    @FXML
    private TableColumn<HistoryLogs, Integer> logSeq;
    @FXML
    private TableColumn<HistoryLogs, String> logName;
    @FXML
    private TableColumn<HistoryLogs, Long> logSize;
    @FXML
    private TableColumn<HistoryLogs, String> logTime;
    @FXML
    private TableColumn<HistoryLogs, Void> action;

    @FXML
    private void initialize() throws IOException {
        // 渲染列表
        Map<String, Long> logMap = getHistoryLogList();
        List<HistoryLogs> historyLogsList = new ArrayList<>();
        AtomicReference<Integer> i = new AtomicReference<>(1);
        logMap.forEach((k, v) -> {
            String timestamp = k.substring(k.lastIndexOf('_') + 1);
            historyLogsList.add(new HistoryLogs(i.get(), k, v, DateUtil.formatDate2String(Long.parseLong(timestamp.split("\\.")[0]), DateUtil.COMMON_PATTERN)));
            i.getAndSet(i.get() + 1);
        });

        logSeq.setCellValueFactory(new PropertyValueFactory<>("logSeq"));
        logName.setCellValueFactory(new PropertyValueFactory<>("logName"));
        logSize.setCellValueFactory(new PropertyValueFactory<>("logSize"));
        logTime.setCellValueFactory(new PropertyValueFactory<>("logTime"));
        historyLogsTable.setItems(FXCollections.observableList(historyLogsList));

        action.setCellFactory(col -> new TableCell<>() {
            private final Button openBut = new Button("打开文件所在位置");
            private final Button selectBut = new Button("预览");
            private final Button deleteBut = new Button("删除");
            private final HBox hbox = new HBox(10, openBut, selectBut, deleteBut);

            {
                openBut.setOnAction(event -> {
                    HistoryLogs item = getTableView().getItems().get(getIndex());
                    File file = new File(currentDir + getProperty.apply("logs.path"), item.getLogName());
                    try {
                        if (!file.exists()) {
                            showConfirmationDialog("错误", "文件不存在：" + file.getAbsolutePath());
                            return;
                        }
                        String os = System.getProperty("os.name").toLowerCase();
                        if (os.contains("win")) {
                            Runtime.getRuntime().exec(new String[]{
                                    "explorer.exe",
                                    "/select,",
                                    file.getAbsolutePath()
                            });
                        } else if (os.contains("mac")) {
                            Runtime.getRuntime().exec(new String[]{
                                    "open",
                                    "-R",
                                    file.getAbsolutePath()
                            });
                        }
                    } catch (Exception e) {
                        showConfirmationDialog("异常", "无法打开文件：" + e.getMessage());
                    }
                });
                selectBut.setOnAction(event -> {
                    HistoryLogs item = getTableView().getItems().get(getIndex());
                    try {
                        StringBuilder content = readFile(getProperty.apply("logs.path") + item.getLogName());
                        FXMLLoader fxmlLoader = showWindow(getProperty.apply("log.detail.view.path"), "查看日志", true, LogController.class);
                        LogController controller = fxmlLoader.getController();
                        controller.setContent(content);
                        // 监听关闭Stage时清空logContent
                        Parent root = fxmlLoader.getRoot();
                        Stage stage = (Stage) root.getScene().getWindow();
                        stage.setOnCloseRequest(e -> controller.onClose());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                deleteBut.setOnAction(event -> {
                    HistoryLogs item = getTableView().getItems().get(getIndex());
                    boolean dialog = showConfirmationDialog("提示消息", "你确认要删除【" + item.getLogName() + "】吗?");
                    if (dialog) {
                        try {
                            Boolean deleted = deleteFile(getProperty.apply("logs.path") + item.getLogName());
                            if (deleted) {
                                showConfirmationDialog("成功消息", "删除成功!");
                                getTableView().getItems().remove(getIndex());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    hbox.setAlignment(Pos.CENTER);
                    setGraphic(hbox);
                }
            }
        });
    }
}
