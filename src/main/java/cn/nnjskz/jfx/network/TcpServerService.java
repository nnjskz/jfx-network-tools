/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.network;

import cn.nnjskz.jfx.utils.AppExecutors;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TcpServerService {
    private final Integer port;
    private ServerSocket serverSocket;
    private final Map<Socket, OutputStream> writersMap = new ConcurrentHashMap<>();
    private Consumer<Map<Socket, byte[]>> receive;
    private Consumer<String> infoCall;
    private Runnable onDisconnect;
    private final String bufSize;
    private final Integer heartbeat;

    public TcpServerService(Integer port, String bufSize, Integer heartbeat) {
        this.port = port;
        this.bufSize = bufSize;
        this.heartbeat = heartbeat;
    }

    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    public void setReceive(Consumer<Map<Socket, byte[]>> receive) {
        this.receive = receive;
    }

    public void setInfoCall(Consumer<String> infoCall) {
        this.infoCall = infoCall;
    }

    public Map<Socket, OutputStream> getWritersMap() {
        return writersMap;
    }

    // 打开连接
    public void openConnect() throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    writersMap.put(socket, socket.getOutputStream());
                    ReadThread readThread = new ReadThread(this, socket);
                    AppExecutors.getInstance().getBackgroundCachedExecutor().submit(readThread);
                    if (infoCall != null) {
                        infoCall.accept("客户端->" + socket.getRemoteSocketAddress() + "已连接");
                    }
                }
            } catch (IOException ignored) {}
        }).start();
    }

    public void send(byte[] message) {
        Iterator<Map.Entry<Socket, OutputStream>> iterator = writersMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Socket, OutputStream> entry = iterator.next();
            try {
                entry.getValue().write(message);
                entry.getValue().flush();
            } catch (IOException e) {
                try {
                    entry.getKey().close();
                } catch (IOException ignored) {}
                iterator.remove();
                if (infoCall != null) {
                    infoCall.accept("客户端->" + entry.getKey().getRemoteSocketAddress() + " 已断开（发送失败）");
                }
            }
        }
    }

    public void send(byte[] message, Socket socket) {
        OutputStream out = writersMap.get(socket);
        if (out != null) {
            try {
                out.write(message);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException("发送数据失败: " + e.getMessage(), e);
            }
        }
    }

    public void close() {
        try {
//            threadPool.shutdownNow();
            for (Map.Entry<Socket, OutputStream> entry : writersMap.entrySet()) {
                try {
                    entry.getKey().shutdownInput();
                    entry.getKey().close();
                    entry.getValue().close();
                } catch (IOException ignored) {}
            }
            writersMap.clear();
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            receive = null;
            infoCall = null;
            onDisconnect = null;
        } catch (IOException e) {
            throw new RuntimeException("关闭连接失败: " + e.getMessage(), e);
        }
    }

    private record ReadThread(TcpServerService server, Socket socket) implements Runnable {
        @Override
            public void run() {
                String notify = "";
                try (InputStream in = socket.getInputStream()) {
                    // 设置心跳超时
                    socket.setSoTimeout(server.heartbeat * 1000);
                    // 设置最大接收包长度
                    int readSize = "auto".equals(server.bufSize)?Math.min(32 * 1024, socket.getReceiveBufferSize()):Integer.parseInt(server.bufSize);
                    byte[] buffer = new byte[readSize];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        byte[] actual = Arrays.copyOf(buffer, len);
                        Map<Socket, byte[]> receiveMap = new HashMap<>();
                        receiveMap.put(socket, actual);
                        if (server.receive != null) {
                            server.receive.accept(receiveMap);
                        }
                    }
                    notify = "客户端->" + socket.getRemoteSocketAddress() + "已断开";
                } catch (SocketTimeoutException timeout) {
                    notify = "客户端->" + socket.getRemoteSocketAddress() + "超时断开";
                } catch (IOException e) {
                    if (server.onDisconnect != null) {
                        server.onDisconnect.run();
                    }
                } finally {
                    server.writersMap.remove(socket);
                    if (server.infoCall != null) {
                        server.infoCall.accept(notify);
                    }
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
}
