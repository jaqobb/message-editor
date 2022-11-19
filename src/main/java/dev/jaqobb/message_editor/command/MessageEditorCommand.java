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

package dev.jaqobb.message_editor.command;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public final class MessageEditorCommand implements CommandExecutor {

    private final MessageEditorPlugin plugin;

    public MessageEditorCommand(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!sender.hasPermission("messageeditor.use")) {
            sender.sendMessage(MessageUtils.translateWithPrefix("&cYou do not have the required permissions to do that."));
            return true;
        }
        if (arguments.length == 0) {
            this.sendHelpMessage(sender, label);
            return true;
        }
        if (arguments[0].equalsIgnoreCase("reload")) {
            if (arguments.length != 1) {
                sender.sendMessage(MessageUtils.translateWithPrefix("&7Correct usage: &e/" + label + " reload&7."));
                return true;
            }
            this.plugin.clearCachedMessages();
            this.plugin.clearCurrentMessageEditsData();
            this.plugin.reloadConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                InventoryView openInventory = player.getOpenInventory();
                if (openInventory.getTitle().equals(MessageUtils.translate("&8Message Editor"))) {
                    player.closeInventory();
                    player.sendMessage(MessageUtils.translateWithPrefix("&7Your message editor menu has been closed due to the plugin reload."));
                }
            }
            sender.sendMessage(MessageUtils.translateWithPrefix("&7Plugin has been reloaded."));
            return true;
        }
        if (arguments[0].equalsIgnoreCase("edit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.translateWithPrefix("&cOnly players can do that."));
                return true;
            }
            Player player = (Player) sender;
            if (arguments.length != 2) {
                player.sendMessage(MessageUtils.translateWithPrefix("&7Correct usage: &e/" + label + " edit <message ID>&7."));
                return true;
            }
            MessageData data = this.plugin.getCachedMessageData(arguments[1]);
            if (data == null) {
                player.sendMessage(MessageUtils.translateWithPrefix("&cThere is no cached message data attached to the '&7" + arguments[1] + "&c' message ID."));
                return true;
            }
            this.plugin.getMenuManager().openMenu(player, data, true);
            return true;
        }
        if (arguments[0].equalsIgnoreCase("activate")) {
            if (arguments.length == 1) {
                sender.sendMessage(MessageUtils.translateWithPrefix("&7Correct usage: &e/" + label + " activate <message places>&7."));
                sender.sendMessage(MessageUtils.translateWithPrefix(""));
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            int placesAffected = 0;
            for (int i = 1; i < arguments.length; i += 1) {
                String argument = arguments[i];
                MessagePlace place = MessagePlace.fromName(argument);
                if (place == null) {
                    sender.sendMessage(MessageUtils.translateWithPrefix("&cCould not convert '&7" + argument + "&c' to a message place."));
                    continue;
                }
                if (!place.isSupported()) {
                    sender.sendMessage(MessageUtils.translateWithPrefix("&7" + place.getFriendlyName() + "&cmessage place is not supported by your server."));
                    continue;
                }
                if (!place.isAnalyzing()) {
                    place.setAnalyzing(true);
                    placesAffected += 1;
                } else {
                    sender.sendMessage(MessageUtils.translateWithPrefix("&cAnalyzing &7" + place.getFriendlyName() + " &cmessage place is already activated."));
                }
            }
            sender.sendMessage(MessageUtils.translateWithPrefix("&7You have activated analyzing &e" + placesAffected + " &7message place(s)."));
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate")) {
            if (arguments.length == 1) {
                sender.sendMessage(MessageUtils.translateWithPrefix("&7Correct usage: &e/" + label + " deactivate <message places>&7."));
                sender.sendMessage(MessageUtils.translateWithPrefix(""));
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            int placesAffected = 0;
            for (int i = 1; i < arguments.length; i += 1) {
                String argument = arguments[i];
                MessagePlace place = MessagePlace.fromName(argument);
                if (place == null) {
                    sender.sendMessage(MessageUtils.translateWithPrefix("&cCould not convert '&7" + argument + "&c' to a message place."));
                    continue;
                }
                if (!place.isSupported()) {
                    sender.sendMessage(MessageUtils.translateWithPrefix("&7" + place.getFriendlyName() + "&cmessage place is not supported by your server."));
                    continue;
                }
                if (place.isAnalyzing()) {
                    place.setAnalyzing(false);
                    placesAffected += 1;
                } else {
                    sender.sendMessage(MessageUtils.translateWithPrefix("&cAnalyzing &7" + place.getFriendlyName() + " &cmessage place is already deactivated."));
                }
            }
            sender.sendMessage(MessageUtils.translateWithPrefix("&7You have deactivated analyzing &e" + placesAffected + " &7message place(s)."));
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate-all") || arguments[0].equalsIgnoreCase("deactivateall")) {
            if (arguments.length != 1) {
                sender.sendMessage(MessageUtils.translateWithPrefix("&7Correct usage: &e/" + label + " deactivate-all&7."));
                return true;
            }
            for (MessagePlace place : MessagePlace.VALUES) {
                place.setAnalyzing(false);
            }
            sender.sendMessage(MessageUtils.translateWithPrefix("&7You have deactivated analyzing all message places."));
            return true;
        }
        this.sendHelpMessage(sender, label);
        return true;
    }

    private void sendHelpMessage(CommandSender target, String label) {
        target.sendMessage(MessageUtils.translateWithPrefix("&7Available commands:"));
        target.sendMessage(MessageUtils.translateWithPrefix("&e/message-editor reload &7- Reloads plugin."));
        target.sendMessage(MessageUtils.translateWithPrefix("&e/message-editor edit <message ID> &7- Opens message editor."));
        target.sendMessage(MessageUtils.translateWithPrefix("&e/message-editor activate <message places> &7- Activates analyzing specified message place(s)."));
        target.sendMessage(MessageUtils.translateWithPrefix("&e/message-editor deactivate <message places> &7- Dectivates analyzing specified message place(s)."));
        target.sendMessage(MessageUtils.translateWithPrefix("&e/message-editor deactivate-all &7- Deactivates analyzing all message places."));
        target.sendMessage(MessageUtils.translateWithPrefix(""));
        this.sendAvailableMessagePlaces(target);
    }

    private void sendAvailableMessagePlaces(CommandSender target) {
        target.sendMessage(MessageUtils.translateWithPrefix("&7Available message places:"));
        for (MessagePlace place : MessagePlace.VALUES) {
            if (place.isSupported()) {
                target.sendMessage(MessageUtils.translateWithPrefix("&7- &e" + place.name() + " &7(&e" + place.getFriendlyName() + "&7)"));
            }
        }
    }
}
