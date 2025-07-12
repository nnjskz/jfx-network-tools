/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import cn.nnjskz.jfx.utils.AppExecutors;
import cn.nnjskz.jfx.utils.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static cn.nnjskz.jfx.utils.FileUtil.getWritableAppDataDir;
import static cn.nnjskz.jfx.utils.ResourceBundleUtil.getProperty;
import static cn.nnjskz.jfx.utils.SunRiseSet.isSystemInDarkMode;

public class MainApplication extends Application {
    public static Stage stage;

    @Override
    public void start(Stage stage) throws IOException {
        MainApplication.stage = stage;
        stage.setTitle("JFX Network Tools");

        // 构建主界面与底部状态栏
        BorderPane root = new BorderPane();
        URL url = MainApplication.class.getResource(getProperty.apply("main.view.path"));
        Node mainView = FXMLLoader.load(Objects.requireNonNull(url));
        root.setCenter(mainView);
        javafx.scene.control.Label footer = new javafx.scene.control.Label("© 2025 Jensen - MIT License");
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: gray; -fx-padding: 5px;");

        HBox footerBox = new HBox(footer);
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        footerBox.setPadding(new javafx.geometry.Insets(0, 10, 5, 0));

        root.setBottom(footerBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMinWidth(775);
        // 根据屏幕高度-100
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setMinHeight(screenBounds.getHeight() - 100);
        dynamicUserAgentStylesheet(scene);
        stage.show();

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            setupDockIcon();
        }
        if (SystemTray.isSupported()) {
            setupSystemTray(stage);
        }
        // 关闭主窗口监听器
        stage.setOnCloseRequest(e -> {
            AppExecutors.getInstance().shutdown();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        try {
            PrintStream log = new PrintStream(getWritableAppDataDir()+"/logs/jfx-launch.log");
            System.setOut(log);
            System.setErr(log);
            System.out.println(">>> MainApplication.main started");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        launch();
    }

    /**
     * 设置系统托盘
     */
    private void setupSystemTray(Stage stage) {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(getProperty.apply("sys.tray.path")));

        PopupMenu popup = new PopupMenu();
        MenuItem openItem = new MenuItem("显示窗口");
        MenuItem exitItem = new MenuItem("退出");

        openItem.addActionListener(e -> Platform.runLater(() -> {
            stage.show();
            stage.toFront();
        }));
        exitItem.addActionListener(e -> {
            tray.remove(tray.getTrayIcons()[0]);
            Platform.exit();
            System.exit(0);
        });

        popup.add(openItem);
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, "App Tray", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> Platform.runLater(() -> {
            stage.show();
            stage.toFront();
        }));

        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 设置 macOS Dock 图标
     */
    private void setupDockIcon() {
        try {
            Taskbar taskbar = Taskbar.getTaskbar();
            Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(getProperty.apply("mac.dock.path")));
            taskbar.setIconImage(image);
        } catch (Exception e) {
            System.err.println("macOS Dock 图标设置失败: " + e.getMessage());
        }
    }

    /**
     * 动态调整系统主题
     */
    private void dynamicUserAgentStylesheet(Scene scene) {
        Runnable themeSwitcher = () -> {
           boolean darkMode = isSystemInDarkMode();
            Platform.runLater(() -> {
                // atlantafx
                Application.setUserAgentStylesheet(darkMode ? new CupertinoDark().getUserAgentStylesheet() : new CupertinoLight().getUserAgentStylesheet());
                // 自定义主题
                String cssPath = darkMode ? getProperty.apply("style.dark.path") : getProperty.apply("style.light.path");
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                // 通知其他自定义样式的变更
                ThemeManager.setDarkMode(darkMode);
            });
        };
        AppExecutors.getInstance().getScheduledTaskExecutor().scheduleAtFixedRate(themeSwitcher, 0, 30, TimeUnit.SECONDS);
    }
}