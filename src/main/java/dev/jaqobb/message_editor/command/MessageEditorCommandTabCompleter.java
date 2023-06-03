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

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import dev.jaqobb.message_editor.message.MessagePlace;

public final class MessageEditorCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] arguments) {
        if (!sender.hasPermission("messageeditor.use")) {
            return null;
        }
        List<String> completions = new LinkedList<>();
        if (arguments.length == 1) {
            String argument = arguments[0].toLowerCase();
            if ("reload".startsWith(argument)) {
                completions.add("reload");
            }
            if ("edit".startsWith(argument)) {
                completions.add("edit");
            }
            if ("activate".startsWith(argument)) {
                completions.add("activate");
            }
            if ("deactivate".startsWith(argument)) {
                completions.add("deactivate");
            }
            if ("deactivate-all".startsWith(argument)) {
                completions.add("deactivate-all");
            }
            if ("deactivateall".startsWith(argument)) {
                completions.add("deactivateall");
            }
            if ("migrate".startsWith(argument)) {
                completions.add("migrate");
            }
        }
        if (arguments.length > 1 && (arguments[0].equalsIgnoreCase("activate") || (arguments[0].equalsIgnoreCase("deactivate")))) {
            for (int i = 1; i < arguments.length; i += 1) {
                for (MessagePlace place : MessagePlace.VALUES) {
                    if (!place.name().startsWith(arguments[i].toUpperCase())) {
                        continue;
                    }
                    if (!place.isSupported()) {
                        continue;
                    }
                    boolean correctState = arguments[0].equalsIgnoreCase("activate") != place.isAnalyzing();
                    if (correctState) {
                        completions.add(place.name());
                    }
                }
            }
        }
        return completions;
    }
}
