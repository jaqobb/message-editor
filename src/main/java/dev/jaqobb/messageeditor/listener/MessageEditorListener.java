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

import com.cryptomorin.xseries.XSound;
import dev.jaqobb.messageeditor.MessageEditorConstants;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.data.MessageEdit;
import dev.jaqobb.messageeditor.data.MessageEditData;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

public final class MessageEditorListener implements Listener {

    private final MessageEditorPlugin plugin;

    public MessageEditorListener(final MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("messageeditor.use") && this.plugin.isUpdateNotify()) {
            player.sendMessage(this.plugin.getUpdater().getUpdateMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.plugin.removeCurrentMessageEditData(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(final PlayerQuitEvent event) {
        this.plugin.removeCurrentMessageEditData(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInventoryClose(final InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        MessageEditData messageEditData = this.plugin.getCurrentMessageEditData(player.getUniqueId());
        if (messageEditData != null && messageEditData.shouldDestroy()) {
            this.plugin.removeCurrentMessageEditData(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInventoryCLick(final InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        InventoryView inventoryView = event.getView();
        if (!inventoryView.getTitle().equals(ChatColor.DARK_GRAY + "Message Editor")) {
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
        if (slot == 48) {
            this.plugin.addMessageEdit(new MessageEdit(
                messageEditData.isOldMessageJson() ? messageEditData.getOldMessage().replaceAll(MessageEditorConstants.SPECIAL_REGEX_CHARACTERS, "\\\\$0") : messageEditData.getOldMessage(),
                messageEditData.getOldMessagePlace(),
                messageEditData.getNewMessage(),
                messageEditData.getNewMessagePlace()
            ));
            this.plugin.clearCachedMessages();
            this.plugin.saveConfig();
            messageEditData.setShouldDestroy(true);
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Message edit has been saved and applied.");
        } else if (slot == 50) {
            messageEditData.setShouldDestroy(true);
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        MessageEditData messageEditData = this.plugin.getCurrentMessageEditData(player.getUniqueId());
        if (messageEditData == null) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(final PluginEnableEvent event) {
        Plugin plugin = event.getPlugin();
        if (plugin.getName().equals(MessageEditorConstants.PLACEHOLDER_API_PLUGIN_NAME)) {
            this.plugin.setPlaceholderApiPresent(true);
            this.plugin.getLogger().log(Level.INFO, MessageEditorConstants.PLACEHOLDER_API_PLUGIN_NAME + " integration has been enabled.");
        } else if (plugin.getName().equals(MessageEditorConstants.MVDW_PLACEHOLDER_API_PLUGIN_NAME)) {
            this.plugin.setMvdwPlaceholderApiPresent(true);
            this.plugin.getLogger().log(Level.INFO, MessageEditorConstants.MVDW_PLACEHOLDER_API_PLUGIN_NAME + " integration has been enabled.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(final PluginDisableEvent event) {
        Plugin plugin = event.getPlugin();
        if (plugin.getName().equals(MessageEditorConstants.PLACEHOLDER_API_PLUGIN_NAME)) {
            this.plugin.setPlaceholderApiPresent(false);
            this.plugin.getLogger().log(Level.INFO, MessageEditorConstants.PLACEHOLDER_API_PLUGIN_NAME + " integration has been disabled.");
        } else if (plugin.getName().equals(MessageEditorConstants.MVDW_PLACEHOLDER_API_PLUGIN_NAME)) {
            this.plugin.setMvdwPlaceholderApiPresent(false);
            this.plugin.getLogger().log(Level.INFO, MessageEditorConstants.MVDW_PLACEHOLDER_API_PLUGIN_NAME + " integration has been disabled.");
        }
    }
}
