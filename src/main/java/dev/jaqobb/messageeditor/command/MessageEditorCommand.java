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

package dev.jaqobb.messageeditor.command;

import dev.jaqobb.messageeditor.MessageEditorConstants;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.data.MessageData;
import dev.jaqobb.messageeditor.data.MessagePlace;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageEditorCommand implements CommandExecutor {

    private final MessageEditorPlugin plugin;

    public MessageEditorCommand(final MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
        final CommandSender sender,
        final Command command,
        final String label,
        final String[] arguments
    ) {
        if (!sender.hasPermission("messageeditor.use")) {
            sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.RED + "You do not have the required permissions to do that.");
            return true;
        }
        if (arguments.length == 0) {
            this.sendHelpMessage(sender, label);
            return true;
        }
        if (arguments[0].equalsIgnoreCase("reload")) {
            if (arguments.length != 1) {
                sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " reload" + ChatColor.GRAY + ".");
                return true;
            }
            this.plugin.clearCachedMessages();
            this.plugin.reloadConfig();
            sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Plugin has been reloaded.");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("edit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.RED + "Only players can do that.");
                return true;
            }
            Player player = (Player) sender;
            if (arguments.length != 2) {
                player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " edit <message ID>" + ChatColor.GRAY + ".");
                return true;
            }
            MessageData messageData = this.plugin.getCachedMessageData(arguments[1]);
            if (messageData == null) {
                player.sendMessage(MessageEditorConstants.PREFIX + ChatColor.RED + "There is no cached message data attached to the '" + ChatColor.GRAY + arguments[1] + ChatColor.RED + "' message ID.");
                return true;
            }
            this.plugin.getMenuManager().openMenu(player, messageData);
            return true;
        }
        if (arguments[0].equalsIgnoreCase("activate")) {
            if (arguments.length == 1) {
                sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " activate <message places>" + ChatColor.GRAY + ".");
                sender.sendMessage(MessageEditorConstants.PREFIX);
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            int affectedMessagePlaces = 0;
            for (int argumentIndex = 1; argumentIndex < arguments.length; argumentIndex++) {
                String argument = arguments[argumentIndex];
                MessagePlace messagePlace = MessagePlace.fromName(argument);
                if (messagePlace == null) {
                    sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.RED + "Could not convert '" + ChatColor.GRAY + argument + ChatColor.RED + "' to message place.");
                    continue;
                }
                if (!messagePlace.isSupported()) {
                    sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + messagePlace.getFriendlyName() + ChatColor.RED + " message place is not supported by your server.");
                    continue;
                }
                if (!messagePlace.isAnalyzingActivated()) {
                    messagePlace.setAnalyzingActivated(true);
                    affectedMessagePlaces++;
                } else {
                    sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.RED + "Analyzing " + ChatColor.GRAY + messagePlace.getFriendlyName() + ChatColor.RED + " message place is already activated.");
                }
            }
            sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "You have activated analyzing " + ChatColor.YELLOW + affectedMessagePlaces + ChatColor.GRAY + " message place(s).");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate")) {
            if (arguments.length == 1) {
                sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " deactivate <message places>" + ChatColor.GRAY + ".");
                sender.sendMessage(MessageEditorConstants.PREFIX);
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            int affectedMessagePlaces = 0;
            for (int argumentIndex = 1; argumentIndex < arguments.length; argumentIndex++) {
                String argument = arguments[argumentIndex];
                MessagePlace messagePlace = MessagePlace.fromName(argument);
                if (messagePlace == null) {
                    sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.RED + "Could not convert '" + ChatColor.GRAY + argument + ChatColor.RED + "' to message place.");
                    continue;
                }
                if (!messagePlace.isSupported()) {
                    sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + messagePlace.getFriendlyName() + ChatColor.RED + " message place is not supported by your server.");
                    continue;
                }
                if (messagePlace.isAnalyzingActivated()) {
                    messagePlace.setAnalyzingActivated(false);
                    affectedMessagePlaces++;
                } else {
                    sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.RED + "Analyzing " + ChatColor.GRAY + messagePlace.getFriendlyName() + ChatColor.RED + " message place is already deactivated.");
                }
            }
            sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "You have deactivated analyzing " + ChatColor.YELLOW + affectedMessagePlaces + ChatColor.GRAY + " message place(s).");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate-all") || arguments[0].equalsIgnoreCase("deactivateall")) {
            if (arguments.length != 1) {
                sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " deactivate-all" + ChatColor.GRAY + ".");
                return true;
            }
            for (MessagePlace messagePlace : MessagePlace.VALUES) {
                messagePlace.setAnalyzingActivated(false);
            }
            sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "You have deactivated analyzing all message places.");
            return true;
        }
        this.sendHelpMessage(sender, label);
        return true;
    }

    private void sendHelpMessage(
        final CommandSender sender,
        final String label
    ) {
        sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Available commands:");
        sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.YELLOW + "/message-editor reload" + ChatColor.GRAY + " - Reloads plugin.");
        sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.YELLOW + "/message-editor edit <message ID>" + ChatColor.GRAY + " - Opens message editor.");
        sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.YELLOW + "/message-editor activate <message places>" + ChatColor.GRAY + " - Activates analyzing specified message place(s).");
        sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.YELLOW + "/message-editor deactivate <message places>" + ChatColor.GRAY + " - Deactivates analyzing specified message place(s).");
        sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.YELLOW + "/message-editor deactivate-all" + ChatColor.GRAY + " - Deactivates analyzing all message places.");
        sender.sendMessage(MessageEditorConstants.PREFIX);
        this.sendAvailableMessagePlaces(sender);
    }

    private void sendAvailableMessagePlaces(final CommandSender sender) {
        sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "Available message places:");
        for (MessagePlace messagePlace : MessagePlace.VALUES) {
            if (messagePlace.isSupported()) {
                sender.sendMessage(MessageEditorConstants.PREFIX + ChatColor.GRAY + "- " + ChatColor.YELLOW + messagePlace.name() + ChatColor.GRAY + " (" + ChatColor.YELLOW + messagePlace.getFriendlyName() + ChatColor.GRAY + ")");
            }
        }
    }
}
