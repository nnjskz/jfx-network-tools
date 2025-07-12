
/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.controller;

import cn.nnjskz.jfx.model.HistoryConn;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.nnjskz.jfx.utils.FileUtil.getConnHistoryFile;
import static cn.nnjskz.jfx.utils.ResourceBundleUtil.getProperty;

public class HistoryConnController {
    @FXML
    private TableView<HistoryConn> historyConnTable;
    @FXML
    private TableColumn<HistoryConn, String> host;
    @FXML
    private TableColumn<HistoryConn, Integer> port;
    @FXML
    private TableColumn<HistoryConn, Void> action;

    private Consumer<HistoryConn> onSelected;
    public void setOnSelected(Consumer<HistoryConn> onSelected) {
        this.onSelected = onSelected;
    }

    @FXML
    private void initialize() throws IOException {
        Map<String, Integer> connMap = getConnHistoryFile(getProperty.apply("conn.history.path"));
        List<HistoryConn> connHistoryList = new ArrayList<>();
        connMap.forEach((k,v) -> connHistoryList.add(new HistoryConn(k, v)));


        if (!connHistoryList.isEmpty()) {
            host.setCellValueFactory(new PropertyValueFactory<>("host"));
            port.setCellValueFactory(new PropertyValueFactory<>("port"));
            historyConnTable.setItems(FXCollections.observableList(connHistoryList));
            action.setCellFactory(col -> new TableCell<>(){
                private final Button selectBut = new Button("选择");
                {
                    selectBut.setOnAction(event -> {
                        HistoryConn item = getTableView().getItems().get(getIndex());
                        // 回显到主窗口输入框
                        if (onSelected != null) {
                            onSelected.accept(item);
                        }
                    });
                }
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    }else {
                        setGraphic(selectBut);
                    }
                }
            });
        }

    }
}
