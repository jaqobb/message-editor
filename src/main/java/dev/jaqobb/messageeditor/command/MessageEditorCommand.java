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
        if (arguments[0].equalsIgnoreCase("activate")) {
            if (arguments.length == 1) {
                sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " activate <message analyze places>" + ChatColor.GRAY + ".");
                sender.sendMessage(this.plugin.getPrefix());
                this.sendAvailablePlacesToAnalyze(sender);
                return true;
            }
            int activatedMessageAnalyzePlaces = 0;
            for (int index = 1; index < arguments.length; index++) {
                MessagePlace messageAnalyzePlace = MessagePlace.fromName(arguments[index]);
                if (messageAnalyzePlace == null) {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Could not convert '" + ChatColor.GRAY + arguments[index] + ChatColor.RED + "' to message analyze place.");
                    continue;
                }
                boolean isValidMinecraftVersion = MinecraftVersion.atOrAbove(messageAnalyzePlace.getMinimumRequiredMinecraftVersion());
                if (!isValidMinecraftVersion) {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + messageAnalyzePlace.name() + ChatColor.RED + " message analyze place is not supported on your server.");
                    continue;
                }
                if (!this.plugin.isMessageAnalyzePlaceActive(messageAnalyzePlace)) {
                    this.plugin.activateMessageAnalyzePlace(messageAnalyzePlace);
                    activatedMessageAnalyzePlaces++;
                } else {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + messageAnalyzePlace.name() + ChatColor.RED + " message analyze place is already active.");
                }
            }
            sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "You have activated " + ChatColor.YELLOW + activatedMessageAnalyzePlaces + ChatColor.GRAY + " message analyze place(s).");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate")) {
            if (arguments.length == 1) {
                sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Correct usage: " + ChatColor.YELLOW + "/" + label + " deactivate <message analyze places>" + ChatColor.GRAY + ".");
                sender.sendMessage(this.plugin.getPrefix());
                this.sendAvailablePlacesToAnalyze(sender);
                return true;
            }
            int deactivatedMessageAnalyzePlaces = 0;
            for (int index = 1; index < arguments.length; index++) {
                MessagePlace messageAnalyzePlace = MessagePlace.fromName(arguments[index]);
                if (messageAnalyzePlace == null) {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Could not convert '" + ChatColor.GRAY + arguments[index] + ChatColor.RED + "' to message analyze place.");
                    continue;
                }
                boolean isValidMinecraftVersion = MinecraftVersion.atOrAbove(messageAnalyzePlace.getMinimumRequiredMinecraftVersion());
                if (!isValidMinecraftVersion) {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + messageAnalyzePlace.name() + ChatColor.RED + " message analyze place is not supported on your server.");
                    continue;
                }
                if (this.plugin.isMessageAnalyzePlaceActive(messageAnalyzePlace)) {
                    this.plugin.deactivateMessageAnalyzePlace(messageAnalyzePlace);
                    deactivatedMessageAnalyzePlaces++;
                } else {
                    sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + messageAnalyzePlace.name() + ChatColor.RED + " message analyze place is not active.");
                }
            }
            sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "You have deactivated " + ChatColor.YELLOW + deactivatedMessageAnalyzePlaces + ChatColor.GRAY + " message analyze place(s).");
            return true;
        }
        if (arguments[0].equalsIgnoreCase("deactivate-all")) {
            this.plugin.deactivateAllActiveMessageAnalyzePlaces();
            sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "You have deactivated all active message analyze places.");
            return true;
        }
        this.sendHelpMessage(sender, label);
        return true;
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Available commands:");
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.YELLOW + "/" + label + " activate <message analyze places> " + ChatColor.GRAY + "-" + ChatColor.YELLOW + " Activates specified message analyze places.");
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.YELLOW + "/" + label + " deactivate <message analyze places> " + ChatColor.GRAY + "-" + ChatColor.YELLOW + " Deactivates specified message analyze places.");
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.YELLOW + "/" + label + " deactivate-all " + ChatColor.GRAY + "-" + ChatColor.YELLOW + " Deactivates all message analyze places.");
        sender.sendMessage(this.plugin.getPrefix());
        this.sendAvailablePlacesToAnalyze(sender);
    }

    private void sendAvailablePlacesToAnalyze(CommandSender sender) {
        sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "Available message analyze places:");
        for (MessagePlace messageAnalyzePlace : MessagePlace.values()) {
            boolean isValidMinecraftVersion = MinecraftVersion.atOrAbove(messageAnalyzePlace.getMinimumRequiredMinecraftVersion());
            if (isValidMinecraftVersion) {
                sender.sendMessage(this.plugin.getPrefix() + ChatColor.GRAY + "- " + ChatColor.YELLOW + messageAnalyzePlace.name());
            }
        }
    }
}
