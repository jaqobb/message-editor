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

package dev.jaqobb.messageeditor.listener.player;

import com.cryptomorin.xseries.XSound;
import dev.jaqobb.messageeditor.MessageEditorConstants;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.message.MessageEditData;
import dev.jaqobb.messageeditor.message.MessagePlace;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class PlayerChatListener implements Listener {

    private final MessageEditorPlugin plugin;

    public PlayerChatListener(final MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        MessageEditData messageEditData = this.plugin.getCurrentMessageEditData(player.getUniqueId());
        if (messageEditData == null) {
            return;
        }
        MessageEditData.Mode messageEditDataMode = messageEditData.getCurrentMode();
        if (messageEditDataMode == MessageEditData.Mode.NONE) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        if (message.equals("done")) {
            if (messageEditDataMode == MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_KEY || messageEditDataMode == MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_VALUE) {
                messageEditData.setCurrentMode(MessageEditData.Mode.NONE);
                messageEditData.setOldMessagePatternKey("");
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getMenuManager().openMenu(player, messageEditData, true));
            } else if (messageEditDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE) {
                messageEditData.setCurrentMode(MessageEditData.Mode.NONE);
                if (!messageEditData.getNewMessageCache().isEmpty()) {
                    messageEditData.setNewMessage(messageEditData.getNewMessageCache());
                    try {
                        new JSONParser().parse(messageEditData.getNewMessage());
                        messageEditData.setNewMessageJson(true);
                    } catch (ParseException exception) {
                        messageEditData.setNewMessage(ChatColor.translateAlternateColorCodes('&', messageEditData.getNewMessage()));
                        messageEditData.setNewMessageJson(false);
                    }
                    messageEditData.setNewMessageCache("");
                }
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getMenuManager().openMenu(player, messageEditData, true));
            } else if (messageEditDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_KEY || messageEditDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_VALUE) {
                messageEditData.setCurrentMode(MessageEditData.Mode.NONE);
                messageEditData.setNewMessageKey("");
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getMenuManager().openMenu(player, messageEditData, true));
            }
        } else if (messageEditDataMode == MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_KEY) {
            messageEditData.setOldMessagePatternKey(message);
            messageEditData.setCurrentMode(MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_VALUE);
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Now enter old message pattern value, that is what you want the key to be replaced with, or enter '" + ChatColor.YELLOW + "done" + ChatColor.GRAY + "' if you are done replacing everything you want.");
        } else if (messageEditDataMode == MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_VALUE) {
            String oldMessagePatternKey = messageEditData.getOldMessagePatternKey();
            String oldMessagePatternValue = message;
            messageEditData.setCurrentMode(MessageEditData.Mode.EDITING_OLD_MESSAGE_PATTERN_KEY);
            messageEditData.setOldMessage(messageEditData.getOldMessage().replaceFirst(Pattern.quote(oldMessagePatternKey), Matcher.quoteReplacement(oldMessagePatternValue.replace("\\", "\\\\"))));
            messageEditData.setOldMessagePattern(messageEditData.getOldMessagePattern().replaceFirst(Pattern.quote(oldMessagePatternKey.replaceAll(MessageEditorConstants.SPECIAL_REGEX_CHARACTERS, "\\\\$0")), Matcher.quoteReplacement(oldMessagePatternValue)));
            try {
                new JSONParser().parse(messageEditData.getOldMessage());
                messageEditData.setOldMessageJson(true);
            } catch (ParseException exception) {
                messageEditData.setOldMessage(ChatColor.translateAlternateColorCodes('&', messageEditData.getOldMessage()));
                messageEditData.setOldMessagePattern(ChatColor.translateAlternateColorCodes('&', messageEditData.getOldMessagePattern()));
                messageEditData.setOldMessageJson(false);
            }
            messageEditData.setOldMessagePatternKey("");
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "The first occurence of '" + ChatColor.YELLOW + oldMessagePatternKey + ChatColor.GRAY + "' has been replaced with '" + ChatColor.YELLOW + oldMessagePatternValue + ChatColor.GRAY + "'.");
            player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Enter old message pattern key, that is what you want to replace, or enter '" + ChatColor.YELLOW + "done" + ChatColor.GRAY + "' if you are done replacing everything you want.");
        } else if (messageEditDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE) {
            MessagePlace newMessagePlace = messageEditData.getNewMessagePlace();
            if ((newMessagePlace == MessagePlace.GAME_CHAT || newMessagePlace == MessagePlace.SYSTEM_CHAT || newMessagePlace == MessagePlace.ACTION_BAR) && messageEditData.getNewMessageCache().isEmpty() && message.equals("remove")) {
                messageEditData.setCurrentMode(MessageEditData.Mode.NONE);
                messageEditData.setNewMessage("");
                messageEditData.setNewMessageJson(false);
                messageEditData.setNewMessageCache("");
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getMenuManager().openMenu(player, messageEditData, true));
            } else {
                messageEditData.setNewMessageCache(messageEditData.getNewMessageCache() + message);
                player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Message has been added. Continue if your message is longer and had to divide it into parts. Otherwise enter '" + ChatColor.YELLOW + "done" + ChatColor.GRAY + "' to set the new message.");
                player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            }
        } else if (messageEditDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_KEY) {
            messageEditData.setNewMessageKey(message);
            messageEditData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_VALUE);
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Now enter new message value, that is what you want the key to be replaced with, or enter '" + ChatColor.YELLOW + "done" + ChatColor.GRAY + "' if you are done replacing everything you want.");
        } else if (messageEditDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_VALUE) {
            String newMessageKey = messageEditData.getNewMessageKey();
            String newMessageValue = message;
            messageEditData.setCurrentMode(MessageEditData.Mode.EDITING_NEW_MESSAGE_KEY);
            messageEditData.setNewMessage(messageEditData.getNewMessage().replaceFirst(Pattern.quote(newMessageKey), Matcher.quoteReplacement(newMessageValue)));
            try {
                new JSONParser().parse(messageEditData.getNewMessage());
                messageEditData.setNewMessageJson(true);
            } catch (ParseException exception) {
                messageEditData.setNewMessage(ChatColor.translateAlternateColorCodes('&', messageEditData.getNewMessage()));
                messageEditData.setNewMessageJson(false);
            }
            messageEditData.setNewMessageKey("");
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound(), 1.0F, 1.0F);
            player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "The first occurence of '" + ChatColor.YELLOW + newMessageKey + ChatColor.GRAY + "' has been replaced with '" + ChatColor.YELLOW + newMessageValue + ChatColor.GRAY + "'.");
            player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Enter new message key, that is what you want to replace, or enter '" + ChatColor.YELLOW + "done" + ChatColor.GRAY + "' if you are done replacing everything you want.");
        } else if (messageEditDataMode == MessageEditData.Mode.EDITING_NEW_MESSAGE_PLACE) {
            MessagePlace messagePlace = MessagePlace.fromName(message);
            if (messagePlace == null) {
                player.playSound(player.getLocation(), XSound.ENTITY_ITEM_BREAK.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.RED + "Could not convert '" + ChatColor.GRAY + message + ChatColor.RED + "' to message place.");
                return;
            }
            if (messagePlace != MessagePlace.GAME_CHAT && messagePlace != MessagePlace.SYSTEM_CHAT && messagePlace != MessagePlace.ACTION_BAR) {
                player.playSound(player.getLocation(), XSound.BLOCK_ANVIL_HIT.parseSound(), 1.0F, 1.0F);
                player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.RED + "This message place is not available.");
                player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Available message places:");
                player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "- " + ChatColor.YELLOW + MessagePlace.GAME_CHAT.name() + ChatColor.GRAY + " (" + ChatColor.YELLOW + MessagePlace.GAME_CHAT.getFriendlyName() + ChatColor.GRAY + ")");
                player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "- " + ChatColor.YELLOW + MessagePlace.SYSTEM_CHAT.name() + ChatColor.GRAY + " (" + ChatColor.YELLOW + MessagePlace.SYSTEM_CHAT.getFriendlyName() + ChatColor.GRAY + ")");
                player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "- " + ChatColor.YELLOW + MessagePlace.ACTION_BAR.name() + ChatColor.GRAY + " (" + ChatColor.YELLOW + MessagePlace.ACTION_BAR.getFriendlyName() + ChatColor.GRAY + ")");
                return;
            }
            messageEditData.setCurrentMode(MessageEditData.Mode.NONE);
            messageEditData.setNewMessagePlace(messagePlace);
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getMenuManager().openMenu(player, messageEditData, true));
        }
    }
}
