/*
 * MIT License
 *
 * Copyright (c) 2020-2022 Jakub Zagórski (jaqobb)
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

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import java.util.Map;
import java.util.regex.Matcher;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

class CommonPacketListener extends PacketAdapter {

    private final MessagePlace messagePlace;

    CommonPacketListener(MessageEditorPlugin plugin, MessagePlace messagePlace) {
        super(plugin, ListenerPriority.HIGHEST, messagePlace.getPacketType());
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
        Player          player = event.getPlayer();
        PacketContainer packet = event.getPacket().shallowClone();
        if (!this.shouldProcess(packet)) {
            return;
        }
        String originalMessage = this.messagePlace.getMessage(packet);
        String message         = originalMessage;
        if (message == null) {
            return;
        }
        Map.Entry<MessageEdit, String> cachedMessage      = this.getPlugin().getCachedMessage(message);
        MessageEdit                    messageEdit        = null;
        Matcher                        messageEditMatcher = null;
        if (cachedMessage == null) {
            for (MessageEdit currentMessageEdit : this.getPlugin().getMessageEdits()) {
                if (currentMessageEdit.getMessageBeforePlace() != null && currentMessageEdit.getMessageBeforePlace() != this.messagePlace) {
                    continue;
                }
                Matcher currentMessageEditMatcher = currentMessageEdit.getMatcher(message);
                if (currentMessageEditMatcher != null) {
                    messageEdit        = currentMessageEdit;
                    messageEditMatcher = currentMessageEditMatcher;
                    break;
                }
            }
        }
        if (cachedMessage != null || (messageEdit != null && messageEditMatcher != null)) {
            if (cachedMessage != null) {
                message = cachedMessage.getValue();
            } else {
                String messageAfter = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
                messageAfter = ChatColor.translateAlternateColorCodes('&', messageAfter);
                if (this.getPlugin().isPlaceholderApiPresent()) {
                    messageAfter = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, messageAfter);
                }
                this.getPlugin().cacheMessage(message, messageEdit, messageAfter);
                message = messageAfter;
            }
        }
        boolean messageJson = MessageUtils.isJson(message);
        String  messageId   = MessageUtils.composeMessageId(this.messagePlace, message);
        this.getPlugin().cacheMessageData(messageId, new MessageData(this.messagePlace, message, messageJson));
        if (this.messagePlace.isAnalyzingActivated()) {
            MessageUtils.logMessage(this.getPlugin().getLogger(), this.messagePlace, player, messageId, messageJson, message);
        }
        if (!message.equals(originalMessage)) {
            this.messagePlace.setMessage(packet, message, messageJson);
            event.setPacket(packet);
        }
    }
}
