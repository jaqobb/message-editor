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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftVersion;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.regex.Matcher;

public final class ChatPacketListener extends PacketAdapter {

    public ChatPacketListener(MessageEditorPlugin plugin) {
        super(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.CHAT, PacketType.Play.Server.SYSTEM_CHAT);
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
        PacketContainer packet = event.getPacket().shallowClone();
        if (packet.getType() == PacketType.Play.Server.CHAT && MinecraftVersion.WILD_UPDATE.atOrAbove()) {
            return;
        }
        Player player = event.getPlayer();
        MessagePlace originalPlace = MessagePlace.fromPacket(packet);
        MessagePlace place = originalPlace;
        String originalMessage = place.getMessage(packet);
        String message = originalMessage;
        if (message == null) {
            return;
        }
        Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(message);
        MessageEdit messageEdit = null;
        Matcher messageEditMatcher = null;
        if (cachedMessage == null) {
            for (MessageEdit edit : this.getPlugin().getMessageEdits()) {
                MessagePlace beforePlace = edit.getMessageBeforePlace();
                if (beforePlace == MessagePlace.GAME_CHAT && MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                    beforePlace = MessagePlace.SYSTEM_CHAT;
                }
                if (beforePlace != null && beforePlace != place) {
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
                String value = cachedMessage.getValue();
                if (value.isEmpty()) {
                    event.setCancelled(true);
                    return;
                }
                MessagePlace newPlace = cachedMessage.getKey().getMessageAfterPlace();
                if (newPlace == MessagePlace.GAME_CHAT || newPlace == MessagePlace.SYSTEM_CHAT || newPlace == MessagePlace.ACTION_BAR) {
                    place = newPlace;
                    if (place == MessagePlace.GAME_CHAT && packet.getType() == PacketType.Play.Server.SYSTEM_CHAT) {
                        place = MessagePlace.SYSTEM_CHAT;
                    }
                }
                message = value;
            } else {
                String newMessage = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
                newMessage = ChatColor.translateAlternateColorCodes('&', newMessage);
                if (this.getPlugin().isPlaceholderApiPresent()) {
                    newMessage = PlaceholderAPI.setPlaceholders(player, newMessage);
                }
                this.getPlugin().cacheMessage(message, messageEdit, newMessage);
                if (newMessage.isEmpty()) {
                    event.setCancelled(true);
                    return;
                }
                MessagePlace afterPlace = messageEdit.getMessageAfterPlace();
                if (afterPlace == MessagePlace.GAME_CHAT || afterPlace == MessagePlace.SYSTEM_CHAT || afterPlace == MessagePlace.ACTION_BAR) {
                    place = afterPlace;
                    if (place == MessagePlace.GAME_CHAT && packet.getType() == PacketType.Play.Server.SYSTEM_CHAT) {
                        place = MessagePlace.SYSTEM_CHAT;
                    }
                }
                message = newMessage;
            }
        }
        boolean json = MessageUtils.isJson(message);
        String id = MessageUtils.generateId(place);
        this.getPlugin().cacheMessageData(id, new MessageData(id, place, message, json));
        if (place.isAnalyzing()) {
            MessageUtils.logMessage(this.getPlugin().getLogger(), place, player, id, json, message);
        }
        if (this.getPlugin().isAttachSpecialHoverAndClickEvents() && player.hasPermission("messageeditor.use")) {
            BaseComponent[] messageComponents;
            if (json) {
                messageComponents = ComponentSerializer.parse(message);
            } else {
                messageComponents = MessageUtils.toBaseComponents(message);
            }
            for (BaseComponent messageToSendElement : messageComponents) {
                messageToSendElement.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(MessageUtils.translate("&7Click to start editing this message.")))
                );
                messageToSendElement.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/message-editor edit " + id));
            }
            message = MessageUtils.toJson(messageComponents, false);
            json = true;
        }
        if (place != originalPlace) {
            if (packet.getType() == PacketType.Play.Server.CHAT) {
                if (packet.getBytes().size() == 1) {
                    packet.getBytes().write(0, place.getChatType());
                } else {
                    packet.getChatTypes().write(0, place.getChatTypeEnum());
                }
            } else {
                packet.getBooleans().write(0, place == MessagePlace.ACTION_BAR);
            }
        }
        if (!message.equals(originalMessage)) {
            place.setMessage(packet, message, json);
        }
        if (!message.equals(originalMessage) || place != originalPlace) {
            event.setPacket(packet);
        }
    }
}
