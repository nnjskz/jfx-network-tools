/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.model;

public class ChatMessage {
    private final String content;
    private final boolean sentByMe;

    public ChatMessage(String content, boolean sentByMe) {
        this.content = content;
        this.sentByMe = sentByMe;
    }

    public String getContent() { return content; }
    public boolean isSentByMe() { return sentByMe; }
}
