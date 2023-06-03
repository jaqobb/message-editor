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

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public final class InventoryItemsPacketListener extends PacketAdapter {

    public InventoryItemsPacketListener(MessageEditorPlugin plugin) {
        super(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.WINDOW_ITEMS);
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
        PacketContainer packet = event.getPacket().shallowClone();
        Iterable<ItemStack> items;
        if (packet.getItemArrayModifier().size() == 1) {
            items = Arrays.asList(packet.getItemArrayModifier().read(0));
        } else {
            items = packet.getItemListModifier().read(0);
        }
        boolean update = false;
        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }
            if (!item.hasItemMeta()) {
                continue;
            }
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta.hasDisplayName()) {
                String originalMessage = itemMeta.getDisplayName();
                String message = originalMessage;
                Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(message);
                MessageEdit messageEdit = null;
                Matcher messageEditMatcher = null;
                if (cachedMessage == null) {
                    for (MessageEdit edit : this.getPlugin().getMessageEdits()) {
                        MessagePlace place = edit.getMessageBeforePlace();
                        if (place != null && place != MessagePlace.INVENTORY_ITEM_NAME) {
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
                            newMessage = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, newMessage);
                        }
                        this.getPlugin().cacheMessage(message, messageEdit, newMessage);
                        message = newMessage;
                    }
                }
                boolean json = MessageUtils.isJson(message);
                if (json) {
                    json = false;
                    message = BaseComponent.toLegacyText(MessageUtils.toBaseComponents(message));
                }
                String id = MessageUtils.generateId(MessagePlace.INVENTORY_ITEM_NAME, message);
                this.getPlugin().cacheMessageData(id, new MessageData(id, MessagePlace.INVENTORY_ITEM_NAME, message, json));
                if (MessagePlace.INVENTORY_ITEM_NAME.isAnalyzing()) {
                    MessageUtils.logMessage(this.getPlugin().getLogger(), MessagePlace.INVENTORY_ITEM_NAME, player, id, json, message);
                }
                if (!message.equals(originalMessage)) {
                    update = true;
                    itemMeta.setDisplayName(message);
                    item.setItemMeta(itemMeta);
                }
            }
            if (itemMeta.hasLore()) {
                String originalMessage = String.join("\\n", itemMeta.getLore());
                String message = originalMessage;
                Map.Entry<MessageEdit, String> cachedMessage = this.getPlugin().getCachedMessage(message);
                MessageEdit messageEdit = null;
                Matcher messageEditMatcher = null;
                if (cachedMessage == null) {
                    for (MessageEdit edit : this.getPlugin().getMessageEdits()) {
                        MessagePlace place = edit.getMessageBeforePlace();
                        if (place != null && place != MessagePlace.INVENTORY_ITEM_LORE) {
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
                            newMessage = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, newMessage);
                        }
                        this.getPlugin().cacheMessage(message, messageEdit, newMessage);
                        message = newMessage;
                    }
                }
                boolean json = MessageUtils.isJson(message);
                if (json) {
                    json = false;
                    message = BaseComponent.toLegacyText(MessageUtils.toBaseComponents(message));
                }
                String id = MessageUtils.generateId(MessagePlace.INVENTORY_ITEM_LORE, message);
                this.getPlugin().cacheMessageData(id, new MessageData(id, MessagePlace.INVENTORY_ITEM_LORE, message, json));
                if (MessagePlace.INVENTORY_ITEM_LORE.isAnalyzing()) {
                    MessageUtils.logMessage(this.getPlugin().getLogger(), MessagePlace.INVENTORY_ITEM_LORE, player, id, json, message);
                }
                if (!message.equals(originalMessage)) {
                    update = true;
                    itemMeta.setLore(Arrays.asList(message.split("\\\\n")));
                    item.setItemMeta(itemMeta);
                }
            }
        }
        if (update) {
            // Updating items in the cloned packet and then setting the packet does seem to work but only partially
            // (initial items are still the old ones and updating the opened inventory actually fixes the issue).
            // However, during initial testing, not modifying the packet and just sending another one (with modified items) a tick later
            // seemed to work all the time. Although not sure, this may break (by replacing items in a wrong inventory) when
            // the opened inventory is changed multiple times in a rapid succession and each inventory needs to be updated.
            // If such issue happens, I believe it can be fixed by storing and then verifying window id that should have
            // its items updated.
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.getPlugin(), () -> {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                } catch (Exception exception) {
                    this.getPlugin().getLogger().log(Level.WARNING, "Could not send packet with updated items.", exception);
                }
            });
        }
    }
}
