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

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.comphenix.protocol.utility.MinecraftVersion;
import com.cryptomorin.xseries.XSound;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import dev.jaqobb.message_editor.MessageEditorConstants;
import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageEditData;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;

public final class PlayerChatListener implements Listener {

    private final MessageEditorPlugin plugin;

    public PlayerChatListener(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        MessageEditData editData = this.plugin.getCurrentMessageEditData(player.getUniqueId());
        if (editData == null) {
            return;
        }
        MessageEditData.Mode editDataMode = editData.getCurrentMode();
        if (editDataMode == MessageEditData.Mode.NONE) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        if (message.equals("done")) {
            editData.setCurrentMode(MessageEditData.Mode.NONE);
            if (editDataMode == MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_KEY || editDataMode == MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_VALUE) {
                editData.setOldMessagePatternKey("");
            } else if (editDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE) {
                if (!editData.getNewMessageCache().isEmpty()) {
                    editData.setNewMessage(editData.getNewMessageCache());
                    try {
                        // Streams is being used instead of JsonParser
                        // because JsonParser parses the string in lenient mode
                        // which we don't want.
                        Streams.parse(new JsonReader(new StringReader(editData.getNewMessage())));
                        editData.setNewMessageJson(true);
                    } catch (JsonParseException exception) {
                        editData.setNewMessage(MessageUtils.translate(editData.getNewMessage()));
                        editData.setNewMessageJson(false);
                    }
                    editData.setNewMessageCache("");
                }
            } else if (editDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_KEY || editDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_VALUE) {
                editData.setNewMessageKey("");
            }
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getMenuManager().openMenu(player, editData, true));
        } else if (editDataMode == MessageEditData.Mode.EDITING_FILE_NAME) {
            if (message.charAt(0) == '#') {
                player.playSound(player.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.translateWithPrefix("&cMessage edit file name cannot start with the '&7#&c' character."));
                return;
            }
            File file = new File(this.plugin.getDataFolder(), "edits" + File.separator + message + ".yml");
            if (file.exists()) {
                player.playSound(player.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.translateWithPrefix("&cThere is already a message edit that uses a file named '&7" + message + ".yml&c'."));
                return;
            }
            editData.setCurrentMode(MessageEditData.Mode.NONE);
            editData.setFileName(message);
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getMenuManager().openMenu(player, editData, true));
        } else if (editDataMode == MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_KEY) {
            editData.setOldMessagePatternKey(message);
            editData.setCurrentMode(MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_VALUE);
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.translateWithPrefix("&7Now enter old message pattern value, that is what you want the key to be replaced with, or enter '&edone&7' if you are done replacing everything you want."));
        } else if (editDataMode == MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_VALUE) {
            String patternKey = editData.getOldMessagePatternKey();
            String patternValue = message;
            editData.setCurrentMode(MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_KEY);
            editData.setOldMessage(editData.getOldMessage().replaceFirst(Pattern.quote(patternKey), Matcher.quoteReplacement(patternValue.replace("\\", "\\\\"))));
            editData.setOldMessagePattern(editData.getOldMessagePattern().replaceFirst(Pattern.quote(patternKey.replaceAll(MessageEditorConstants.SPECIAL_REGEX_CHARACTERS, "\\\\$0")), Matcher.quoteReplacement(patternValue)));
            try {
                // Streams is being used instead of JsonParser
                // because JsonParser parses the string in lenient mode
                // which we don't want.
                Streams.parse(new JsonReader(new StringReader(editData.getOldMessage())));
                editData.setOldMessageJson(true);
            } catch (JsonParseException exception) {
                editData.setOldMessage(MessageUtils.translate(editData.getOldMessage()));
                editData.setOldMessagePattern(MessageUtils.translate(editData.getOldMessagePattern()));
                editData.setOldMessageJson(false);
            }
            editData.setOldMessagePatternKey("");
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.translateWithPrefix("&7The first occurence of '&e" + patternKey + "&7' has been replaced with '&e" + patternValue + "&7'."));
            player.sendMessage(MessageUtils.translateWithPrefix("&7Enter old message pattern key, that is what you want to replace, or enter '&edone&7' if you are done replacing everything you want."));
        } else if (editDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE) {
            MessagePlace place = editData.getNewMessagePlace();
            if ((place == MessagePlace.GAME_CHAT || place == MessagePlace.SYSTEM_CHAT || place == MessagePlace.ACTION_BAR) && editData.getNewMessageCache().isEmpty() && message.equals("remove")) {
                editData.setCurrentMode(MessageEditData.Mode.NONE);
                editData.setNewMessage("");
                editData.setNewMessageJson(false);
                editData.setNewMessageCache("");
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getMenuManager().openMenu(player, editData, true));
                return;
            }
            editData.setNewMessageCache(editData.getNewMessageCache() + message);
            player.sendMessage(MessageUtils.translateWithPrefix("&7Message has been added. Continue if your message is longer and had to divide it into parts. Otherwise enter '&edone&7' to set the new message."));
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
        } else if (editDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_KEY) {
            editData.setNewMessageKey(message);
            editData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_VALUE);
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.translateWithPrefix("&7Now enter new message value, that is what you want the key to be replaced with, or enter '&edone&7' if you are done replacing everything you want."));
        } else if (editDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_VALUE) {
            String key = editData.getNewMessageKey();
            String value = message;
            editData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_KEY);
            editData.setNewMessage(editData.getNewMessage().replaceFirst(Pattern.quote(key), Matcher.quoteReplacement(value)));
            try {
                // Streams is being used instead of JsonParser
                // because JsonParser parses the string in lenient mode
                // which we don't want.
                Streams.parse(new JsonReader(new StringReader(editData.getNewMessage())));
                editData.setNewMessageJson(true);
            } catch (JsonParseException exception) {
                editData.setNewMessage(MessageUtils.translate(editData.getNewMessage()));
                editData.setNewMessageJson(false);
            }
            editData.setNewMessageKey("");
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageUtils.translateWithPrefix("&7The first occurence of '&e" + key + "&7' has been replaced with '&e" + value + "&7'."));
            player.sendMessage(MessageUtils.translateWithPrefix("&7Enter new message key, that is what you want to replace, or enter '&edone&7' if you are done replacing everything you want."));
        } else if (editDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_PLACE) {
            MessagePlace place = MessagePlace.fromName(message);
            if (place == null) {
                player.playSound(player.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.translateWithPrefix("&cCould not convert '&7" + message + "&c' to a message place."));
                return;
            }
            if (!place.isSupported() || (place != MessagePlace.GAME_CHAT && place != MessagePlace.SYSTEM_CHAT && place != MessagePlace.ACTION_BAR)) {
                player.playSound(player.getLocation(), XSound.BLOCK_ANVIL_HIT.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageUtils.translateWithPrefix("&cThis message place is not supported by your server or is unavailable."));
                player.sendMessage(MessageUtils.translateWithPrefix("&7Available message places:"));
                Collection<MessagePlace> availableMessagePlaces = new ArrayList<>(3);
                if (!MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                    availableMessagePlaces.add(MessagePlace.GAME_CHAT);
                }
                availableMessagePlaces.add(MessagePlace.SYSTEM_CHAT);
                availableMessagePlaces.add(MessagePlace.ACTION_BAR);
                for (MessagePlace availableMessagePlace : availableMessagePlaces) {
                    player.sendMessage(MessageUtils.translateWithPrefix("&7- &e" + availableMessagePlace.name() + " &7(&e" + availableMessagePlace.getFriendlyName() + "&7)"));
                }
                return;
            }
            if (place == MessagePlace.GAME_CHAT && MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                place = MessagePlace.SYSTEM_CHAT;
            }
            editData.setCurrentMode(MessageEditData.Mode.NONE);
            editData.setNewMessagePlace(place);
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getMenuManager().openMenu(player, editData, true));
        }
    }
}
