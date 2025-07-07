/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.controller;

import cn.nnjskz.jfx.MainApplication;
import cn.nnjskz.jfx.network.TcpClientService;
import cn.nnjskz.jfx.network.TcpServerService;
import cn.nnjskz.jfx.network.UdpService;
import cn.nnjskz.jfx.utils.AppExecutors;
import cn.nnjskz.jfx.utils.DateUtil;
import cn.nnjskz.jfx.utils.FileUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static cn.nnjskz.jfx.utils.CommonUtil.*;
import static cn.nnjskz.jfx.utils.FileUtil.*;
import static cn.nnjskz.jfx.utils.ResourceBundleUtil.getProperty;
import static cn.nnjskz.jfx.utils.SocketUtil.*;

public class MainController {
    @FXML
    private ComboBox<String> modeCombo;
    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private Button connHistoryBtn;
    @FXML
    private Button connectBtn;
    @FXML
    private Button disconnectBtn;
    @FXML
    private TextArea sendArea;
    @FXML
    private TextArea chatArea;
    @FXML
    private HBox sendHBox;
    @FXML
    private CheckBox autoAnswerCheck;
    @FXML
    private CheckBox hexSendCheck;
    @FXML
    private CheckBox autoSendCheck;
    @FXML
    private TextField sendIntervalField;
    @FXML
    private Button sendBtn;
    @FXML
    private Button sendStopBtn;
    @FXML
    private Button clearSendBtn;
    @FXML
    private CheckBox hexRecvCheck;
    @FXML
    private Button clearRecvBtn;
    @FXML
    private Button saveLogBtn;
    @FXML
    private Label statusLabel;
    @FXML
    private Label byteCountLabel;
    @FXML
    private Label clientNumLabel;

    private long sentBytes = 0;
    private long receivedBytes = 0;
    private boolean isConnected = false;

    private ScheduledFuture<?> scheduledFuture;
    private TcpClientService tcpClientService;
    private TcpServerService tcpServerService;
    private UdpService udpService;
    private final String statusColor = "#019801";

    @FXML
    public void initialize() throws IOException {
        Files.createDirectories(Paths.get(FileUtil.currentDir));
        modeCombo.getItems().addAll("TCP Client", "TCP Server", "UDP");
        modeCombo.getSelectionModel().selectFirst();
        statusLabel.setText("未连接");
        statusLabel.setStyle("-fx-text-fill: #da1010;");

        ipField.textProperty().addListener((observable, oldValue, newValue) -> validateInputs());
        portField.textProperty().addListener((observable, oldValue, newValue) -> validateInputs());
        sendArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if ("".equals(sendArea.getText())) {
                sendBtn.setDisable(true);
            } else {
                sendBtn.setDisable(!isConnected);
            }
        });
        modeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if ("TCP Server".equals(newValue) || "UDP".equals(newValue)) {
                ipField.setDisable(true);
                ipField.setText(Objects.requireNonNull(getLocalHostExactAddress()).getHostAddress());
                portField.setText(getAvailablePort().toString());
                connectBtn.setText("打开");
                disconnectBtn.setText("关闭");
                connHistoryBtn.setDisable(true);
            } else {
                ipField.setDisable(false);
                ipField.setText("");
                portField.setText("");
                connectBtn.setText("连接");
                disconnectBtn.setText("断开");
                connHistoryBtn.setDisable(false);
                clientNumLabel.setVisible(false);
            }
        });

        //加载上一次编辑的内容
        StringBuilder lastContent = readFile(getProperty.apply("last.send.path"));
        int len = lastContent.length();
        if (len >= 1 && lastContent.charAt(len - 1) == '\n') {
            lastContent.deleteCharAt(len - 1);
            if (len >= 2 && lastContent.charAt(len - 2) == '\r') {
                lastContent.deleteCharAt(len - 2);
            }
        }
        sendArea.setText(lastContent.toString());
    }

    @FXML
    private void onConnect() {
        String mode = modeCombo.getValue();
        String ip = ipField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        switch (mode) {
            case "TCP Client" -> {
                Dialog<Void> connectDialog = showLoading("正在尝试连接中...");
                AppExecutors.getInstance().getBackgroundExecutor().execute(()->{
                    tcpClientService = new TcpClientService(ip, port);
                    Boolean success = tcpClientService.connect();
                    Platform.runLater(() -> {
                        closeLoading(connectDialog);
                        if (success) {
                            ipField.setDisable(true);
                            portField.setDisable(true);
                            modeCombo.setDisable(true);
                            connHistoryBtn.setDisable(true);
                            connectBtn.setDisable(true);
                            disconnectBtn.setDisable(false);
                            sendBtn.setDisable("".equals(sendArea.getText()));
                            statusLabel.setText("已连接->" + ip + ":" + port);
                            statusLabel.setStyle("-fx-text-fill: "+statusColor+";");
                            isConnected = true;

                            // 监听来自 tcpClientService 的消息
                            tcpClientService.setOnDisconnect(() -> Platform.runLater(this::onDisconnect));
                            tcpClientService.setReceive(bytes -> {
                                appendReceivedData(bytes, "来自TCP服务端:" + ip + ":" + port + "<<");
                                // 如果开启自动应答
                                if (autoAnswerCheck.isSelected()) {
                                    // 默认回复"received"（后续版本更新可自定义应答模板）
                                    tcpClientService.send("received".getBytes());
                                    appendMessage("系统消息>>自动应答", "received".getBytes(), false);
                                }
                            });

                            // 记录本次连接进入历史
                            AppExecutors.getInstance().getBackgroundExecutor().execute(() -> {
                                try {
                                    StringBuilder content = readFile(getProperty.apply("conn.history.path"));
                                    if (!content.toString().contains(ip + ":" + port)) {
                                        // 保存新连接
                                        writeFile(ip+":"+port, getProperty.apply("conn.history.path"), true);
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            appendMessage("系统消息>>", "服务已连接".getBytes(), false);
                        } else {
                            // 弹窗提示失败
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("连接失败");
                            alert.setHeaderText("无法连接到服务器");
                            alert.setContentText("请检查IP/域名和端口是否正确，或服务器是否可用。");
                            alert.initOwner(MainApplication.stage);
                            alert.showAndWait();
                            appendMessage("系统消息>>", "无法连接到服务器，请检查IP和端口是否正确，或服务器是否可用".getBytes(), false);
                        }
                    });
                });

            }
            case "TCP Server" -> {
                try {
                    tcpServerService = new TcpServerService(Integer.parseInt(portField.getText().trim()), 4096);
                    tcpServerService.openConnect();
                    Platform.runLater(() -> {
                        portField.setDisable(true);
                        modeCombo.setDisable(true);
                        connHistoryBtn.setDisable(true);
                        connectBtn.setDisable(true);
                        disconnectBtn.setDisable(false);
                        sendBtn.setDisable("".equals(sendArea.getText()));
                        statusLabel.setText("服务已启动->" + ip + ":" + port);
                        statusLabel.setStyle("-fx-text-fill: "+statusColor+";");
                        isConnected = true;
                        clientNumLabel.setVisible(true);

                        // 监听来自 tcpServerService 的消息
                        tcpServerService.setInfoCall(msg -> {
                            Platform.runLater(() -> {
                                appendMessage("系统消息>>", msg.getBytes(), false);
                                // 更新底部客户端数量
                                clientNumLabel.setText("客户端数量：" + tcpServerService.getWritersMap().size());
                                onStopSend();
                            });
                        });
                        tcpServerService.setReceive(receive ->
                                receive.forEach(((socket, bytes) -> {
                                    AppExecutors.getInstance().getBackgroundExecutor().execute(() -> {
                                        String host = socket.getRemoteSocketAddress().toString().replaceAll("/", "");
                                        appendReceivedData(bytes, "来自TCP客户端:" + host + "<<");
                                        // 如果开启自动应答
                                        if (autoAnswerCheck.isSelected()) {
                                            // 默认回复"received"（后续版本更新可自定义应答模板）
                                            byte[] received = "received".getBytes();
                                            tcpServerService.send(received, socket);
                                            appendMessage("系统消息>>自动应答", "received".getBytes(), false);
                                        }
                                    });
                                })));
                        tcpServerService.setOnDisconnect(() -> Platform.runLater(this::onDisconnect));
                        appendMessage("系统消息>>", "TCP服务启动成功，等待客户端连接...".getBytes(), false);
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showTip("服务端启动失败：" + e.getMessage(), "错误提示");
                    });
                }
            }
            case "UDP" -> {
                try {
                    udpService = new UdpService(Integer.parseInt(portField.getText().trim()), 4096);
                    udpService.openUdp();
                    Platform.runLater(() -> {
                        portField.setDisable(true);
                        modeCombo.setDisable(true);
                        connHistoryBtn.setDisable(true);
                        connectBtn.setDisable(true);
                        disconnectBtn.setDisable(false);
                        sendBtn.setDisable("".equals(sendArea.getText()));
                        statusLabel.setText("UDP已启动->" + ip + ":" + port);
                        statusLabel.setStyle("-fx-text-fill: "+statusColor+";");
                        isConnected = true;
                        // 为目标主机输入框加载历史输入
                        try {
                            StringBuilder targetHost = readFile(getProperty.apply("target.host.history.path"));
                            String[] hosts = targetHost.toString().split("\n");
                            sendHBox.getChildren().add(genUdpTargetHostHBoxView(Arrays.asList(hosts)));
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                            throw new RuntimeException(e);
                        }

                        udpService.setReceive(receive ->
                                receive.forEach(((socket, bytes) -> {
                                    String host = socket.getSocketAddress().toString().replaceAll("/", "");
                                    appendReceivedData(bytes.getBytes(), "来自UDP客户端:" + host + "<<");
                                    // 如果开启自动应答
                                    if (autoAnswerCheck.isSelected()) {
                                        // 默认回复"received"（后续版本更新可自定义应答模板）
                                        byte[] received = "received".getBytes();
                                        String sendRes = udpService.send(received, host.split(":")[0], Integer.parseInt(host.split(":")[1]));
                                        if (!"".equals(sendRes)){
                                            showTip("应答无法发送!!!"+sendRes+"，请检查IP/域名和端口是否可用", "⚠️警告消息");
                                            return;
                                        }
                                        appendMessage("系统消息>>自动应答", "received".getBytes(), false);
                                    }
                                })));
                        appendMessage("系统消息>>", "UDP启动成功!".getBytes(), false);
                    });

                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showTip("UDP启动失败：" + e.getMessage(), "错误提示");
                    });
                }
            }
        }
    }

    @FXML
    private void onHandDisconnect() {
        onDisconnect();
    }

    /**
     * 断开连接
     */
    private void onDisconnect() {
        // 断开连接并清理线程
        if (isConnected) {
            Platform.runLater(() -> {
                ipField.setDisable(false);
                portField.setDisable(false);
                modeCombo.setDisable(false);
                connectBtn.setDisable(false);
                disconnectBtn.setDisable(true);
                statusLabel.setText("未连接");
                statusLabel.setStyle("-fx-text-fill: #da1010;");
                connHistoryBtn.setDisable(false);
                isConnected = false;
                sendBtn.setDisable(true);
                onStopSend();
                String selectedItem = modeCombo.getSelectionModel().getSelectedItem();
                if ("TCP Server".equals(selectedItem)) {
                    tcpServerService.close();
                    appendMessage("系统消息>>", "服务已关闭".getBytes(), false);
                    clientNumLabel.setVisible(false);
                } else if ("UDP".equals(selectedItem)) {
                    udpService.close();
                    appendMessage("系统消息>>", "UDP已关闭".getBytes(), false);
                    // 清除udp模式下渲染的 目标主机输入框的界面
                    for (Node node : sendHBox.getChildren()) {
                        if (node instanceof HBox) {
                            sendHBox.getChildren().remove(node);
                            break;
                        }
                    }
                } else {
                    tcpClientService.close();
                }
            });
        }
    }

    @FXML
    private void onSend() {
        String data = sendArea.getText();
        String mode = modeCombo.getValue();
        byte[] payload = data.getBytes();
        boolean autoSend = autoSendCheck.isSelected() && !"".equals(sendIntervalField.getText());

        Runnable task = () -> {
            switch (mode) {
                case "TCP Client" -> {
                    appendSendingData(payload);
                    tcpClientService.send(payload);
                }
                case "TCP Server" -> {
                    if (tcpServerService.getWritersMap().isEmpty()) {
                        if (autoSend) {
                            onStopSend();
                        }
                        Platform.runLater(() -> showTip("无法发送，暂无客户端连接", "⚠️警告消息"));
                        return;
                    }
                    appendSendingData(payload);
                    tcpServerService.send(payload);
                }
                case "UDP" -> {
                    AtomicReference<String> ip = new AtomicReference<>();
                    AtomicReference<String> port = new AtomicReference<>();
                    Optional<Node> targetIpField = findChildById(sendHBox, "targetIpField");
                    Optional<Node> targetPortField = findChildById(sendHBox, "targetPortField");
                    targetIpField.ifPresent(node -> {
                        if (node instanceof ComboBox<?> cb) {
                            ip.set((String) cb.getValue());
                        }
                    });
                    targetPortField.ifPresent(node -> {
                        if (node instanceof ComboBox<?> cb) {
                            port.set((String) cb.getValue());
                        }
                    });
                    if (Objects.isNull(ip.get()) || Objects.isNull(port.get())) {
                        if (autoSend) {
                            onStopSend();
                        }
                        Platform.runLater(() -> showTip("无法发送，请输入目标主机", "⚠️警告消息"));
                        return;
                    }
                    appendSendingData(payload);
                    String sendRes = udpService.send(payload, ip.get(), Integer.parseInt(port.get()));
                    if (!"".equals(sendRes)){
                        showTip("消息已发出，但对方也许无法收到!!!原因："+sendRes+"，请检查IP/域名和端口是否可用", "⚠️警告消息");
                        return;
                    }
                    // 记录本次目标主机进入历史
                    AppExecutors.getInstance().getBackgroundExecutor().execute(() -> {
                        try {
                            StringBuilder content = readFile(getProperty.apply("target.host.history.path"));
                            if (!content.toString().contains(ip + ":" + port)) {
                                writeFile(ip+":"+port, getProperty.apply("target.host.history.path"), true);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        };
        // 定时发送
        if (autoSend) {
            int interval = Integer.parseInt(sendIntervalField.getText());
            if (interval < 1000) {
                showTip("发送间隔必须在1000ms及以上时间!", "⚠️警告消息");
                return;
            }
            ScheduledExecutorService executor = AppExecutors.getInstance().getScheduledTaskExecutor();
            scheduledFuture = executor.scheduleAtFixedRate(task, 0, interval, TimeUnit.MILLISECONDS);
            sendStopBtn.setDisable(false);
            sendBtn.setDisable(true);
            sendArea.setDisable(true);
            clearSendBtn.setDisable(true);
            autoSendCheck.setDisable(true);
            sendIntervalField.setDisable(true);
            hexSendCheck.setDisable(true);
            autoAnswerCheck.setDisable(true);
        } else {
            task.run();
        }
        // 保存最后一次编辑
        AppExecutors.getInstance().getBackgroundExecutor().execute(() -> {
            try {
                writeFile(data, getProperty.apply("last.send.path"), false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    private void onStopSend() {
        if (scheduledFuture != null) {
            boolean cancel = scheduledFuture.cancel(true);
            if (cancel && scheduledFuture.isCancelled()) {
                sendStopBtn.setDisable(true);
                sendBtn.setDisable(!isConnected);
                sendArea.setDisable(false);
                clearSendBtn.setDisable(false);
                autoSendCheck.setDisable(false);
                sendIntervalField.setDisable(false);
                hexSendCheck.setDisable(false);
                autoAnswerCheck.setDisable(false);
            }
        }
    }

    @FXML
    private void onClearSend() {
        sendArea.clear();
    }

    @FXML
    private void onClearRecv() {
        boolean dialog = showConfirmationDialog("提示消息", "你确认要清空以上数据记录志吗?");
        if (dialog) {
            chatArea.clear();
            receivedBytes = 0;
            updateByteCount();
            clearRecvBtn.setDisable(true);
            saveLogBtn.setDisable(true);
        }
    }

    @FXML
    private void onSaveLog() {
        // 实现保存接收日志到文件
        boolean dialog = showConfirmationDialog("提示消息", "你确认要将以上数据记录为日志吗?");
        if (dialog) {
            String[] hosts = ipField.getText().split("\\.");
            StringBuilder host = new StringBuilder();
            for (String h : hosts) {
                host.append(h).append("_");
            }
            try {
                // 限制最多50份日志
                Map<String, Long> historyLogList = getHistoryLogList();
                if (historyLogList.size() >= 50) {
                    showTip("已达最大上限(50)!", "⚠️警告消息");
                    return;
                }
                String name = "chat_" + host + portField.getText() + "_" + new Date().getTime() + ".log";
                writeFile(chatArea.getText(), getProperty.apply("logs.path") + name, false);
                showTip("保存日志【" + name + "】成功!", "提示消息");
            } catch (Exception e) {
                showTip("保存失败!\n错误原因：" + e.getCause(), "错误消息");
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void openConnectHistory() throws IOException {
        // 打开历史连接窗口
        FXMLLoader fxmlLoader = showWindow(getProperty.apply("history.conn.view.path"), "连接历史", false, HistoryConnController.class);
        HistoryConnController controller = fxmlLoader.getController();
        controller.setOnSelected(item -> {
            ipField.setText(item.getIp());
            portField.setText(String.valueOf(item.getPort()));
            Stage stage = (Stage) ((Parent) fxmlLoader.getRoot()).getScene().getWindow();
            stage.close();
        });
    }

    @FXML
    private void onHistoryLogs() throws IOException {
        showWindow(getProperty.apply("log.list.view.path"), "日志列表", false, HistoryLogsController.class);
    }

    /**
     * 验证IP&端口输入框
     */
    private void validateInputs() {
        String ipText = ipField.getText();
        String portText = portField.getText();

        // 判断IP的完整性与正确性
        boolean ipValid = false;
        if (ipText != null && !ipText.isBlank()) {
            String ipv4Pattern = "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.|$)){4}$";
            ipValid = ipText.matches(ipv4Pattern);
        }

        // 判断端口的完整性与正确性
        boolean portValid = false;
        try {
            int port = Integer.parseInt(portText);
            portValid = port >= 1 && port <= 65535;
        } catch (NumberFormatException ignored) {
        }

        boolean valid = ipValid && portValid;
        connectBtn.setDisable(!valid);
    }

    /**
     * 接收消息
     *
     * @param data        数据
     * @param labelPrefix 标头
     */
    public void appendReceivedData(byte[] data, String labelPrefix) {
        appendMessage(labelPrefix, data, hexRecvCheck.isSelected());
        receivedBytes += data.length;
        clearRecvBtn.setDisable(false);
        saveLogBtn.setDisable(false);
    }

    /**
     * 发送消息
     *
     * @param data 数据
     */
    public void appendSendingData(byte[] data) {
        appendMessage("你>>", data, hexSendCheck.isSelected());
        sentBytes += data.length;
        clearRecvBtn.setDisable(false);
        saveLogBtn.setDisable(false);
    }

    /**
     * 追加消息到面板
     *
     * @param labelPrefix 标头
     * @param data        数据
     * @param isHex       是否十六进制
     */
    private void appendMessage(String labelPrefix, byte[] data, boolean isHex) {
        Platform.runLater(() -> {
            String displayText = isHex ? formatBytesToHex(data) : new String(data, StandardCharsets.UTF_8);
            String format = isHex ? "【HEX】" : "";
            String head = "[" + DateUtil.formatDate2String(new Date(), DateUtil.LONG_PATTERN) +
                    "] " + labelPrefix + " /" + data.length + "字节" + format + ":\n";
            chatArea.appendText(head);
            chatArea.appendText(displayText);
            chatArea.appendText("\n\n");
            updateByteCount();
        });
    }

    /**
     * 更新收/发字节数
     */
    private void updateByteCount() {
        byteCountLabel.setText(String.format("收：%d 字节  发：%d 字节", receivedBytes, sentBytes));
    }
}
