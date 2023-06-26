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

package dev.jaqobb.message_editor.menu;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import dev.jaqobb.message_editor.MessageEditorConstants;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessageEditData;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MenuManager {

    private static final WrappedGameProfile ARROW_PROFILE;

    private static final int[] BORDER_ITEM_1_SLOTS = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 40, 42, 43, 44, 45, 46, 47, 49, 51, 52, 53};
    private static final ItemStack BORDER_ITEM_1 = constructItem(XMaterial.BLACK_STAINED_GLASS_PANE, " ");

    private static final int[] BORDER_ITEM_2_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 21, 23, 25, 28, 29, 30, 31, 32, 34, 37, 38, 39, 40, 41, 42, 43};
    private static final ItemStack BORDER_ITEM_2 = constructItem(XMaterial.GRAY_STAINED_GLASS_PANE, " ");

    private static final ItemStack MESSAGE_ARROW_ITEM;
    private static final ItemStack MESSAGE_PLACE_ARROW_ITEM;

    private static final ItemStack DONE_ITEM = constructItem(
        XMaterial.GREEN_TERRACOTTA,
        "&aDone",
        "",
        "&7Click to save message edit",
        "&7and apply it to your server."
    );
    private static final ItemStack CANCEL_ITEM = constructItem(
        XMaterial.RED_TERRACOTTA,
        "&cCancel",
        "",
        "&7Click to cancel message edit."
    );

    static {
        String textures = "ewogICJ0aW1lc3RhbXAiIDogMTYwNzM2ODc1MzI0NCwKICAicHJvZmlsZUlkIiA6ICI1MGM4NTEwYjVlYTA0ZDYwYmU5YTdkNTQyZDZjZDE1NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dSaWdodCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMzRlZjA2Mzg1MzcyMjJiMjBmNDgwNjk0ZGFkYzBmODVmYmUwNzU5ZDU4MWFhN2ZjZGYyZTQzMTM5Mzc3MTU4IgogICAgfQogIH0KfQ==";
        ARROW_PROFILE = new WrappedGameProfile(UUID.nameUUIDFromBytes(textures.getBytes(StandardCharsets.UTF_8)), "MHF_ArrowRight");
        ARROW_PROFILE.getProperties().put("textures", new WrappedSignedProperty("textures", textures, null));
        MESSAGE_ARROW_ITEM = constructArrowItem("&e< &7Old message", "&e> &7New message");
        MESSAGE_PLACE_ARROW_ITEM = constructArrowItem("&e< &7Old message place", "&e> &7New message place");
    }

    private final MessageEditorPlugin plugin;

    public MenuManager(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, MessageData data, boolean playSound) {
        MessageEditData editData = new MessageEditData(data);
        this.openMenu(player, editData, playSound);
        this.plugin.setCurrentMessageEdit(player.getUniqueId(), editData);
    }

    public void openMenu(Player player, MessageEditData editData, boolean playSound) {
        Inventory inventory = Bukkit.createInventory(null, 54, MessageUtils.translate("&8Message Editor"));
        for (int slot : BORDER_ITEM_1_SLOTS) {
            inventory.setItem(slot, BORDER_ITEM_1);
        }
        for (int slot : BORDER_ITEM_2_SLOTS) {
            inventory.setItem(slot, BORDER_ITEM_2);
        }
        ItemStack fileNameItem = XMaterial.OAK_SIGN.parseItem();
        ItemMeta fileNameItemMeta = fileNameItem.getItemMeta();
        fileNameItemMeta.setDisplayName(MessageUtils.translate("&6" + editData.getFileName()));
        fileNameItemMeta.setLore(Arrays.asList(
            "",
            MessageUtils.translate("&7This message edit will be saved"),
            MessageUtils.translate("&7in the '&eedits/" + editData.getFileName() + ".yml&7' file"),
            "",
            MessageUtils.translate("&7Click to edit file name.")
        ));
        fileNameItem.setItemMeta(fileNameItemMeta);
        inventory.setItem(4, fileNameItem);
        ItemStack oldMessageItem = XMaterial.PAPER.parseItem();
        ItemMeta oldMessageItemMeta = oldMessageItem.getItemMeta();
        oldMessageItemMeta.setDisplayName(MessageUtils.translate("&fOld message"));
        String oldMessage;
        if (editData.isOldMessageJson()) {
            oldMessage = BaseComponent.toLegacyText(ComponentSerializer.parse(editData.getOldMessage()));
        } else {
            oldMessage = editData.getOldMessage();
        }
        List<String> oldMessageLore = new ArrayList<>();
        oldMessageLore.add("");
        for (String oldMessageData : oldMessage.split(editData.isOldMessageJson() ? "\\n" : "\\\\n")) {
            String oldMessageChunk = "";
            String[] oldMessageDataChunk = oldMessageData.split(" ");
            for (int index = 0; index < oldMessageDataChunk.length; index += 1) {
                if (index > 0 && index < oldMessageDataChunk.length && !oldMessageChunk.isEmpty()) {
                    oldMessageChunk += " ";
                }
                oldMessageChunk += oldMessageDataChunk[index];
                if (index == oldMessageDataChunk.length - 1 || oldMessageChunk.length() >= MessageEditorConstants.MESSAGE_LENGTH) {
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
        oldMessageLore.add(MessageUtils.translate("&7Click to edit old message pattern."));
        oldMessageItemMeta.setLore(oldMessageLore);
        oldMessageItem.setItemMeta(oldMessageItemMeta);
        inventory.setItem(20, oldMessageItem);
        ItemStack oldMessagePlaceItem = XMaterial.COMPASS.parseItem();
        ItemMeta oldMessagePlaceItemMeta = oldMessagePlaceItem.getItemMeta();
        oldMessagePlaceItemMeta.setDisplayName(MessageUtils.translate("&fOld message place"));
        MessagePlace oldPlace = editData.getOldMessagePlace();
        oldMessagePlaceItemMeta.setLore(Arrays.asList(
            "",
            MessageUtils.translate("&7ID: &e" + oldPlace.name()),
            MessageUtils.translate("&7Friendly name: &e" + oldPlace.getFriendlyName())
        ));
        oldMessagePlaceItem.setItemMeta(oldMessagePlaceItemMeta);
        inventory.setItem(29, oldMessagePlaceItem);
        ItemStack newMessageItem = XMaterial.PAPER.parseItem();
        ItemMeta newMessageItemMeta = newMessageItem.getItemMeta();
        newMessageItemMeta.setDisplayName(MessageUtils.translate("&fNew message"));
        String newMessage;
        if (editData.getNewMessage().isEmpty()) {
            newMessage = MessageUtils.translate("&cMessage removed.\\n(this is not an actual message)");
        } else if (editData.isNewMessageJson()) {
            newMessage = BaseComponent.toLegacyText(ComponentSerializer.parse(editData.getNewMessage()));
        } else {
            newMessage = editData.getNewMessage();
        }
        List<String> newMessageLore = new ArrayList<>();
        newMessageLore.add("");
        for (String newMessageData : newMessage.split(editData.isNewMessageJson() ? "\\n" : "\\\\n")) {
            String newMessageChunk = "";
            String[] newMessageDataChunk = newMessageData.split(" ");
            for (int index = 0; index < newMessageDataChunk.length; index += 1) {
                if (index > 0 && index < newMessageDataChunk.length && !newMessageChunk.isEmpty()) {
                    newMessageChunk += " ";
                }
                newMessageChunk += newMessageDataChunk[index];
                if (index == newMessageDataChunk.length - 1 || newMessageChunk.length() >= MessageEditorConstants.MESSAGE_LENGTH) {
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
        newMessageLore.add(MessageUtils.translate("&7Click LMB to edit new message"));
        newMessageLore.add(MessageUtils.translate("&7in the override mode."));
        newMessageLore.add("");
        newMessageLore.add(MessageUtils.translate("&7Click RMB to edit new message"));
        newMessageLore.add(MessageUtils.translate("&7in the replace mode."));
        newMessageItemMeta.setLore(newMessageLore);
        newMessageItem.setItemMeta(newMessageItemMeta);
        inventory.setItem(24, newMessageItem);
        ItemStack newMessagePlaceItem = XMaterial.COMPASS.parseItem();
        ItemMeta newMessagePlaceItemMeta = newMessagePlaceItem.getItemMeta();
        newMessagePlaceItemMeta.setDisplayName(MessageUtils.translate("&fNew message place"));
        List<String> newMessagePlaceItemMetaLore = new ArrayList<>();
        newMessagePlaceItemMetaLore.add("");
        MessagePlace newPlace = editData.getNewMessagePlace();
        newMessagePlaceItemMetaLore.add(MessageUtils.translate("&7ID: &e" + newPlace.name()));
        newMessagePlaceItemMetaLore.add(MessageUtils.translate("&7Friendly name: &e" + newPlace.getFriendlyName()));
        if (!MinecraftVersion.WILD_UPDATE.atOrAbove()) {
            if (newPlace == MessagePlace.GAME_CHAT || newPlace == MessagePlace.SYSTEM_CHAT || newPlace == MessagePlace.ACTION_BAR) {
                newMessagePlaceItemMetaLore.add("");
                newMessagePlaceItemMetaLore.add(MessageUtils.translate("&7Click to edit new message place."));
            }
        } else if (newPlace == MessagePlace.SYSTEM_CHAT || newPlace == MessagePlace.ACTION_BAR) {
            newMessagePlaceItemMetaLore.add("");
            newMessagePlaceItemMetaLore.add(MessageUtils.translate("&7Click to edit new message place."));
        }
        newMessagePlaceItemMeta.setLore(newMessagePlaceItemMetaLore);
        newMessagePlaceItem.setItemMeta(newMessagePlaceItemMeta);
        inventory.setItem(33, newMessagePlaceItem);
        inventory.setItem(22, MESSAGE_ARROW_ITEM);
        inventory.setItem(31, MESSAGE_PLACE_ARROW_ITEM);
        inventory.setItem(50, DONE_ITEM);
        inventory.setItem(48, CANCEL_ITEM);
        player.openInventory(inventory);
        if (playSound) {
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
        }
    }

    private static ItemStack constructArrowItem(String name, String... lore) {
        ItemStack item = constructItem(XMaterial.PLAYER_HEAD, name, lore);
        ItemMeta itemMeta = item.getItemMeta();
        FieldAccessor fieldAccessor = Accessors.getFieldAccessorOrNull(itemMeta.getClass(), "profile", ARROW_PROFILE.getHandle().getClass());
        fieldAccessor.set(itemMeta, ARROW_PROFILE.getHandle());
        item.setItemMeta(itemMeta);
        return item;
    }

    private static ItemStack constructItem(XMaterial material, String name, String... lore) {
        ItemStack item = material.parseItem();
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(MessageUtils.translate(name));
        if (lore != null) {
            itemMeta.setLore(Arrays.stream(lore).map(MessageUtils::translate).collect(Collectors.toList()));
        }
        item.setItemMeta(itemMeta);
        return item;
    }
}
