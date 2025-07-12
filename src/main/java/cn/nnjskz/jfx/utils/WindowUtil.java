/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.utils;

import cn.nnjskz.jfx.MainApplication;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.nnjskz.jfx.MainApplication.stage;
import static cn.nnjskz.jfx.utils.ResourceBundleUtil.getProperty;

public class WindowUtil {

    /**
     * 通用切换页面
     *
     * @param fxml   fxml路径
     * @param width  宽度；0代表不设置
     * @param height 高度；0代表不设置
     */
    public static void changeView(String fxml, Integer width, Integer height) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(fxml));
        Scene scene;
        if (width != 0 && height != 0) {
            scene = new Scene(fxmlLoader.load(), width, height);
        } else {
            scene = new Scene(fxmlLoader.load());
        }
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    /**
     * 弹出新窗口
     *
     * @param fxml        fxml路径
     * @param tClass      fxml加载类
     * @param title       标头
     * @param isResizable 是否可放大缩小
     * @return loader
     */
    public static FXMLLoader showWindow(String fxml, String title, Boolean isResizable, Class<?> tClass) throws IOException {
        FXMLLoader loader = new FXMLLoader(tClass.getResource(fxml));
        Parent root = loader.load();
        createScene(root, title, isResizable);
        return loader;
    }

    /**
     * 显示提示框
     *
     * @param message 提示消息
     * @param title   标头
     */
    public static void showTip(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(stage);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * 通用显示确认弹窗
     *
     * @param title   弹窗头
     * @param message 询问内容
     * @return boolean
     */
    public static boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(stage);
        alert.initModality(Modality.APPLICATION_MODAL);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }


    private static void createScene(Parent root, String title, Boolean isResizable) {
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(isResizable);
        stage.initOwner(MainApplication.stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        // 主题切换
        String cssPath = ThemeManager.isDarkMode() ? getProperty.apply("style.dark.path") : getProperty.apply("style.light.path");
        scene.getStylesheets().clear();
        scene.getStylesheets().add(WindowUtil.class.getResource(cssPath).toExternalForm());

        // 跟随主窗口 Main.stage 显示
        double width = root.prefWidth(-1);
        double height = root.prefHeight(-1);
        double x = MainApplication.stage.getX() + (MainApplication.stage.getWidth() - width) / 2;
        double y = MainApplication.stage.getY() + (MainApplication.stage.getHeight() - height) / 2;
        stage.setX(x);
        stage.setY(y);
        stage.show();
    }

    /**
     * 显示加载窗口
     *
     * @param message 询问内容
     * @return Dialog实例
     */
    public static Dialog<Void> showLoading(String message) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("请稍候");
        dialog.setHeaderText(message);
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.setResizable(false);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(64, 64);
        dialog.getDialogPane().setContent(progressIndicator);

        dialog.show();
        return dialog;
    }

    /**
     * 关闭指定加载窗口
     *
     * @param loading Dialog实例
     */
    public static void closeLoading(Dialog<?> loading) {
        if (loading != null && loading.isShowing()) {
            Platform.runLater(() -> {
                if (loading.isShowing()) {
                    // 手动设置一个关闭结果。不然会关闭失败
                    ((Dialog<Object>) loading).setResult(ButtonType.CLOSE);
                    loading.hide();
                }
            });
        }
    }

    /**
     * 在 Parent 节点中查找指定 fx:id 的子控件
     *
     * @param parent   父节点（如 VBox, HBox, Pane）
     * @param targetId 要查找的 fx:id
     * @return Optional<Node>
     */
    public static Optional<Node> findChildById(Parent parent, String targetId) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (targetId.equals(child.getId())) {
                return Optional.of(child);
            }
            if (child instanceof Parent subParent) {
                Optional<Node> found = findChildById(subParent, targetId);
                if (found.isPresent()) return found;
            }
        }
        return Optional.empty();
    }

    /**
     * 渲染udp模式下目标主机输入框的界面
     */
    public static HBox genUdpTargetHostHBoxView(List<String> hostList) {

        Label targetHostLabel = new Label("目标主机：");
        targetHostLabel.setPrefHeight(30);

        ComboBox<String> targetHostCombo = new ComboBox<>();
        targetHostCombo.setId("targetHostField");
        targetHostCombo.setPrefWidth(140);
        targetHostCombo.setPrefHeight(30);
        targetHostCombo.setPromptText("IP/域名");
        targetHostCombo.setEditable(true);

        Label colonLabel = new Label(":");
        colonLabel.setPrefHeight(30);
        colonLabel.setPrefWidth(5);

        ComboBox<String> targetPortCombo = new ComboBox<>();
        targetPortCombo.setId("targetPortField");
        targetPortCombo.setPrefWidth(85);
        targetPortCombo.setPrefHeight(30);
        targetPortCombo.setPromptText("端口");
        targetPortCombo.setEditable(true);

        if (!hostList.isEmpty()) {
            Set<String> hosts = hostList.stream()
                    .filter(host -> !host.isEmpty())
                    .map(h -> h.split(":")[0])
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<String> posts = hostList.stream()
                    .filter(host -> !host.isEmpty())
                    .map(p -> p.split(":")[1])
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            targetHostCombo.getItems().addAll(hosts);
            targetPortCombo.getItems().addAll(posts);
//            for (String host : hostList) {
//                if ("".equals(host)) {
//                    continue;
//                }
//                targetHostCombo.getItems().add(host.split(":")[0]);
//                targetPortCombo.getItems().add(host.split(":")[1]);
//            }
        }
        return new HBox(5, targetHostLabel, targetHostCombo, colonLabel, targetPortCombo);
    }

    /**
     * 显示tcp服务端扩展选项
     */
    public static Optional<Map<String, String>> showTcpServerExtensionOption() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("TCP Server配置参数");

        ButtonType okButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField heartbeatField = new TextField("3600");

        ComboBox<String> maxPacketBox = new ComboBox<>();
        maxPacketBox.setEditable(true);
        maxPacketBox.getItems().addAll("auto", "128", "256", "512", "1024", "2048", "4096");
        maxPacketBox.setValue("auto");

        grid.add(new Label("心跳时间(秒):"), 0, 0);
        grid.add(heartbeatField, 1, 0);
        grid.add(new Label("最大接收包长(字节):"), 0, 1);
        grid.add(maxPacketBox, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Node confirmBtn = dialog.getDialogPane().lookupButton(okButton);
        confirmBtn.disableProperty().bind(
                maxPacketBox.getEditor().textProperty().isEmpty()
        );
        // 拦截确认 + 验证逻辑
        confirmBtn.addEventFilter(ActionEvent.ACTION, event -> {
            String maxPacket = maxPacketBox.getEditor().getText();
            String heartbeatText = heartbeatField.getText();
            try {
                int heartbeat = Integer.parseInt(heartbeatText);
                if (heartbeat < 10 || heartbeat > 86400) {
                    showTip("心跳时间必须在 10~86400 秒之间！", "⚠️警告消息");
                    event.consume();
                    return;
                }
                if (!"auto".equalsIgnoreCase(maxPacket)) {
                    int val = Integer.parseInt(maxPacket);
                    if (val < 128 || val > 32768) {
                        showTip("最大接收包长必须为 \"auto\" 或 128~32768 之间的整数！", "⚠️警告消息");
                        event.consume();
                        return;
                    }
                }
                Map<String, String> result = new HashMap<>();
                result.put("heartbeat", heartbeatText);
                result.put("maxPacket", maxPacket);
                dialog.setResult(result);
            } catch (NumberFormatException e) {
                showTip("请输入有效的数字格式！", "⚠️警告消息");
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == okButton ? dialog.getResult() : null);
        return dialog.showAndWait();
    }
}
