/*
 * MIT License
 *
 * Copyright (c) 2020-2023 Jakub Zagórski (jaqobb)
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

package dev.jaqobb.message_editor.listener.packet;

import java.util.Map;
import java.util.regex.Matcher;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

class CommonPacketListener extends PacketAdapter {

    private final MessagePlace messagePlace;

    CommonPacketListener(MessageEditorPlugin plugin, MessagePlace messagePlace) {
        super(plugin, ListenerPriority.HIGHEST, messagePlace.getPacketTypes());
        this.messagePlace = messagePlace;
    }

    @Override
    public MessageEditorPlugin getPlugin() {
        return (MessageEditorPlugin) this.plugin;
    }

    public boolean shouldProcess(PacketContainer packet) {
        return true;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PacketContainer packet = event.getPacket().shallowClone();
        if (!this.shouldProcess(packet)) {
            return;
        }
        Player player = event.getPlayer();
        String originalMessage = this.messagePlace.getMessage(packet);
        String message = originalMessage;
        if (message == null) {
            return;
        }
        Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(message);
        MessageEdit messageEdit = null;
        Matcher messageEditMatcher = null;
        if (cachedMessage == null) {
            for (MessageEdit edit : this.getPlugin().getMessageEdits()) {
                MessagePlace place = edit.getMessageBeforePlace();
                if (place != null && place != this.messagePlace) {
                    continue;
                }
                Matcher matcher = edit.getMatcher(message);
                if (matcher != null) {
                    messageEdit = edit;
                    messageEditMatcher = matcher;
                    break;
                }
            }
        }
        if (cachedMessage != null || (messageEdit != null && messageEditMatcher != null)) {
            if (cachedMessage != null) {
                message = cachedMessage.getValue();
            } else {
                String newMessage = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
                newMessage = ChatColor.translateAlternateColorCodes('&', newMessage);
                if (this.getPlugin().isPlaceholderApiPresent()) {
                    newMessage = PlaceholderAPI.setPlaceholders(player, newMessage);
                }
                this.getPlugin().cacheMessage(message, messageEdit, newMessage);
                message = newMessage;
            }
        }
        boolean json = MessageUtils.isJson(message);
        String id = MessageUtils.generateId(this.messagePlace);
        this.getPlugin().cacheMessageData(id, new MessageData(id, this.messagePlace, message, json));
        if (this.messagePlace.isAnalyzing()) {
            MessageUtils.logMessage(this.getPlugin().getLogger(), this.messagePlace, player, id, json, message);
        }
        if (!message.equals(originalMessage)) {
            this.messagePlace.setMessage(packet, message, json);
            event.setPacket(packet);
        }
    }
}
