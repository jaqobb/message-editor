/*
 * MIT License
 *
 * Copyright (c) 2020-2021 Jakub Zagórski (jaqobb)
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

package dev.jaqobb.messageeditor.listener.packet;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.message.MessagePlace;
import dev.jaqobb.messageeditor.message.bossbar.BossBarAction;

public final class BossBarPacketListener extends CommonPacketListener {

    public BossBarPacketListener(MessageEditorPlugin plugin) {
        super(plugin, MessagePlace.BOSS_BAR);
    }

    @Override
    public boolean shouldProcess(PacketContainer packet) {
        if (!MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
            BossBarAction action = packet.getEnumModifier(BossBarAction.class, 1).read(0);
            return action == BossBarAction.ADD || action == BossBarAction.UPDATE_NAME;
        }
        return packet.getStructures().read(1).getChatComponents().readSafely(0) != null;
    }
}
