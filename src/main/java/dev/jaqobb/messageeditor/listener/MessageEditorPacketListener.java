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
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.data.MessageData;
import dev.jaqobb.messageeditor.data.bossbar.BossBarMessageAction;
import dev.jaqobb.messageeditor.data.bossbar.BossBarMessageColor;
import dev.jaqobb.messageeditor.data.bossbar.BossBarMessageStyle;
import dev.jaqobb.messageeditor.data.MessageEdit;
import dev.jaqobb.messageeditor.data.MessagePlace;
import dev.jaqobb.messageeditor.data.scoreboard.ScoreboardHealthDisplayMode;
import dev.jaqobb.messageeditor.util.MessageUtils;
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

    private static final String SPECIAL_REGEX_CHARACTERS = "[<>{}()\\[\\].+\\-*?^$\\\\|]";

    @SuppressWarnings("deprecation")
    private static final HoverEvent COPY_TO_CLIPBOARD_HOVER_EVENT = new HoverEvent(
        HoverEvent.Action.SHOW_TEXT,
        TextComponent.fromLegacyText(ChatColor.GRAY + "Click to copy this message's JSON to your clipboard.")
    );

    public MessageEditorPacketListener(final MessageEditorPlugin plugin) {
        super(
            plugin,
            ListenerPriority.HIGHEST,
            PacketType.Login.Server.DISCONNECT,
            PacketType.Play.Server.KICK_DISCONNECT,
            PacketType.Play.Server.CHAT,
            PacketType.Play.Server.BOSS,
            PacketType.Play.Server.SCOREBOARD_OBJECTIVE
        );
    }

    @Override
    public MessageEditorPlugin getPlugin() {
        return (MessageEditorPlugin) super.getPlugin();
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
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
        } else if (messagePlace == MessagePlace.SCOREBOARD_TITLE) {
            // 0 = create scoreboard objective
            // 1 = delete scoreboard objective
            // 2 = update scoreboard objective display name
            int action = newPacket.getIntegers().read(0);
            if (action != 0 && action != 2) {
                return;
            }
        }
        String message = null;
        if (newPacket.getChatComponents().size() == 1 && newPacket.getChatComponents().read(0) != null) {
            message = newPacket.getChatComponents().read(0).getJson();
        } else if (newPacket.getSpecificModifier(BaseComponent[].class).size() == 1 && newPacket.getSpecificModifier(BaseComponent[].class).read(0) != null) {
            message = ComponentSerializer.toString(newPacket.getSpecificModifier(BaseComponent[].class).read(0));
        }
        if (message == null) {
            return;
        }
        Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(message);
        MessageEdit messageEdit = null;
        Matcher messageEditMatcher = null;
        if (cachedMessage == null) {
            for (MessageEdit currentMessageEdit : this.getPlugin().getMessageEdits()) {
                if (currentMessageEdit.getMessageBeforePlace() != null && currentMessageEdit.getMessageBeforePlace() != messagePlace) {
                    continue;
                }
                Matcher currentMessageEditMatcher = currentMessageEdit.getMatcher(message);
                if (currentMessageEditMatcher != null) {
                    messageEdit = currentMessageEdit;
                    messageEditMatcher = currentMessageEditMatcher;
                    break;
                }
            }
        }
        if (cachedMessage != null || (messageEdit != null && messageEditMatcher != null)) {
            if (cachedMessage != null) {
                if (messagePlace == MessagePlace.GAME_CHAT || messagePlace == MessagePlace.SYSTEM_CHAT || messagePlace == MessagePlace.ACTION_BAR) {
                    MessagePlace newMessagePlace = cachedMessage.getKey().getMessageAfterPlace();
                    if (newMessagePlace != messagePlace && (newMessagePlace == MessagePlace.GAME_CHAT || newMessagePlace == MessagePlace.SYSTEM_CHAT || newMessagePlace == MessagePlace.ACTION_BAR)) {
                        if (newPacket.getBytes().size() == 1) {
                            newPacket.getBytes().write(0, newMessagePlace.getChatType());
                        } else {
                            newPacket.getChatTypes().write(0, newMessagePlace.getChatTypeEnum());
                        }
                        messagePlace = newMessagePlace;
                    }
                }
                if (cachedMessage.getValue().isEmpty() && (messagePlace == MessagePlace.GAME_CHAT || messagePlace == MessagePlace.SYSTEM_CHAT || messagePlace == MessagePlace.ACTION_BAR)) {
                    event.setCancelled(true);
                    return;
                }
                message = cachedMessage.getValue();
            } else {
                String messageAfter = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
                if (this.getPlugin().isPlaceholderApiPresent()) {
                    messageAfter = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, messageAfter);
                }
                if (this.getPlugin().isMvdwPlaceholderApiPresent()) {
                    messageAfter = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, messageAfter);
                }
                this.getPlugin().cacheMessage(message, messageEdit, messageAfter);
                if (messagePlace == MessagePlace.GAME_CHAT || messagePlace == MessagePlace.SYSTEM_CHAT || messagePlace == MessagePlace.ACTION_BAR) {
                    MessagePlace newMessagePlace = messageEdit.getMessageAfterPlace();
                    if (newMessagePlace != messagePlace && (newMessagePlace == MessagePlace.GAME_CHAT || newMessagePlace == MessagePlace.SYSTEM_CHAT || newMessagePlace == MessagePlace.ACTION_BAR)) {
                        if (newPacket.getBytes().size() == 1) {
                            newPacket.getBytes().write(0, newMessagePlace.getChatType());
                        } else {
                            newPacket.getChatTypes().write(0, newMessagePlace.getChatTypeEnum());
                        }
                        messagePlace = newMessagePlace;
                    }
                }
                if (messageAfter.isEmpty() && (messagePlace == MessagePlace.GAME_CHAT || messagePlace == MessagePlace.SYSTEM_CHAT || messagePlace == MessagePlace.ACTION_BAR)) {
                    event.setCancelled(true);
                    return;
                }
                message = messageAfter;
            }
        }
        String messageId = MessageUtils.composeMessageId(messagePlace, message);
        MessageData messageData = new MessageData(messagePlace, message);
        this.getPlugin().cacheMessageData(messageId, messageData);
        if (messagePlace.isAnalyzingActivated()) {
            String messageReplaced = message.replaceAll(SPECIAL_REGEX_CHARACTERS, "\\\\$0");
            String messageClear = "";
            for (BaseComponent component : ComponentSerializer.parse(message)) {
                messageClear += component.toPlainText();
            }
            this.getPlugin().getLogger().log(Level.INFO, "Place: " + messagePlace.name());
            this.getPlugin().getLogger().log(Level.INFO, "Player: " + player.getName());
            this.getPlugin().getLogger().log(Level.INFO, "Message JSON: '" + messageReplaced + "'");
            this.getPlugin().getLogger().log(Level.INFO, "Message clear: '" + messageClear + "'");
            this.getPlugin().getLogger().log(Level.INFO, "Message ID: '" + messageId + "'");
        }
        if ((messagePlace == MessagePlace.GAME_CHAT || messagePlace == MessagePlace.SYSTEM_CHAT) && player.hasPermission("messageeditor.use") && this.getPlugin().isAttachingSpecialHoverAndClickEventsEnabled()) {
            TextComponent messageToSend = new TextComponent(ComponentSerializer.parse(message));
            messageToSend.setHoverEvent(COPY_TO_CLIPBOARD_HOVER_EVENT);
            messageToSend.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, message.replaceAll(SPECIAL_REGEX_CHARACTERS, "\\\\$0")));
            message = ComponentSerializer.toString(messageToSend);
        }
        if (newPacket.getChatComponents().size() == 1 && newPacket.getChatComponents().read(0) != null) {
            newPacket.getChatComponents().write(0, WrappedChatComponent.fromJson(message));
        } else if (newPacket.getSpecificModifier(BaseComponent[].class).size() == 1) {
            newPacket.getSpecificModifier(BaseComponent[].class).write(0, ComponentSerializer.parse(message));
        }
        event.setPacket(newPacket);
    }

    private PacketContainer copyPacketContent(
        final PacketContainer oldPacket,
        final PacketContainer newPacket
    ) {
        if (newPacket.getType() == PacketType.Login.Server.DISCONNECT) {
            newPacket.getChatComponents().write(0, oldPacket.getChatComponents().read(0));
        } else if (newPacket.getType() == PacketType.Play.Server.KICK_DISCONNECT) {
            newPacket.getChatComponents().write(0, oldPacket.getChatComponents().read(0));
        } else if (newPacket.getType() == PacketType.Play.Server.CHAT) {
            newPacket.getChatComponents().write(0, oldPacket.getChatComponents().read(0));
            if (newPacket.getSpecificModifier(BaseComponent[].class).size() == 1) {
                newPacket.getSpecificModifier(BaseComponent[].class).write(0, oldPacket.getSpecificModifier(BaseComponent[].class).read(0));
            }
            if (newPacket.getBytes().size() == 1) {
                newPacket.getBytes().write(0, oldPacket.getBytes().read(0));
            } else {
                newPacket.getChatTypes().write(0, oldPacket.getChatTypes().read(0));
            }
        } else if (newPacket.getType() == PacketType.Play.Server.BOSS) {
            newPacket.getUUIDs().write(0, oldPacket.getUUIDs().read(0));
            newPacket.getChatComponents().write(0, oldPacket.getChatComponents().read(0));
            newPacket.getEnumModifier(BossBarMessageAction.class, 1).write(0, oldPacket.getEnumModifier(BossBarMessageAction.class, 1).read(0));
            newPacket.getEnumModifier(BossBarMessageColor.class, 4).write(0, oldPacket.getEnumModifier(BossBarMessageColor.class, 4).read(0));
            newPacket.getEnumModifier(BossBarMessageStyle.class, 5).write(0, oldPacket.getEnumModifier(BossBarMessageStyle.class, 5).read(0));
            newPacket.getFloat().write(0, oldPacket.getFloat().read(0));
            newPacket.getBooleans().write(0, oldPacket.getBooleans().read(0));
            newPacket.getBooleans().write(1, oldPacket.getBooleans().read(1));
            newPacket.getBooleans().write(2, oldPacket.getBooleans().read(2));
        } else if (newPacket.getType() == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
            newPacket.getStrings().write(0, oldPacket.getStrings().read(0));
            newPacket.getChatComponents().write(0, oldPacket.getChatComponents().read(0));
            newPacket.getEnumModifier(ScoreboardHealthDisplayMode.class, 2).write(0, oldPacket.getEnumModifier(ScoreboardHealthDisplayMode.class, 2).read(0));
            newPacket.getIntegers().write(0, oldPacket.getIntegers().read(0));
        }
        return newPacket;
    }
}
