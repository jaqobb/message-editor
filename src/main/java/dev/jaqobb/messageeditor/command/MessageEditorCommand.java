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

import com.comphenix.protocol.utility.MinecraftVersion;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.data.place.MessagePlace;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class MessageEditorCommand implements CommandExecutor {

    private final MessageEditorPlugin plugin;

    public MessageEditorCommand(MessageEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!sender.hasPermission("messageeditor.use")) {
            sender.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "You do not have the required permissions to do that.");
            return true;
        }
        if (arguments.length == 0) {
            this.sendHelpMessage(sender, label);
            return true;
        }
        if (arguments[0].equalsIgnoreCase("reload")) {
            if (arguments.length != 1) {
                sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " reload" + ChatColor.GRAY + ".");
                return true;
            }
            this.plugin.reloadConfig();
            this.plugin.clearCachedMessages();
            sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Plugin has been reloaded.");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("activate")) {
            if (arguments.length == 1) {
                sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " activate <message places>" + ChatColor.GRAY + ".");
                sender.sendMessage(this.plugin.getPrefix());
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            int affectedMessagePlaces = 0;
            for (int index = 1; index < arguments.length; index++) {
                MessagePlace messagePlace = MessagePlace.fromName(arguments[index]);
                if (messagePlace == null) {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Could not convert '" + ChatColor.GRAY + arguments[index] + ChatColor.RED + "' to message place.");
                    continue;
                }
                boolean isValidMinecraftVersion = MinecraftVersion.atOrAbove(messagePlace.getMinimumRequiredMinecraftVersion());
                if (!isValidMinecraftVersion) {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + messagePlace.name() + ChatColor.RED + " message place is not supported on your server.");
                    continue;
                }
                if (!messagePlace.isAnalyzingActivated()) {
                    messagePlace.setAnalyzingActivated(true);
                    affectedMessagePlaces++;
                } else {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Analyzing " + ChatColor.GRAY + messagePlace.name() + ChatColor.RED + " message place is already activated.");
                }
            }
            sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "You have activated analyzing " + ChatColor.YELLOW + affectedMessagePlaces + ChatColor.GRAY + " message place(s).");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate")) {
            if (arguments.length == 1) {
                sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " deactivate <message places>" + ChatColor.GRAY + ".");
                sender.sendMessage(this.plugin.getPrefix());
                this.sendAvailableMessagePlaces(sender);
                return true;
            }
            int affectedMessagePlaces = 0;
            for (int index = 1; index < arguments.length; index++) {
                MessagePlace messagePlace = MessagePlace.fromName(arguments[index]);
                if (messagePlace == null) {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Could not convert '" + ChatColor.GRAY + arguments[index] + ChatColor.RED + "' to message place.");
                    continue;
                }
                boolean isValidMinecraftVersion = MinecraftVersion.atOrAbove(messagePlace.getMinimumRequiredMinecraftVersion());
                if (!isValidMinecraftVersion) {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + messagePlace.name() + ChatColor.RED + " message place is not supported on your server.");
                    continue;
                }
                if (messagePlace.isAnalyzingActivated()) {
                    messagePlace.setAnalyzingActivated(false);
                    affectedMessagePlaces++;
                } else {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Analyzing " + ChatColor.GRAY + messagePlace.name() + ChatColor.RED + " message place is already deactivated.");
                }
            }
            sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "You have deactivated analyzing " + ChatColor.YELLOW + affectedMessagePlaces + ChatColor.GRAY + " message place(s).");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate-all")) {
            if (arguments.length != 1) {
                sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " deactivate-all" + ChatColor.GRAY + ".");
                return true;
            }
            for (MessagePlace messagePlace : MessagePlace.values()) {
                messagePlace.setAnalyzingActivated(false);
            }
            sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "You have deactivated analyzing all message places.");
            return true;
        }
        this.sendHelpMessage(sender, label);
        return true;
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Available commands:");
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.YELLOW + "/" + label + " reload" + ChatColor.GRAY + " - " + ChatColor.YELLOW + "Reloads plugin.");
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.YELLOW + "/" + label + " activate <message places>" + ChatColor.GRAY + " - " + ChatColor.YELLOW + "Activates analyzing specified message place.");
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.YELLOW + "/" + label + " deactivate <message places>" + ChatColor.GRAY + " - " + ChatColor.YELLOW + "Deactivates analyzing specified message place.");
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.YELLOW + "/" + label + " deactivate-all" + ChatColor.GRAY + " - " + ChatColor.YELLOW + "Deactivates analyzing all message places.");
        sender.sendMessage(this.plugin.getPrefix());
        this.sendAvailableMessagePlaces(sender);
    }

    private void sendAvailableMessagePlaces(CommandSender sender) {
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Available message places:");
        for (MessagePlace messagePlace : MessagePlace.values()) {
            boolean isValidMinecraftVersion = MinecraftVersion.atOrAbove(messagePlace.getMinimumRequiredMinecraftVersion());
            if (isValidMinecraftVersion) {
                sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "- " + ChatColor.YELLOW + messagePlace.name());
            }
        }
    }
}
