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

package dev.jaqobb.messageeditor.command;

import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.message.MessageData;
import dev.jaqobb.messageeditor.message.MessagePlace;
import dev.jaqobb.messageeditor.util.MessageUtils;
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
            sender.sendMessage(MessageUtils.composeMessageWithPrefix("&cYou do not have the required permissions to do that."));
            return true;
        }
        if (arguments.length == 0) {
            this.sendHelpMessage(sender, label);
            return true;
        }
        if (arguments[0].equalsIgnoreCase("reload")) {
            if (arguments.length != 1) {
                sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7Correct usage: &e/" + label + " reload&7."));
                return true;
            }
            this.plugin.clearCachedMessages();
            this.plugin.clearCurrentMessageEditsData();
            this.plugin.reloadConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                InventoryView inventoryView = player.getOpenInventory();
                if (inventoryView.getTitle().equals(MessageUtils.composeMessage("&8Message Editor"))) {
                    player.closeInventory();
                    player.sendMessage(MessageUtils.composeMessageWithPrefix("&7Your message editor menu has been closed due to the plugin reload."));
                }
            }
            sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7Plugin has been reloaded."));
            return true;
        }
        if (arguments[0].equalsIgnoreCase("edit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.composeMessageWithPrefix("&cOnly players can do that."));
                return true;
            }
            Player player = (Player) sender;
            if (arguments.length != 2) {
                player.sendMessage(MessageUtils.composeMessageWithPrefix("&7Correct usage: &e/" + label + " edit <message ID>&7."));
                return true;
            }
            MessageData messageData = this.plugin.getCachedMessageData(arguments[1]);
            if (messageData == null) {
                player.sendMessage(MessageUtils.composeMessageWithPrefix("&cThere is no cached message data attached to the '&7" + arguments[1] + "&c' message ID."));
                return true;
            }
            this.plugin.getMenuManager().openMenu(player, messageData, true);
            return true;
        }
        if (arguments[0].equalsIgnoreCase("activate")) {
            if (arguments.length == 1) {
                sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7Correct usage: &e/" + label + " activate <message places>&7."));
                sender.sendMessage(MessageUtils.composeMessageWithPrefix(""));
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            int affectedMessagePlaces = 0;
            for (int argumentIndex = 1; argumentIndex < arguments.length; argumentIndex++) {
                String       argument     = arguments[argumentIndex];
                MessagePlace messagePlace = MessagePlace.fromName(argument);
                if (messagePlace == null) {
                    sender.sendMessage(MessageUtils.composeMessageWithPrefix("&cCould not convert '&7" + argument + "&c' to a message place."));
                    continue;
                }
                if (!messagePlace.isSupported()) {
                    sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7" + messagePlace.getFriendlyName() + "&cmessage place is not supported by your server."));
                    continue;
                }
                if (!messagePlace.isAnalyzingActivated()) {
                    messagePlace.setAnalyzingActivated(true);
                    affectedMessagePlaces++;
                } else {
                    sender.sendMessage(MessageUtils.composeMessageWithPrefix("&cAnalyzing &7" + messagePlace.getFriendlyName() + " &cmessage place is already activated."));
                }
            }
            sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7You have activated analyzing &e" + affectedMessagePlaces + " &7message place(s)."));
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate")) {
            if (arguments.length == 1) {
                sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7Correct usage: &e/" + label + " deactivate <message places>&7."));
                sender.sendMessage(MessageUtils.composeMessageWithPrefix(""));
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            int affectedMessagePlaces = 0;
            for (int argumentIndex = 1; argumentIndex < arguments.length; argumentIndex++) {
                String       argument     = arguments[argumentIndex];
                MessagePlace messagePlace = MessagePlace.fromName(argument);
                if (messagePlace == null) {
                    sender.sendMessage(MessageUtils.composeMessageWithPrefix("&cCould not convert '&7" + argument + "&c' to a message place."));
                    continue;
                }
                if (!messagePlace.isSupported()) {
                    sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7" + messagePlace.getFriendlyName() + "&cmessage place is not supported by your server."));
                    continue;
                }
                if (messagePlace.isAnalyzingActivated()) {
                    messagePlace.setAnalyzingActivated(false);
                    affectedMessagePlaces++;
                } else {
                    sender.sendMessage(MessageUtils.composeMessageWithPrefix("&cAnalyzing &7" + messagePlace.getFriendlyName() + " &cmessage place is already deactivated."));
                }
            }
            sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7You have deactivated analyzing &e" + affectedMessagePlaces + " &7message place(s)."));
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate-all") || arguments[0].equalsIgnoreCase("deactivateall")) {
            if (arguments.length != 1) {
                sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7Correct usage: &e/" + label + " deactivate-all&7."));
                return true;
            }
            for (MessagePlace messagePlace : MessagePlace.VALUES) {
                messagePlace.setAnalyzingActivated(false);
            }
            sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7You have deactivated analyzing all message places."));
            return true;
        }
        this.sendHelpMessage(sender, label);
        return true;
    }

    private void sendHelpMessage(
        CommandSender sender,
        String label
    ) {
        sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7Available commands:"));
        sender.sendMessage(MessageUtils.composeMessageWithPrefix("&e/message-editor reload &7- Reloads plugin."));
        sender.sendMessage(MessageUtils.composeMessageWithPrefix("&e/message-editor edit <message ID> &7- Opens message editor."));
        sender.sendMessage(MessageUtils.composeMessageWithPrefix("&e/message-editor activate <message places> &7- Activates analyzing specified message place(s)."));
        sender.sendMessage(MessageUtils.composeMessageWithPrefix("&e/message-editor deactivate <message places> &7- Dectivates analyzing specified message place(s)."));
        sender.sendMessage(MessageUtils.composeMessageWithPrefix("&e/message-editor deactivate-all &7- Deactivates analyzing all message places."));
        sender.sendMessage(MessageUtils.composeMessageWithPrefix(""));
        this.sendAvailableMessagePlaces(sender);
    }

    private void sendAvailableMessagePlaces(CommandSender sender) {
        sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7Available message places:"));
        for (MessagePlace messagePlace : MessagePlace.VALUES) {
            if (messagePlace.isSupported()) {
                sender.sendMessage(MessageUtils.composeMessageWithPrefix("&7- &e" + messagePlace.name() + " &7(&e" + messagePlace.getFriendlyName() + "&7)"));
            }
        }
    }
}
