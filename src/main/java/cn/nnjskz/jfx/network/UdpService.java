/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.network;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class UdpService {
    private final Integer port;
    private final Integer bufSize;
    private DatagramSocket socket;
    private Consumer<Map<DatagramPacket,String>> receive;

    public UdpService(Integer port, Integer bufSize) {
        this.port = port;
        this.bufSize = bufSize;
    }

    public void setReceive(Consumer<Map<DatagramPacket, String>> receive) {
        this.receive = receive;
    }

    public void openUdp() throws IOException {
        socket = new DatagramSocket(port);
        new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    Map<DatagramPacket,String> receiveMap = new ConcurrentHashMap<>();
                    byte[] buf = new byte[bufSize];
                    DatagramPacket packet = new DatagramPacket(buf, bufSize);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                    receiveMap.put(packet, msg);
                    receive.accept(receiveMap);
                }
            }catch (IOException ignored) {}
        }).start();

    }

    public String send(byte[] msg, String targetIp, Integer targetPort) {
        try {
            DatagramPacket packet = new DatagramPacket(
                msg,
                msg.length,
                InetAddress.getByName(targetIp),
                targetPort
            );
            socket.send(packet);
            return "";
        } catch (IOException e) {
            return "send udp error: " + e.getMessage();
        }
    }

    public void close(){
        socket.close();
        receive = null;
    }
}
