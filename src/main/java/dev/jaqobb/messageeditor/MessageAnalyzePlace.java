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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum MessageAnalyzePlace {

	CHAT(PacketType.Play.Server.CHAT, (byte) 0, (byte) 1),
	ACTION_BAR(PacketType.Play.Server.CHAT, (byte) 2),
	KICK(PacketType.Play.Server.KICK_DISCONNECT),
	DISCONNECT(PacketType.Login.Server.DISCONNECT);

	private final PacketType packetType;
	private final Set<Byte> chatTypes;

	private MessageAnalyzePlace(PacketType packetType, byte... chatTypes) {
		this.packetType = packetType;
		this.chatTypes = new HashSet<>(chatTypes.length);
		for (byte chatType : chatTypes) {
			this.chatTypes.add(chatType);
		}
	}

	public PacketType getPacketType() {
		return this.packetType;
	}

	public Set<Byte> getChatTypes() {
		return Collections.unmodifiableSet(this.chatTypes);
	}

	public static MessageAnalyzePlace fromPacketType(PacketType packetType) {
		return fromPacketType(packetType, (byte) -1);
	}

	public static MessageAnalyzePlace fromPacketType(PacketType packetType, byte chatType) {
		return Arrays.stream(values())
			.filter(place -> place.packetType == packetType)
			.filter(place -> place.chatTypes.contains(chatType) || chatType == -1)
			.findFirst()
			.orElse(null);
	}
}
