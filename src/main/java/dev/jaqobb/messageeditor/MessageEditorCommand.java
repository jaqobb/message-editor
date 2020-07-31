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

package dev.jaqobb.messageeditor;

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
			sender.sendMessage(ChatColor.RED + "[Message Editor] You do not have required permissions to do that.");
			return true;
		}
		if (arguments.length == 0) {
			this.sendHelpMessage(sender, label);
			return true;
		}
		if (arguments[0].equalsIgnoreCase("add-places")) {
			if (arguments.length < 1) {
				sender.sendMessage(ChatColor.RED + "[Message Editor] Correct usage: /" + label + " add-places <places>.");
				this.sendAvailablePlacesToAnalyze(sender);
				return true;
			}
			int places = 0;
			for (int index = 1; index < arguments.length; index++) {
				try {
					MessageAnalyzePlace place = MessageAnalyzePlace.fromName(arguments[index]);
					if (!this.plugin.isPlaceToAnalyze(place)) {
						this.plugin.addPlaceToAnalyze(place);
						places++;
					} else {
						sender.sendMessage(ChatColor.RED + "[Message Editor] " + place.name() + " is already added to places to analyze.");
					}
				} catch (IllegalArgumentException exception) {
					sender.sendMessage(ChatColor.RED + "[Message Editor] Could not convert '" + arguments[index] + "' to place to analyze.");
				}
			}
			sender.sendMessage(ChatColor.GREEN + "[Message Editor] You have added " + places + " place(s) to analyze.");
			return true;
		}
		if (arguments[0].equalsIgnoreCase("remove-places")) {
			if (arguments.length < 1) {
				sender.sendMessage(ChatColor.RED + "[Message Editor] Correct usage: /" + label + " remove-places <places>.");
				this.sendAvailablePlacesToAnalyze(sender);
				return true;
			}
			int places = 0;
			for (int index = 1; index < arguments.length; index++) {
				try {
					MessageAnalyzePlace place = MessageAnalyzePlace.fromName(arguments[index]);
					if (this.plugin.isPlaceToAnalyze(place)) {
						this.plugin.removePlaceToAnalyze(place);
						places++;
					} else {
						sender.sendMessage(ChatColor.RED + "[Message Editor] " + place.name() + " is not added to places to analyze.");
					}
				} catch (IllegalArgumentException exception) {
					sender.sendMessage(ChatColor.RED + "[Message Editor] Could not convert '" + arguments[index] + "' to place to analyze.");
				}
			}
			sender.sendMessage(ChatColor.GREEN + "[Message Editor] You have removed " + places + " place(s) to analyze.");
			return true;
		}
		if (arguments[0].equalsIgnoreCase("clear-places")) {
			this.plugin.clearPlacesToAnalyze();
			sender.sendMessage(ChatColor.GREEN + "[Message Editor] You have removed all places to analyze.");
			return true;
		}
		this.sendHelpMessage(sender, label);
		return true;
	}

	private void sendHelpMessage(CommandSender sender, String label) {
		sender.sendMessage(ChatColor.GREEN + "[Message Editor] Available commands:");
		sender.sendMessage(ChatColor.GREEN + "[Message Editor] - /" + label + " add-places <places> - Adds places to analyze.");
		sender.sendMessage(ChatColor.GREEN + "[Message Editor] - /" + label + " remove-places <places> - Removes places to analyze.");
		sender.sendMessage(ChatColor.GREEN + "[Message Editor] - /" + label + " clear-places - Removes all places to analyze.");
		this.sendAvailablePlacesToAnalyze(sender);
	}

	private void sendAvailablePlacesToAnalyze(CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "[Message Editor] Available places to analyze:");
		for (MessageAnalyzePlace place : MessageAnalyzePlace.values()) {
			sender.sendMessage(ChatColor.GREEN + "[Message Editor] - " + place.name());
		}
	}
}
