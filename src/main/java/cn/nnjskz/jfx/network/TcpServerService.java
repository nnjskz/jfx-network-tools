/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TcpServerService {
    private final Integer port;
    private ServerSocket serverSocket;
    private final Map<Socket,OutputStream> writersMap = new ConcurrentHashMap<>();
    private ReadThread readThread;
    private Consumer<Map<Socket,byte[]>> receive;
    private Consumer<String> infoCall;
    private Runnable onDisconnect;
    private final Integer bufSize;

    public TcpServerService(Integer port,Integer bufSize) {
        this.port = port;
        this.bufSize = bufSize;
    }

    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }
    public void setReceive(Consumer<Map<Socket,byte[]>> receive) {
        this.receive = receive;
    }

    public void setInfoCall(Consumer<String> infoCall) {
        this.infoCall = infoCall;
    }

    public Map<Socket, OutputStream> getWritersMap() {
        return writersMap;
    }

    public void openConnect() throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    writersMap.put(socket,socket.getOutputStream());
                    this.readThread = new ReadThread(this,socket);
                    this.readThread.start();
                    String notify = "客户端->" + socket.getRemoteSocketAddress() + "已连接";
                    if (infoCall != null) {
                        infoCall.accept(notify);
                    }
                }
            } catch (IOException ignored) {}
        }).start();
    }

    /**
     * 消息发送（广播）
     * @param message 消息数据
     */
    public void send(byte[] message){
        Iterator<Map.Entry<Socket, OutputStream>> iterator = writersMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Socket, OutputStream> entry = iterator.next();
            try {
                entry.getValue().write(message);
                entry.getValue().flush();
            } catch (IOException e) {
                // 移除已断开连接
                try {
                    entry.getKey().close();
                    entry.getValue().close();
                } catch (IOException ignored) {
                }
                iterator.remove();
                if (infoCall != null) {
                    infoCall.accept("客户端->" + entry.getKey().getRemoteSocketAddress() + " 已断开（发送失败）");
                }
            }
        }
    }

    /**
     * 消息发送
     * @param socket socket实例
     * @param message 消息数据
     */
    public void send(byte[] message, Socket socket){
        OutputStream out = writersMap.get(socket);
        if(out != null){
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
            if (readThread != null && readThread.isAlive()) {
                readThread.interrupt();
            }
            if (!writersMap.isEmpty()) {
                writersMap.forEach((k,v)->{
                    try {
                        k.close();
                        v.close();
                    } catch (IOException e) {
                        throw new RuntimeException("关闭连接失败: " + e.getMessage(), e);
                    }
                });
                writersMap.clear();
            }
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

    private static class ReadThread extends Thread {
        private final TcpServerService server;
        private final Socket socket;
        private final Map<Socket,byte[]> receiveMap = new ConcurrentHashMap<>();

        public ReadThread(TcpServerService server, Socket socket) {
            this.server = server;
            this.socket = socket;
        }

        @Override
        public void run() {
            String notify = "";
            try (InputStream in = socket.getInputStream()) {
                byte[] buffer = new byte[server.bufSize];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    receiveMap.clear();
                    byte[] actual = new byte[len];
                    System.arraycopy(buffer, 0, actual, 0, len);
                    receiveMap.put(socket,actual);
                    server.receive.accept(receiveMap);
                }
                notify = "客户端->" + socket.getRemoteSocketAddress() + "已断开";
            } catch (IOException e) {
                if (server.onDisconnect != null){
                    server.onDisconnect.run();
                }
            }

            server.writersMap.remove(socket);

            if (server.infoCall != null) {
                server.infoCall.accept(notify);
            }
        }
    }
}

