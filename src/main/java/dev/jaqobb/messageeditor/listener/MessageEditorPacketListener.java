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

package dev.jaqobb.messageeditor.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.data.bossbar.BossBarMessageAction;
import dev.jaqobb.messageeditor.data.bossbar.BossBarMessageColor;
import dev.jaqobb.messageeditor.data.bossbar.BossBarMessageStyle;
import dev.jaqobb.messageeditor.data.edit.MessageEdit;
import dev.jaqobb.messageeditor.data.place.MessagePlace;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

public final class MessageEditorPacketListener extends PacketAdapter {

    private static final String SPECIAL_REGEX_CHARACTERS = "[{}()\\[\\].+*?^$\\\\|]";

    @SuppressWarnings("deprecation")
    private static final HoverEvent COPY_TO_CLIPBOARD_HOVER_EVENT = new HoverEvent(
        HoverEvent.Action.SHOW_TEXT,
        TextComponent.fromLegacyText(ChatColor.GRAY + "Click to copy this message's JSON to your clipboard.")
    );

    public MessageEditorPacketListener(MessageEditorPlugin plugin) {
        super(
            plugin,
            ListenerPriority.HIGHEST,
            PacketType.Login.Server.DISCONNECT,
            PacketType.Play.Server.KICK_DISCONNECT,
            PacketType.Play.Server.CHAT,
            PacketType.Play.Server.BOSS
        );
    }

    @Override
    public MessageEditorPlugin getPlugin() {
        return (MessageEditorPlugin) super.getPlugin();
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        PacketContainer oldPacket = event.getPacket();
        PacketContainer newPacket = this.copyPacketContent(oldPacket, ProtocolLibrary.getProtocolManager().createPacket(oldPacket.getType()));
        MessagePlace messagePlace = MessagePlace.fromPacket(newPacket);
        if (messagePlace == MessagePlace.BOSS_BAR) {
            BossBarMessageAction action = newPacket.getEnumModifier(BossBarMessageAction.class, 1).read(0);
            if (action != BossBarMessageAction.ADD && action != BossBarMessageAction.UPDATE_NAME) {
                return;
            }
        }
        WrappedChatComponent message = newPacket.getChatComponents().read(0);
        String messageJson = null;
        if (message != null) {
            messageJson = message.getJson();
        } else if ((messagePlace == MessagePlace.CHAT || messagePlace == MessagePlace.ACTION_BAR) && newPacket.getSpecificModifier(BaseComponent[].class).size() == 1) {
            messageJson = ComponentSerializer.toString(newPacket.getSpecificModifier(BaseComponent[].class).read(0));
        }
        if (messageJson == null) {
            return;
        }
        Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(messageJson, messagePlace);
        if (cachedMessage != null && cachedMessage.getKey().getMessageBeforePlace() != null && cachedMessage.getKey().getMessageBeforePlace() != messagePlace) {
            return;
        }
        MessageEdit messageEdit = null;
        Matcher messageEditMatcher = null;
        if (cachedMessage == null) {
            for (MessageEdit currentMessageEdit : this.getPlugin().getMessageEdits()) {
                if (currentMessageEdit.getMessageBeforePlace() != null && currentMessageEdit.getMessageBeforePlace() != messagePlace) {
                    continue;
                }
                Matcher currentMessageEditMatcher = currentMessageEdit.getMatcher(messageJson);
                if (currentMessageEditMatcher != null) {
                    messageEdit = currentMessageEdit;
                    messageEditMatcher = currentMessageEditMatcher;
                    break;
                }
            }
        }
        if (cachedMessage != null || (messageEdit != null && messageEditMatcher != null)) {
            if (cachedMessage != null) {
                if (cachedMessage.getValue().isEmpty() && (messagePlace == MessagePlace.CHAT || messagePlace == MessagePlace.ACTION_BAR)) {
                    event.setCancelled(true);
                    return;
                }
                messageJson = cachedMessage.getValue();
            } else {
                String messageAfter = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
                if (this.getPlugin().isPlaceholderApiPresent()) {
                    messageAfter = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, messageAfter);
                }
                if (this.getPlugin().isMvdwPlaceholderApiPresent()) {
                    messageAfter = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, messageAfter);
                }
                this.getPlugin().cacheMessage(messageJson, messagePlace, messageEdit, messageAfter);
                if (messageAfter.isEmpty() && (messagePlace == MessagePlace.CHAT || messagePlace == MessagePlace.ACTION_BAR)) {
                    event.setCancelled(true);
                    return;
                }
                messageJson = messageAfter;
            }
        }
        if (messagePlace.isAnalyzingActivated()) {
            String messageClear = "";
            for (BaseComponent component : ComponentSerializer.parse(messageJson)) {
                messageClear += component.toPlainText();
            }
            this.getPlugin().getLogger().log(Level.INFO, "Place: " + messagePlace.name() + ".");
            this.getPlugin().getLogger().log(Level.INFO, "Receiver: " + player.getName() + ".");
            this.getPlugin().getLogger().log(Level.INFO, "Message clear: " + messageClear);
            this.getPlugin().getLogger().log(Level.INFO, "Message JSON: " + messageJson.replaceAll(SPECIAL_REGEX_CHARACTERS, "\\\\$0"));
        }
        if (messagePlace == MessagePlace.CHAT && player.hasPermission("messageeditor.use") && this.getPlugin().isAttachingSpecialHoverAndClickEventsEnabled()) {
            TextComponent messageToSend = new TextComponent(ComponentSerializer.parse(message.getJson()));
            messageToSend.setHoverEvent(COPY_TO_CLIPBOARD_HOVER_EVENT);
            messageToSend.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, message.getJson().replaceAll(SPECIAL_REGEX_CHARACTERS, "\\\\$0")));
            messageJson = ComponentSerializer.toString(messageToSend);
        }
        if (message != null) {
            newPacket.getChatComponents().write(0, WrappedChatComponent.fromJson(messageJson));
        } else if ((messagePlace == MessagePlace.CHAT || messagePlace == MessagePlace.ACTION_BAR) && newPacket.getSpecificModifier(BaseComponent[].class).size() == 1) {
            newPacket.getSpecificModifier(BaseComponent[].class).write(0, ComponentSerializer.parse(messageJson));
        }
        event.setPacket(newPacket);
    }

    private Byte getMessagePosition(PacketContainer packet) {
        if (packet.getType() != PacketType.Play.Server.CHAT) {
            return null;
        }
        // 0 and 1 - chat
        // 2 - action bar
        Byte position = packet.getBytes().readSafely(0);
        if (position == null) {
            position = packet.getChatTypes().read(0).getId();
        }
        return position;
    }

    private PacketContainer copyPacketContent(PacketContainer oldPacket, PacketContainer newPacket) {
        newPacket.getChatComponents().write(0, oldPacket.getChatComponents().read(0));
        if (newPacket.getType() == PacketType.Play.Server.CHAT) {
            BaseComponent[] components = oldPacket.getSpecificModifier(BaseComponent[].class).readSafely(0);
            if (components != null) {
                newPacket.getSpecificModifier(BaseComponent[].class).writeSafely(0, components);
            }
            Byte position = this.getMessagePosition(oldPacket);
            newPacket.getBytes().writeSafely(0, position);
            if (EnumWrappers.getChatTypeClass() != null) {
                Arrays.stream(EnumWrappers.ChatType.values())
                    .filter(type -> type.getId() == position)
                    .findAny()
                    .ifPresent(type -> newPacket.getChatTypes().write(0, type));
            }
        } else if (newPacket.getType() == PacketType.Play.Server.BOSS) {
            newPacket.getEnumModifier(BossBarMessageAction.class, 1).write(0, oldPacket.getEnumModifier(BossBarMessageAction.class, 1).read(0));
            newPacket.getEnumModifier(BossBarMessageColor.class, 4).write(0, oldPacket.getEnumModifier(BossBarMessageColor.class, 4).read(0));
            newPacket.getEnumModifier(BossBarMessageStyle.class, 5).write(0, oldPacket.getEnumModifier(BossBarMessageStyle.class, 5).read(0));
            newPacket.getUUIDs().write(0, oldPacket.getUUIDs().read(0));
            // Health
            newPacket.getFloat().write(0, oldPacket.getFloat().read(0));
            // Darken sky
            newPacket.getBooleans().write(0, oldPacket.getBooleans().read(0));
            // Play music
            newPacket.getBooleans().write(1, oldPacket.getBooleans().read(1));
            // Create fog
            newPacket.getBooleans().write(2, oldPacket.getBooleans().read(2));
        }
        return newPacket;
    }
}
