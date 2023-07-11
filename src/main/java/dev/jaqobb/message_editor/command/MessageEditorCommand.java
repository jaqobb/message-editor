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

package dev.jaqobb.message_editor.command;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class MessageEditorCommand implements CommandExecutor {

    private final MessageEditorPlugin plugin;

    public MessageEditorCommand(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!sender.hasPermission("messageeditor.use")) {
            MessageUtils.sendPrefixedMessage(sender, "&cYou do not have the required permissions to do that.");
            return true;
        }
        if (arguments.length == 0) {
            this.sendHelpMessage(sender, label);
            return true;
        }
        if (arguments[0].equalsIgnoreCase("reload")) {
            if (arguments.length != 1) {
                MessageUtils.sendPrefixedMessage(sender, "&7Correct usage: &e/" + label + " reload&7.");
                return true;
            }
            this.plugin.clearCachedMessages();
            this.plugin.clearCurrentMessageEditsData();
            this.plugin.reloadConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                InventoryView openInventory = player.getOpenInventory();
                if (openInventory.getTitle().equals(MessageUtils.translate("&8Message Editor"))) {
                    player.closeInventory();
                    MessageUtils.sendPrefixedMessage(player, "&7Your message editor menu has been closed due to the plugin reload.");
                }
            }
            MessageUtils.sendPrefixedMessage(sender, "&7Plugin has been reloaded.");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("edit")) {
            if (!(sender instanceof Player)) {
                MessageUtils.sendPrefixedMessage(sender, "&cOnly players can do that.");
                return true;
            }
            Player player = (Player) sender;
            if (arguments.length != 2) {
                MessageUtils.sendPrefixedMessage(player, "&7Correct usage: &e/" + label + " edit &6<message id>&7.");
                return true;
            }
            MessageData data = this.plugin.getCachedMessageData(arguments[1]);
            if (data == null) {
                MessageUtils.sendPrefixedMessage(player, "&cThere is no cached message data attached to the '&7" + arguments[1] + "&c' message id.");
                return true;
            }
            this.plugin.getMenuManager().openMenu(player, data, true);
            return true;
        }
        if (arguments[0].equalsIgnoreCase("activate")) {
            if (arguments.length == 1) {
                MessageUtils.sendPrefixedMessage(sender, "&7Correct usage: &e/" + label + " activate &6<message places>&7.");
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            for (int index = 1; index < arguments.length; index += 1) {
                MessagePlace place = MessagePlace.fromName(arguments[index]);
                if (place == null) {
                    MessageUtils.sendPrefixedMessage(sender, "&cCould not convert '&7" + arguments[index] + "&c' to a message place.");
                    continue;
                }
                if (!place.isSupported()) {
                    MessageUtils.sendPrefixedMessage(sender, "&7" + place.getFriendlyName() + "&cmessage place is not supported by your server.");
                    continue;
                }
                if (!place.isAnalyzing()) {
                    place.setAnalyzing(true);
                    MessageUtils.sendPrefixedMessage(sender, "&7Analyzing &e" + place.getFriendlyName() + " &7message place has been activated.");
                } else {
                    MessageUtils.sendPrefixedMessage(sender, "&cAnalyzing &7" + place.getFriendlyName() + " &cmessage place is already activated.");
                }
            }
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate")) {
            if (arguments.length == 1) {
                MessageUtils.sendPrefixedMessage(sender, "&7Correct usage: &e/" + label + " deactivate &6<message places>&7.");
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            for (int index = 1; index < arguments.length; index += 1) {
                MessagePlace place = MessagePlace.fromName(arguments[index]);
                if (place == null) {
                    MessageUtils.sendPrefixedMessage(sender, "&cCould not convert '&7" + arguments[index] + "&c' to a message place.");
                    continue;
                }
                if (!place.isSupported()) {
                    MessageUtils.sendPrefixedMessage(sender, "&7" + place.getFriendlyName() + "&cmessage place is not supported by your server.");
                    continue;
                }
                if (place.isAnalyzing()) {
                    place.setAnalyzing(false);
                    MessageUtils.sendPrefixedMessage(sender, "&7Analyzing &e" + place.getFriendlyName() + " &7message place has been deactivated.");
                } else {
                    MessageUtils.sendPrefixedMessage(sender, "&cAnalyzing &7" + place.getFriendlyName() + " &cmessage place is already deactivated.");
                }
            }
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate-all") || arguments[0].equalsIgnoreCase("deactivateall")) {
            if (arguments.length != 1) {
                MessageUtils.sendPrefixedMessage(sender, "&7Correct usage: &e/" + label + " deactivate-all&7.");
                return true;
            }
            for (MessagePlace place : MessagePlace.VALUES) {
                place.setAnalyzing(false);
            }
            MessageUtils.sendMessage(sender, "&7You have deactivated analyzing all message places.");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("migrate")) {
            if (arguments.length != 1) {
                MessageUtils.sendPrefixedMessage(sender, "&7Correct usage: &e/" + label + " migrate&7.");
                return true;
            }
            List<MessageEdit> edits = (List<MessageEdit>) this.plugin.getConfig().getList("message-edits");
            if (edits.isEmpty()) {
                MessageUtils.sendPrefixedMessage(sender, "&cThere are no message edits to migrate.");
                return true;
            }
            int migrated = 0;
            for (MessageEdit edit : edits) {
                String id = MessageUtils.generateId(edit.getMessageBeforePlace());
                File file = new File(this.plugin.getDataFolder(), "edits" + File.separator + id + ".yml");
                try {
                    if (!file.createNewFile()) {
                        MessageUtils.sendPrefixedMessage(sender, "&cCould not create message edit file for message edit '&7" + id + "&c'.");
                        continue;
                    }
                } catch (IOException exception) {
                    this.plugin.getLogger().log(Level.WARNING, "Could not create message edit file for message edit '" + id + "'.", exception);
                    MessageUtils.sendPrefixedMessage(sender, "&cCould not create message edit file for message edit '&7" + id + "&c', check console for more information.");
                    continue;
                }
                FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                for (Map.Entry<String, Object> entry : edit.serialize().entrySet()) {
                    configuration.set(entry.getKey(), entry.getValue());
                }
                try {
                    configuration.save(file);
                    migrated += 1;
                } catch (IOException exception) {
                    this.plugin.getLogger().log(Level.WARNING, "Could not save message edit file for message edit '" + id + "'.", exception);
                    MessageUtils.sendPrefixedMessage(sender, "&cCould not save message edit file for message edit '&7" + id + "&c', check console for more information.");
                }
            }
            this.plugin.getConfig().set("message-edits", new ArrayList<>());
            this.plugin.saveConfig();
            MessageUtils.sendPrefixedMessage(sender, "&7You have migrated &e" + migrated + "&7/&e" + edits.size() + " &7message edits.");
            return true;
        }
        this.sendHelpMessage(sender, label);
        return true;
    }

    private void sendHelpMessage(CommandSender target, String label) {
        MessageUtils.sendPrefixedMessage(target, "&7Correct usage: &e/message-editor &e<label> &6[<arguments>]&7.");
        MessageUtils.sendMessage(target, " &8- &ereload &7- Reloads plugin.");
        MessageUtils.sendMessage(target, " &8- &eedit &6<message id> &7- Opens message editor.");
        MessageUtils.sendMessage(target, " &8- &eactivate &6<message places> &7- Activates analyzing message places.");
        MessageUtils.sendMessage(target, " &8- &edeactivate &6<message places> &7- Dectivates analyzing message places.");
        MessageUtils.sendMessage(target, " &8- &edeactivate-all &7- Deactivates analyzing all message places.");
        MessageUtils.sendMessage(target, " &8- &emigrate &7- Migrates old message edits to the new per-file system.");
        this.sendAvailableMessagePlaces(target);
    }

    private void sendAvailableMessagePlaces(CommandSender target) {
        MessageUtils.sendPrefixedMessage(target, "&7Available message places:");
        for (MessagePlace place : MessagePlace.VALUES) {
            if (place.isSupported()) {
                MessageUtils.sendMessage(target, " &8- &e" + place.name() + " &7(&e" + place.getFriendlyName() + "&7)");
            }
        }
    }
}
