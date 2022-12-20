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

package dev.jaqobb.message_editor;

import com.comphenix.protocol.utility.MinecraftVersion;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public final class MessageEditorConstants {

    public static final String PREFIX = "&8[&6Message Editor&8] ";

    public static final String PLACEHOLDER_API_PLUGIN_NAME = "PlaceholderAPI";

    public static final int MESSAGE_LENGTH = 40;

    public static final Pattern CHAT_COLOR_PATTERN = Pattern.compile("(?i)" + ChatColor.COLOR_CHAR + "([0-9A-FK-ORX])");

    public static final String SPECIAL_REGEX_CHARACTERS = "[/<>{}()\\[\\],.+\\-*?^$\\\\|]";

    public static final MinecraftVersion WILD_UPDATE_3_VERSION = new MinecraftVersion("1.19.3");

    private MessageEditorConstants() {
        throw new UnsupportedOperationException("Cannot create instance of this class");
    }
}
