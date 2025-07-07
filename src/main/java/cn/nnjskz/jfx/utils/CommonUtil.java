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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static cn.nnjskz.jfx.MainApplication.stage;

public class CommonUtil {

    /**
     * 通用切换页面
     * @param fxml fxml路径
     * @param width 宽度；0代表不设置
     * @param height 高度；0代表不设置
     */
    public static void changeView(String fxml,Integer width,Integer height) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(fxml));
        Scene scene;
        if (width != 0 && height != 0) {
            scene = new Scene(fxmlLoader.load(), width, height);
        }else {
            scene = new Scene(fxmlLoader.load());
        }
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    /**
     * 弹出新窗口
     * @param fxml fxml路径
     * @param tClass fxml加载类
     * @param title 标头
     * @param isResizable 是否可放大缩小
     * @return loader
     */
    public static FXMLLoader showWindow(String fxml,String title,Boolean isResizable,Class<?> tClass) throws IOException {
        FXMLLoader loader = new FXMLLoader(tClass.getResource(fxml));
        Parent root = loader.load();
        createScene(root,title,isResizable);
        return loader;
    }

    /**
     * 显示提示框
     * @param message 提示消息
     * @param title 标头
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
     * @param title 弹窗头
     * @param message 询问内容
     * @return  boolean
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


    private static void createScene(Parent root, String title,Boolean isResizable) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.setResizable(isResizable);
        stage.initOwner(MainApplication.stage);
        stage.initModality(Modality.APPLICATION_MODAL); // 模态窗口

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
     * @param message 询问内容
     * @return  Dialog实例
     */
    public static Dialog<Void> showLoading(String message) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("请稍候");
        dialog.setHeaderText(message);
        dialog.initOwner(stage); // 注意 stage 应为 MainApplication 的静态成员
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
     * @param parent 父节点（如 VBox, HBox, Pane）
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
     * 生成udp模式下目标主机输入框的界面，以供渲染
     */
    public static HBox genUdpTargetHostHBoxView(List<String> hostList) {

        Label targetHostLabel = new Label("目标主机：");
        targetHostLabel.setPrefHeight(30);

        ComboBox<String> targetIpCombo = new ComboBox<>();
        targetIpCombo.setId("targetIpField");
        targetIpCombo.setPrefWidth(140);
        targetIpCombo.setPrefHeight(30);
        targetIpCombo.setPromptText("IP/域名");
        targetIpCombo.setEditable(true);

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
            for (String host : hostList) {
                if ("".equals(host)) {continue;}
                targetIpCombo.getItems().add(host.split(":")[0]);
                targetPortCombo.getItems().add(host.split(":")[1]);
            }
        }
        return new HBox(5,targetHostLabel, targetIpCombo, colonLabel, targetPortCombo);
    }
}
