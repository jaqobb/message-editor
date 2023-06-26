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

package dev.jaqobb.message_editor.listener.player;

import com.comphenix.protocol.utility.MinecraftVersion;
import com.cryptomorin.xseries.XSound;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessageEditData;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import org.bukkit.configuration.file.YamlConfiguration;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlayerInventoryClickListener implements Listener {

    private final MessageEditorPlugin plugin;

    public PlayerInventoryClickListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        InventoryView view = event.getView();
        if (!view.getTitle().equals(MessageUtils.translate("&8Message Editor"))) {
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
        MessageEditData editData = this.plugin.getCurrentMessageEditData(player.getUniqueId());
        if (editData == null) {
            return;
        }
        if (slot == 4) {
            editData.setCurrentMode(MessageEditData.Mode.EDITING_FILE_NAME);
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.translateWithPrefix("&7Enter new file name where you want your message edit to be stored, or enter '&edone&7' if you changed your mind and no longer want to edit file name."));
        } else if (slot == 20) {
            editData.setCurrentMode(MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_KEY);
            editData.setOldMessagePatternKey("");
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.translateWithPrefix("&7Enter old message pattern key, that is what you want to replace, or enter '&edone&7' if you are done replacing everything you want."));
        } else if (slot == 24) {
            ClickType click = event.getClick();
            if (click.isLeftClick()) {
                editData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE);
                editData.setNewMessageCache("");
                player.closeInventory();
                player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.translateWithPrefix("&7Enter new message. Enter '&edone&7' once you are done entering the new message."));
                MessagePlace place = editData.getNewMessagePlace();
                if (place == MessagePlace.GAME_CHAT || place == MessagePlace.SYSTEM_CHAT || place == MessagePlace.ACTION_BAR) {
                    player.sendMessage(MessageUtils.translateWithPrefix("&7You can also enter '&eremove&7' if you do not want the new message to be sent to the players (this will completely remove the message)."));
                }
            } else if (click.isRightClick()) {
                editData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_KEY);
                editData.setNewMessageKey("");
                player.closeInventory();
                player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.translateWithPrefix("&7Enter new message key, that is what you want to replace, or enter '&edone&7' if you are done replacing everything you want."));
            }
        } else if (slot == 33) {
            MessagePlace place = editData.getOldMessagePlace();
            if (place != MessagePlace.GAME_CHAT && place != MessagePlace.SYSTEM_CHAT && place != MessagePlace.ACTION_BAR) {
                player.playSound(player.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.translateWithPrefix("&cYou cannot change new message place of this message."));
                return;
            }
            editData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_PLACE);
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.translateWithPrefix("&7Enter new message place, or enter '&edone&7' if you changed your mind and no longer want to edit message place."));
            player.sendMessage(MessageUtils.translateWithPrefix("&7Available message places:"));
            Collection<MessagePlace> availableMessagePlaces = new ArrayList<>(3);
            if (!MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                availableMessagePlaces.add(MessagePlace.GAME_CHAT);
            }
            availableMessagePlaces.add(MessagePlace.SYSTEM_CHAT);
            availableMessagePlaces.add(MessagePlace.ACTION_BAR);
            for (MessagePlace availableMessagePlace : availableMessagePlaces) {
                player.sendMessage(MessageUtils.translate(" &8- &e" + availableMessagePlace.name() + " &7(&e" + availableMessagePlace.getFriendlyName() + "&7)"));
            }
        } else if (slot == 48) {
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
        } else if (slot == 50) {
            File file = new File(this.plugin.getDataFolder(), "edits" + File.separator + editData.getFileName() + ".yml");
            if (file.exists()) {
                player.playSound(player.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.translateWithPrefix("&cThere is already a message edit that uses a file with the name '&7" + editData.getFileName() + ".yml&c'."));
                return;
            }
            String oldMessagePatternString = editData.getOldMessagePattern();
            Pattern oldMessagePattern = Pattern.compile(oldMessagePatternString);
            Matcher oldMessagePatternMatcher = oldMessagePattern.matcher(editData.getOriginalOldMessage());
            MessagePlace oldMessagePlace = editData.getOldMessagePlace();
            String newMessage = editData.getNewMessage();
            newMessage = newMessage.replace("\\", "\\\\");
            if (oldMessagePatternMatcher.matches()) {
                StringJoiner excludePattern = new StringJoiner("|", "(?!", ")");
                excludePattern.add("\\$0");
                for (int groupId = 0; groupId < oldMessagePatternMatcher.groupCount(); groupId += 1) {
                    excludePattern.add("\\$" + (groupId + 1));
                }
                String excludePatternString = excludePattern + "\\$[0-9]+";
                newMessage = newMessage.replaceAll(excludePatternString, "\\\\$0");
            } else {
                newMessage = newMessage.replace("$", "\\$");
            }
            MessagePlace newMessagePlace = editData.getNewMessagePlace();
            MessageEdit edit = new MessageEdit(oldMessagePatternString, oldMessagePlace, newMessage, newMessagePlace);
            this.plugin.addMessageEdit(edit);
            this.plugin.clearCachedMessages();
            try {
                if (file.createNewFile()) {
                    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                    for (Map.Entry<String, Object> entry : edit.serialize().entrySet()) {
                        configuration.set(entry.getKey(), entry.getValue());
                    }
                    configuration.save(file);
                } else {
                    player.sendMessage(MessageUtils.translateWithPrefix("&cCould not create message edit file."));
                }
            } catch (IOException exception) {
                this.plugin.getLogger().log(Level.WARNING, "Could not save message edit.", exception);
                player.sendMessage(MessageUtils.translateWithPrefix("&cCould not save message edit, check console for more information."));
            }
            player.closeInventory();
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.translateWithPrefix("&7Message edit has been saved and applied."));
        }
    }
}
