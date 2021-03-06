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

package dev.jaqobb.messageeditor.menu;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.jaqobb.messageeditor.MessageEditorConstants;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.message.MessageData;
import dev.jaqobb.messageeditor.message.MessageEditData;
import dev.jaqobb.messageeditor.util.MessageUtils;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class MenuManager {

    private static final int[] BLACK_STAINED_GLASS_PANE_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 49, 51, 52, 53};
    private static final int[] GRAY_STAINED_GLASS_PANE_SLOTS  = {10, 12, 13, 14, 16, 19, 21, 23, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    private final MessageEditorPlugin plugin;
    private final ItemStack           blackStainedGlassPaneItem;
    private final ItemStack           grayStainedGlassPaneItem;
    private final ItemStack           arrowItem;

    public MenuManager(MessageEditorPlugin plugin) {
        this.plugin = plugin;
        ItemStack blackStainedGlassPaneItem     = XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
        ItemMeta  blackStainedGlassPaneItemMeta = blackStainedGlassPaneItem.getItemMeta();
        blackStainedGlassPaneItemMeta.setDisplayName(" ");
        blackStainedGlassPaneItem.setItemMeta(blackStainedGlassPaneItemMeta);
        this.blackStainedGlassPaneItem = blackStainedGlassPaneItem;
        ItemStack grayStainedGlassPaneItem     = XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
        ItemMeta  grayStainedGlassPaneItemMeta = grayStainedGlassPaneItem.getItemMeta();
        grayStainedGlassPaneItemMeta.setDisplayName(" ");
        grayStainedGlassPaneItem.setItemMeta(grayStainedGlassPaneItemMeta);
        this.grayStainedGlassPaneItem = grayStainedGlassPaneItem;
        ItemStack arrowItem     = XMaterial.PLAYER_HEAD.parseItem();
        ItemMeta  arrowItemMeta = arrowItem.getItemMeta();
        try {
            String      textures = "ewogICJ0aW1lc3RhbXAiIDogMTYwNzM2ODc1MzI0NCwKICAicHJvZmlsZUlkIiA6ICI1MGM4NTEwYjVlYTA0ZDYwYmU5YTdkNTQyZDZjZDE1NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dSaWdodCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMzRlZjA2Mzg1MzcyMjJiMjBmNDgwNjk0ZGFkYzBmODVmYmUwNzU5ZDU4MWFhN2ZjZGYyZTQzMTM5Mzc3MTU4IgogICAgfQogIH0KfQ==";
            GameProfile profile  = new GameProfile(UUID.nameUUIDFromBytes(textures.getBytes(StandardCharsets.UTF_8)), "MHF_ArrowRight");
            profile.getProperties().put("textures", new Property("textures", textures));
            Field profileField = arrowItemMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(arrowItemMeta, profile);
        } catch (Exception exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not load skull texture for the arrow item.", exception);
        }
        arrowItemMeta.setDisplayName(MessageUtils.composeMessage("&e< &7Old message"));
        arrowItemMeta.setLore(Arrays.asList(MessageUtils.composeMessage("&e> &7New message")));
        arrowItem.setItemMeta(arrowItemMeta);
        this.arrowItem = arrowItem;
    }

    public void openMenu(Player player, MessageData messageData, boolean playSound) {
        MessageEditData messageEditData = new MessageEditData(messageData);
        this.openMenu(player, messageEditData, playSound);
        this.plugin.setCurrentMessageEdit(player.getUniqueId(), messageEditData);
    }

    public void openMenu(Player player, MessageEditData messageEditData, boolean playSound) {
        Inventory inventory = Bukkit.createInventory(null, 54, MessageUtils.composeMessage("&8Message Editor"));
        for (int blackStainedGlassPaneSlot : BLACK_STAINED_GLASS_PANE_SLOTS) {
            inventory.setItem(blackStainedGlassPaneSlot, this.blackStainedGlassPaneItem);
        }
        for (int grayStainedGlassPaneSlot : GRAY_STAINED_GLASS_PANE_SLOTS) {
            inventory.setItem(grayStainedGlassPaneSlot, this.grayStainedGlassPaneItem);
        }
        ItemStack oldMessageItem     = XMaterial.PAPER.parseItem();
        ItemMeta  oldMessageItemMeta = oldMessageItem.getItemMeta();
        oldMessageItemMeta.setDisplayName(MessageUtils.composeMessage("&fOld message"));
        String oldMessage;
        if (messageEditData.isOldMessageJson()) {
            oldMessage = BaseComponent.toLegacyText(ComponentSerializer.parse(messageEditData.getOldMessage()));
        } else {
            oldMessage = messageEditData.getOldMessage();
        }
        List<String> oldMessageLore = new ArrayList<>(10);
        oldMessageLore.add("");
        for (String oldMessageData : oldMessage.split(messageEditData.isOldMessageJson() ? "\\n" : "\\\\n")) {
            String   oldMessageChunk     = "";
            String[] oldMessageDataChunk = oldMessageData.split(" ");
            for (int oldMessageDataIndex = 0; oldMessageDataIndex < oldMessageDataChunk.length; oldMessageDataIndex++) {
                if (oldMessageDataIndex > 0 && oldMessageDataIndex < oldMessageDataChunk.length && !oldMessageChunk.isEmpty()) {
                    oldMessageChunk += " ";
                }
                oldMessageChunk += oldMessageDataChunk[oldMessageDataIndex];
                if (oldMessageDataIndex == oldMessageDataChunk.length - 1 || oldMessageChunk.length() >= MessageEditorConstants.MESSAGE_LENGTH) {
                    if (oldMessageLore.size() == 1) {
                        oldMessageLore.add(oldMessageChunk);
                    } else {
                        oldMessageLore.add(MessageUtils.getLastColors(oldMessageLore.get(oldMessageLore.size() - 1)) + oldMessageChunk);
                    }
                    oldMessageChunk = "";
                }
            }
        }
        oldMessageLore.add("");
        oldMessageLore.add(MessageUtils.composeMessage("&7Click to edit old message pattern."));
        oldMessageItemMeta.setLore(oldMessageLore);
        oldMessageItem.setItemMeta(oldMessageItemMeta);
        inventory.setItem(11, oldMessageItem);
        ItemStack oldMessagePlaceItem     = XMaterial.COMPASS.parseItem();
        ItemMeta  oldMessagePlaceItemMeta = oldMessagePlaceItem.getItemMeta();
        oldMessagePlaceItemMeta.setDisplayName(MessageUtils.composeMessage("&fOld message place"));
        oldMessagePlaceItemMeta.setLore(Arrays.asList(
            "",
            MessageUtils.composeMessage("&7ID: &e" + messageEditData.getOldMessagePlace().name()),
            MessageUtils.composeMessage("&7Friendly name: &e" + messageEditData.getOldMessagePlace().getFriendlyName())
        ));
        oldMessagePlaceItem.setItemMeta(oldMessagePlaceItemMeta);
        inventory.setItem(20, oldMessagePlaceItem);
        ItemStack newMessageItem     = XMaterial.PAPER.parseItem();
        ItemMeta  newMessageItemMeta = newMessageItem.getItemMeta();
        newMessageItemMeta.setDisplayName(MessageUtils.composeMessage("&fNew message"));
        String newMessage;
        if (messageEditData.getNewMessage().isEmpty()) {
            newMessage = MessageUtils.composeMessage("&cMessage removed.\\n(this is not an actual message)");
        } else if (messageEditData.isNewMessageJson()) {
            newMessage = BaseComponent.toLegacyText(ComponentSerializer.parse(messageEditData.getNewMessage()));
        } else {
            newMessage = messageEditData.getNewMessage();
        }
        List<String> newMessageLore = new ArrayList<>(10);
        newMessageLore.add("");
        for (String newMessageData : newMessage.split(messageEditData.isNewMessageJson() ? "\\n" : "\\\\n")) {
            String   newMessageChunk     = "";
            String[] newMessageDataChunk = newMessageData.split(" ");
            for (int newMessageDataChunkIndex = 0; newMessageDataChunkIndex < newMessageDataChunk.length; newMessageDataChunkIndex++) {
                if (newMessageDataChunkIndex > 0 && newMessageDataChunkIndex < newMessageDataChunk.length && !newMessageChunk.isEmpty()) {
                    newMessageChunk += " ";
                }
                newMessageChunk += newMessageDataChunk[newMessageDataChunkIndex];
                if (newMessageDataChunkIndex == newMessageDataChunk.length - 1 || newMessageChunk.length() >= MessageEditorConstants.MESSAGE_LENGTH) {
                    if (newMessageLore.size() == 1) {
                        newMessageLore.add(newMessageChunk);
                    } else {
                        newMessageLore.add(MessageUtils.getLastColors(newMessageLore.get(newMessageLore.size() - 1)) + newMessageChunk);
                    }
                    newMessageChunk = "";
                }
            }
        }
        newMessageLore.add("");
        newMessageLore.add(MessageUtils.composeMessage("&7Click LMB to edit new message"));
        newMessageLore.add(MessageUtils.composeMessage("&7in the override mode."));
        newMessageLore.add("");
        newMessageLore.add(MessageUtils.composeMessage("&7Click RMB to edit new message"));
        newMessageLore.add(MessageUtils.composeMessage("&7in the replace mode."));
        newMessageItemMeta.setLore(newMessageLore);
        newMessageItem.setItemMeta(newMessageItemMeta);
        inventory.setItem(15, newMessageItem);
        ItemStack newMessagePlaceItem     = XMaterial.COMPASS.parseItem();
        ItemMeta  newMessagePlaceItemMeta = newMessagePlaceItem.getItemMeta();
        newMessagePlaceItemMeta.setDisplayName(MessageUtils.composeMessage("&fNew message place"));
        newMessagePlaceItemMeta.setLore(Arrays.asList(
            "",
            MessageUtils.composeMessage("&7ID: &e" + messageEditData.getNewMessagePlace().name()),
            MessageUtils.composeMessage("&7Friendly name: &e" + messageEditData.getNewMessagePlace().getFriendlyName()),
            "",
            MessageUtils.composeMessage("&7Click to edit new message place.")
        ));
        newMessagePlaceItem.setItemMeta(newMessagePlaceItemMeta);
        inventory.setItem(24, newMessagePlaceItem);
        inventory.setItem(22, this.arrowItem);
        ItemStack doneItem     = XMaterial.GREEN_TERRACOTTA.parseItem();
        ItemMeta  doneItemMeta = doneItem.getItemMeta();
        doneItemMeta.setDisplayName(MessageUtils.composeMessage("&aDone"));
        doneItemMeta.setLore(Arrays.asList(
            "",
            MessageUtils.composeMessage("&7Click to save message edit"),
            MessageUtils.composeMessage("&7and apply it to your server.")
        ));
        doneItem.setItemMeta(doneItemMeta);
        inventory.setItem(48, doneItem);
        ItemStack cancelItem     = XMaterial.RED_TERRACOTTA.parseItem();
        ItemMeta  cancelItemMeta = cancelItem.getItemMeta();
        cancelItemMeta.setDisplayName(MessageUtils.composeMessage("&cCancel"));
        cancelItemMeta.setLore(Arrays.asList(
            "",
            MessageUtils.composeMessage("&7Click to cancel message edit.")
        ));
        cancelItem.setItemMeta(cancelItemMeta);
        inventory.setItem(50, cancelItem);
        player.openInventory(inventory);
        if (playSound) {
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
        }
    }
}
