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

package dev.jaqobb.messageeditor.util;

import dev.jaqobb.messageeditor.data.MessagePlace;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public final class MessageUtils {

    private static final char[] CHARACTERS = {'q', 'Q', 'w', 'W', 'e', 'E', 'r', 'R', 't', 'T', 'y', 'Y', 'u', 'U', 'i', 'I', 'o', 'O', 'p', 'P', 'a', 'A', 's', 'S', 'd', 'D', 'f', 'F', 'g', 'G', 'h', 'H', 'j', 'J', 'k', 'K', 'l', 'L', 'z', 'Z', 'x', 'X', 'c', 'C', 'v', 'V', 'b', 'B', 'n', 'N', 'm', 'M', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

    private static final BaseComponent[] EMPTY_BASE_COMPONENT_ARRAY = new BaseComponent[0];

    private MessageUtils() {
        throw new UnsupportedOperationException("Cannot create instance of this class");
    }

    public static String composeMessageId(
        final MessagePlace messagePlace,
        final String message
    ) {
        String messagePlaceId = messagePlace.getId();
        String messageHashCode = message.hashCode() < 0 ? String.valueOf(-message.hashCode() * 2L) : String.valueOf(message.hashCode());
        String messageId = "";
        for (int messageHashCodeIndex = 0; messageHashCodeIndex < messageHashCode.length(); messageHashCodeIndex++) {
            if (messageHashCodeIndex + 1 < messageHashCode.length()) {
                String messageHashCodeNumberFirst = String.valueOf(messageHashCode.charAt(messageHashCodeIndex));
                String messageHashCodeNumberSecond = String.valueOf(messageHashCode.charAt(messageHashCodeIndex + 1));
                int messageHashCodeNumber = Integer.parseInt(messageHashCodeNumberFirst + messageHashCodeNumberSecond);
                if (messageHashCodeNumber < CHARACTERS.length) {
                    messageId += CHARACTERS[messageHashCodeNumber];
                } else {
                    messageId += CHARACTERS[Integer.parseInt(messageHashCodeNumberFirst)];
                    messageId += CHARACTERS[Integer.parseInt(messageHashCodeNumberSecond)];
                }
                messageHashCodeIndex++;
            } else {
                messageId += CHARACTERS[Integer.parseInt(String.valueOf(messageHashCode.charAt(messageHashCodeIndex)))];
            }
        }
        return messagePlaceId + messageId;
    }

    public static String getLastColors(final String message) {
        int messageLength = message.length();
        String result = "";
        for (int index = messageLength - 1; index > -1; index--) {
            char section = message.charAt(index);
            if (section == ChatColor.COLOR_CHAR && index < messageLength - 1) {
                char character = message.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(character);
                if (color != null) {
                    result = color + result;
                    if (color == ChatColor.RESET || (color != ChatColor.MAGIC && color != ChatColor.BOLD && color != ChatColor.STRIKETHROUGH && color != ChatColor.UNDERLINE && color != ChatColor.ITALIC)) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static BaseComponent[] toBaseComponents(
        final String message
    ) {
        List<BaseComponent> messageComponents = new ArrayList<>(10);
        String messagePart = "";
        for (int messageIndex = 0; messageIndex < message.length(); messageIndex++) {
            boolean makeMessageComponent = false;
            String messagePartNew = "";
            char messageCharacter = message.charAt(messageIndex);
            if (messageIndex == message.length() - 1) {
                makeMessageComponent = true;
                messagePart += messageCharacter;
            } else if (messageCharacter != '§') {
                messagePart += messageCharacter;
            } else {
                ChatColor color = ChatColor.getByChar(message.charAt(messageIndex + 1));
                if (color != null) {
                    messageIndex++;
                    if (messagePart.isEmpty() || (color == ChatColor.MAGIC || color == ChatColor.BOLD || color == ChatColor.STRIKETHROUGH || color == ChatColor.UNDERLINE || color == ChatColor.ITALIC || color == ChatColor.RESET)) {
                        messagePart += color.toString();
                    } else {
                        makeMessageComponent = true;
                        messagePartNew += color.toString();
                    }
                } else {
                    messagePart += messageCharacter;
                }
            }
            if (makeMessageComponent) {
                messageComponents.add(new TextComponent(messagePart));
                messagePart = messagePartNew;
            }
        }
        return messageComponents.toArray(EMPTY_BASE_COMPONENT_ARRAY);
    }

    // Using ComponentSerializer#toString when the amount of components is greater than 1
    // wraps the message into TextComponent and thus can break plugins where the index
    // of a message component is important.
    public static String toJson(
        final BaseComponent[] messageComponents,
        final boolean wrapIntoTextComponent
    ) {
        if (messageComponents.length == 1) {
            return ComponentSerializer.toString(messageComponents[0]);
        } else if (wrapIntoTextComponent) {
            return ComponentSerializer.toString(messageComponents);
        } else {
            StringJoiner messageComponentsJson = new StringJoiner(",", "[", "]");
            for (BaseComponent messageComponent : messageComponents) {
                messageComponentsJson.add(ComponentSerializer.toString(messageComponent));
            }
            return messageComponentsJson.toString();
        }
    }
}
