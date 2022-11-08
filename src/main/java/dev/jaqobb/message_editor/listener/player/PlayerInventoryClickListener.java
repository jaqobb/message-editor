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

package dev.jaqobb.message_editor.listener.player;

import com.cryptomorin.xseries.XSound;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessageEditData;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public final class PlayerInventoryClickListener implements Listener {

    private final MessageEditorPlugin plugin;

    public PlayerInventoryClickListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        Player        player        = (Player) event.getWhoClicked();
        Inventory     inventory     = event.getInventory();
        InventoryView inventoryView = event.getView();
        if (!inventoryView.getTitle().equals(MessageUtils.composeMessage("&8Message Editor"))) {
            return;
        }
        event.setCancelled(true);
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
            return;
        }
        if (event.getAction() == InventoryAction.NOTHING) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 0 && slot >= inventory.getSize()) {
            return;
        }
        MessageEditData messageEditData = this.plugin.getCurrentMessageEditData(player.getUniqueId());
        if (messageEditData == null) {
            return;
        }
        if (slot == 11) {
            messageEditData.setCurrentMode(MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_KEY);
            messageEditData.setOldMessagePatternKey("");
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.composeMessageWithPrefix("&7Enter old message pattern key, that is what you want to replace, or enter '&edone&7' if you are done replacing everything you want."));
        } else if (slot == 15) {
            ClickType click = event.getClick();
            if (click.isLeftClick()) {
                messageEditData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE);
                messageEditData.setNewMessageCache("");
                player.closeInventory();
                player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.composeMessageWithPrefix("&7Enter new message. Enter '&edone&7' once you are done entering the new message."));
                MessagePlace newMessagePlace = messageEditData.getNewMessagePlace();
                if (newMessagePlace == MessagePlace.GAME_CHAT || newMessagePlace == MessagePlace.SYSTEM_CHAT || newMessagePlace == MessagePlace.ACTION_BAR) {
                    player.sendMessage(MessageUtils.composeMessageWithPrefix("&7You can also enter '&eremove&7' if you do not want the new message to be sent to the players (this will completely remove the message)."));
                }
            } else if (click.isRightClick()) {
                messageEditData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_KEY);
                messageEditData.setNewMessageKey("");
                player.closeInventory();
                player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.composeMessageWithPrefix("&7Enter new message key, that is what you want to replace, or enter '&edone&7' if you are done replacing everything you want."));
            }
        } else if (slot == 24) {
            MessagePlace oldMessagePlace = messageEditData.getOldMessagePlace();
            if (oldMessagePlace != MessagePlace.GAME_CHAT && oldMessagePlace != MessagePlace.SYSTEM_CHAT && oldMessagePlace != MessagePlace.ACTION_BAR) {
                player.playSound(player.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.composeMessageWithPrefix("&cYou cannot change new message place of this message."));
                return;
            }
            messageEditData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_PLACE);
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.composeMessageWithPrefix("&7Enter new message place."));
            player.sendMessage(MessageUtils.composeMessageWithPrefix("&7Available message places:"));
            for (MessagePlace availableMessagePlace : Arrays.asList(MessagePlace.GAME_CHAT, MessagePlace.SYSTEM_CHAT, MessagePlace.ACTION_BAR)) {
                player.sendMessage(MessageUtils.composeMessageWithPrefix("&7- &e" + availableMessagePlace.name() + " &7(&e" + availableMessagePlace.getFriendlyName() + "&7)"));
            }
        } else if (slot == 48) {
            String       oldMessagePatternString  = messageEditData.getOldMessagePattern();
            Pattern      oldMessagePattern        = Pattern.compile(oldMessagePatternString);
            Matcher      oldMessagePatternMatcher = oldMessagePattern.matcher(messageEditData.getOriginalOldMessage());
            MessagePlace oldMessagePlace          = messageEditData.getOldMessagePlace();
            String       newMessage               = messageEditData.getNewMessage();
            newMessage = newMessage.replace("\\", "\\\\");
            if (oldMessagePatternMatcher.matches()) {
                StringJoiner excludePattern = new StringJoiner("|", "(?!", ")");
                excludePattern.add("\\$0");
                for (int index = 0; index < oldMessagePatternMatcher.groupCount(); index++) {
                    excludePattern.add("\\$" + (index + 1));
                }
                String excludePatternString = excludePattern + "\\$[0-9]+";
                newMessage = newMessage.replaceAll(excludePatternString, "\\\\$0");
            } else {
                newMessage = newMessage.replace("$", "\\$");
            }
            MessagePlace newMessagePlace = messageEditData.getNewMessagePlace();
            this.plugin.addMessageEdit(new MessageEdit(oldMessagePatternString, oldMessagePlace, newMessage, newMessagePlace));
            this.plugin.clearCachedMessages();
            this.plugin.saveConfig();
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.composeMessageWithPrefix("&7Message edit has been saved and applied."));
        } else if (slot == 50) {
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
        }
    }
}
