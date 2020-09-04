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

package dev.jaqobb.messageeditor.data.place;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.util.Arrays;
import java.util.Objects;

public enum MessagePlace {

    GAME_CHAT(MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.CHAT, (byte) 0, EnumWrappers.ChatType.CHAT),
    SYSTEM_CHAT(MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.CHAT, (byte) 1, EnumWrappers.ChatType.SYSTEM),
    ACTION_BAR(MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.CHAT, (byte) 2, EnumWrappers.ChatType.GAME_INFO),
    KICK(MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.KICK_DISCONNECT),
    DISCONNECT(MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Login.Server.DISCONNECT),
    BOSS_BAR(MinecraftVersion.COMBAT_UPDATE, PacketType.Play.Server.BOSS);

    private final MinecraftVersion minimumRequiredMinecraftVersion;
    private final PacketType packetType;
    private final Byte chatType;
    private final EnumWrappers.ChatType chatTypeEnum;
    private boolean analyzingActivated;

    private MessagePlace(
        final MinecraftVersion minimumRequiredMinecraftVersion,
        final PacketType packetType
    ) {
        this(minimumRequiredMinecraftVersion, packetType, null, null);
    }

    private MessagePlace(
        final MinecraftVersion minimumRequiredMinecraftVersion,
        final PacketType packetType,
        final Byte chatType,
        final EnumWrappers.ChatType chatTypeEnum
    ) {
        this.minimumRequiredMinecraftVersion = minimumRequiredMinecraftVersion;
        this.packetType = packetType;
        this.chatType = chatType;
        this.chatTypeEnum = chatTypeEnum;
        this.analyzingActivated = false;
    }

    public MinecraftVersion getMinimumRequiredMinecraftVersion() {
        return this.minimumRequiredMinecraftVersion;
    }

    public PacketType getPacketType() {
        return this.packetType;
    }

    public Byte getChatType() {
        return this.chatType;
    }

    public EnumWrappers.ChatType getChatTypeEnum() {
        return this.chatTypeEnum;
    }

    public boolean isAnalyzingActivated() {
        return this.analyzingActivated;
    }

    public void setAnalyzingActivated(final boolean activated) {
        this.analyzingActivated = activated;
    }

    public static MessagePlace fromName(final String name) {
        return Arrays.stream(values())
            .filter(place -> place.name().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public static MessagePlace fromPacket(final PacketContainer packet) {
        if (packet.getType() != PacketType.Play.Server.CHAT) {
            return fromPacketType(packet.getType());
        }
        if (packet.getBytes().size() == 1) {
            return fromPacketType(packet.getType(), packet.getBytes().read(0));
        }
        return fromPacketType(packet.getType(), packet.getChatTypes().read(0));
    }

    public static MessagePlace fromPacketType(final PacketType packetType) {
        return Arrays.stream(values())
            .filter(place -> place.packetType == packetType)
            .findFirst()
            .orElse(null);
    }

    public static MessagePlace fromPacketType(
        final PacketType packetType,
        final Byte chatType
    ) {
        return Arrays.stream(values())
            .filter(place -> place.packetType == packetType)
            .filter(place -> place.chatType == null || Objects.equals(place.chatType, chatType))
            .findFirst()
            .orElse(null);
    }

    public static MessagePlace fromPacketType(
        final PacketType packetType,
        final EnumWrappers.ChatType chatTypeEnum
    ) {
        return Arrays.stream(values())
            .filter(place -> place.packetType == packetType)
            .filter(place -> place.chatTypeEnum == null || place.chatTypeEnum == chatTypeEnum)
            .findFirst()
            .orElse(null);
    }
}
