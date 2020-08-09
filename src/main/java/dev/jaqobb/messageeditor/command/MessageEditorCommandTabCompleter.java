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

import dev.jaqobb.messageeditor.MessageEditorPlugin;
import dev.jaqobb.messageeditor.data.MessageAnalyzePlace;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class MessageEditorCommandTabCompleter implements TabCompleter {

	private final MessageEditorPlugin plugin;

	public MessageEditorCommandTabCompleter(MessageEditorPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] arguments) {
		if (!sender.hasPermission("messageeditor.use")) {
			return null;
		}
		List<String> completion = new LinkedList<>();
		if (arguments.length == 1) {
			if ("activate".startsWith(arguments[0].toLowerCase())) {
				completion.add("activate");
			}
			if ("deactivate".startsWith(arguments[0].toLowerCase())) {
				completion.add("deactivate");
			}
			if ("deactivate-all".startsWith(arguments[0].toLowerCase())) {
				completion.add("deactivate-all");
			}
		}
		if (arguments.length > 1 && (arguments[0].equalsIgnoreCase("activate") || (arguments[0].equalsIgnoreCase("deactivate")))) {
			for (int index = 1; index < arguments.length; index++) {
				for (MessageAnalyzePlace messageAnalyzePlace : MessageAnalyzePlace.values()) {
					boolean messageAnalyzePlaceActive = this.plugin.isMessageAnalyzePlaceActive(messageAnalyzePlace);
					boolean canMessageAnalyzePlaceActiveStateBeModified = arguments[0].equalsIgnoreCase("activate") != messageAnalyzePlaceActive;
					if (canMessageAnalyzePlaceActiveStateBeModified && messageAnalyzePlace.name().startsWith(arguments[index].toUpperCase())) {
						completion.add(messageAnalyzePlace.name());
					}
				}
			}
		}
		return completion;
	}
}
