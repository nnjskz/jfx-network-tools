/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.function.Consumer;

public class TcpClientService {
    private final String ip;
    private final Integer port;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ReadThread readThread;

    private Consumer<byte[]> receive;
    public void setReceive(Consumer<byte[]> receive) {
        this.receive = receive;
    }
    private Runnable onDisconnect;
    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    public TcpClientService(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public Boolean connect(){
        this.socket = new Socket();
        try {
            this.socket.setKeepAlive(true);
            SocketAddress socketAddress = new InetSocketAddress(ip, port);
            this.socket.connect(socketAddress, 10000);

            this.outputStream = this.socket.getOutputStream();
            this.inputStream = this.socket.getInputStream();

            this.readThread = new ReadThread();
            this.readThread.start();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        if (this.readThread != null) {
            this.readThread.interrupt();
        }
        if (this.socket != null) {
           try {
               this.socket.close();
               this.socket = null;
           }catch (Exception e){
               e.printStackTrace();
           }
        }
        receive = null;
        onDisconnect = null;
    }

    public void send(byte[] bytes) {
        try {
            this.outputStream.write(bytes);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 判断是否断开连接，断开返回true,没有返回false
     * @param socket 套接字对象
     * @return true / false
     */
    public boolean isServerClose(Socket socket){
        try{
            socket.sendUrgentData(0xFF);    // 发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            return false;
        }catch(Exception se){
            return true;
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[4096];
            try {
                while (!isInterrupted()) {
                    int size = TcpClientService.this.inputStream.read(buffer);
                    if (size == -1) {
                        // 服务端断开连接
                        if (onDisconnect != null) {
                            onDisconnect.run();
                        }
                        break;
                    }
                    if (size > 0 && receive != null) {
                        receive.accept(new String(buffer, 0, size).getBytes());
                    }
                }
            } catch (Throwable e) {
                System.err.println(e.getMessage());
                if (onDisconnect != null) {
                    onDisconnect.run();
                }
            }
        }
    }
}
