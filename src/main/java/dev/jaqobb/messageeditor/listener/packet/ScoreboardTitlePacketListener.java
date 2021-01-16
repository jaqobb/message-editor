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
import org.bukkit.entity.Player;

public final class ScoreboardTitlePacketListener extends PacketAdapter {

    private static final MessagePlace MESSAGE_PLACE = MessagePlace.SCOREBOARD_TITLE;

    public ScoreboardTitlePacketListener(final MessageEditorPlugin plugin) {
        super(plugin, ListenerPriority.HIGHEST, MESSAGE_PLACE.getPacketType());
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
        PacketContainer packet = event.getPacket().shallowClone();
        // 0 = create scoreboard objective
        // 1 = delete scoreboard objective
        // 2 = update scoreboard objective display name
        int action = packet.getIntegers().read(0);
        if (action != 0 && action != 2) {
            return;
        }
        String originalMessage = MESSAGE_PLACE.getMessage(packet);
        String message = originalMessage;
        if (message == null) {
            return;
        }
        Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(message);
        MessageEdit messageEdit = null;
        Matcher messageEditMatcher = null;
        if (cachedMessage == null) {
            for (MessageEdit currentMessageEdit : this.getPlugin().getMessageEdits()) {
                if (currentMessageEdit.getMessageBeforePlace() != null && currentMessageEdit.getMessageBeforePlace() != MESSAGE_PLACE) {
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
                message = cachedMessage.getValue();
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
                message = messageAfter;
            }
        }
        boolean messageJson = MessageUtils.isJson(message);
        String messageId = MessageUtils.composeMessageId(MESSAGE_PLACE, message);
        this.getPlugin().cacheMessageData(messageId, new MessageData(MESSAGE_PLACE, message, messageJson));
        if (MESSAGE_PLACE.isAnalyzingActivated()) {
            MessageUtils.logMessage(
                this.getPlugin().getLogger(),
                MESSAGE_PLACE,
                player,
                messageId,
                messageJson,
                message
            );
        }
        if (!message.equals(originalMessage)) {
            MESSAGE_PLACE.setMessage(packet, message, messageJson);
            event.setPacket(packet);
        }
    }
}
