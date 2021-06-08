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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.message.MessageData;
import dev.jaqobb.messageeditor.message.MessageEdit;
import dev.jaqobb.messageeditor.message.MessagePlace;
import dev.jaqobb.messageeditor.util.MessageUtils;
import java.util.Map;
import java.util.regex.Matcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

public final class ChatPacketListener extends PacketAdapter {

    public ChatPacketListener(MessageEditorPlugin plugin) {
        super(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.CHAT);
    }

    @Override
    public MessageEditorPlugin getPlugin() {
        return (MessageEditorPlugin) super.getPlugin();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player          player               = event.getPlayer();
        PacketContainer packet               = event.getPacket().shallowClone();
        MessagePlace    originalMessagePlace = MessagePlace.fromPacket(packet);
        MessagePlace    messagePlace         = originalMessagePlace;
        String          originalMessage      = messagePlace.getMessage(packet);
        String          message              = originalMessage;
        if (message == null) {
            return;
        }
        Map.Entry<MessageEdit, String> cachedMessage      = this.getPlugin().getCachedMessage(message);
        MessageEdit                    messageEdit        = null;
        Matcher                        messageEditMatcher = null;
        if (cachedMessage == null) {
            for (MessageEdit currentMessageEdit : this.getPlugin().getMessageEdits()) {
                if (currentMessageEdit.getMessageBeforePlace() != null && currentMessageEdit.getMessageBeforePlace() != messagePlace) {
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
                String cachedMessageValue = cachedMessage.getValue();
                if (cachedMessageValue.isEmpty()) {
                    event.setCancelled(true);
                    return;
                }
                MessagePlace messageAfterPlace = cachedMessage.getKey().getMessageAfterPlace();
                if (messageAfterPlace == MessagePlace.GAME_CHAT || messageAfterPlace == MessagePlace.SYSTEM_CHAT || messageAfterPlace == MessagePlace.ACTION_BAR) {
                    messagePlace = messageAfterPlace;
                }
                message = cachedMessageValue;
            } else {
                String messageAfter = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
                messageAfter = ChatColor.translateAlternateColorCodes('&', messageAfter);
                if (this.getPlugin().isPlaceholderApiPresent()) {
                    messageAfter = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, messageAfter);
                }
                if (this.getPlugin().isMvdwPlaceholderApiPresent()) {
                    messageAfter = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, messageAfter);
                }
                this.getPlugin().cacheMessage(message, messageEdit, messageAfter);
                if (messageAfter.isEmpty()) {
                    event.setCancelled(true);
                    return;
                }
                MessagePlace messageAfterPlace = messageEdit.getMessageAfterPlace();
                if (messageAfterPlace == MessagePlace.GAME_CHAT || messageAfterPlace == MessagePlace.SYSTEM_CHAT || messageAfterPlace == MessagePlace.ACTION_BAR) {
                    messagePlace = messageAfterPlace;
                }
                message = messageAfter;
            }
        }
        boolean messageJson = MessageUtils.isJson(message);
        String  messageId   = MessageUtils.composeMessageId(messagePlace, message);
        this.getPlugin().cacheMessageData(messageId, new MessageData(messagePlace, message, messageJson));
        if (messagePlace.isAnalyzingActivated()) {
            MessageUtils.logMessage(this.getPlugin().getLogger(), messagePlace, player, messageId, messageJson, message);
        }
        if (this.getPlugin().isAttachingSpecialHoverAndClickEventsEnabled() && player.hasPermission("messageeditor.use")) {
            BaseComponent[] messageToSend;
            if (messageJson) {
                messageToSend = ComponentSerializer.parse(message);
            } else {
                messageToSend = MessageUtils.toBaseComponents(message);
            }
            for (BaseComponent messageToSendElement : messageToSend) {
                messageToSendElement.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(MessageUtils.composeMessage("&7Click to start editing this message.")))
                );
                messageToSendElement.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/message-editor edit " + messageId));
            }
            message     = MessageUtils.toJson(messageToSend, false);
            messageJson = true;
        }
        if (messagePlace != originalMessagePlace) {
            if (packet.getBytes().size() == 1) {
                packet.getBytes().write(0, messagePlace.getChatType());
            } else {
                packet.getChatTypes().write(0, messagePlace.getChatTypeEnum());
            }
        }
        if (!message.equals(originalMessage)) {
            messagePlace.setMessage(packet, message, messageJson);
        }
        if (!message.equals(originalMessage) || messagePlace != originalMessagePlace) {
            event.setPacket(packet);
        }
    }
}
