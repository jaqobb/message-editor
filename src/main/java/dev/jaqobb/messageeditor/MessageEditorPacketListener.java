/*
 * MIT License
 *
 * Copyright (c) 2020 Jakub Zagórski (jaqobb)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.jaqobb.messageeditor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.util.regex.Matcher;

public final class MessageEditorPacketListener extends PacketAdapter {

	private final MessageEditorPlugin plugin;

	public MessageEditorPacketListener(MessageEditorPlugin plugin) {
		super(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.CHAT);
		this.plugin = plugin;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		WrappedChatComponent message = packet.getChatComponents().read(0);
		MessageEdit messageEdit = null;
		Matcher messageEditMatcher = null;
		for (MessageEdit currentMessageEdit : this.plugin.getMessageEdits()) {
			Matcher currentMessageEditMatcher = currentMessageEdit.getMessageBeforePattern().matcher(message.getJson());
			if (currentMessageEditMatcher.matches()) {
				messageEdit = currentMessageEdit;
				messageEditMatcher = currentMessageEditMatcher;
				break;
			}
		}
		if (messageEdit != null && messageEditMatcher != null) {
			packet.getChatComponents().write(0, WrappedChatComponent.fromJson(messageEditMatcher.replaceAll(messageEdit.getMessageAfter())));
		}
	}
}
