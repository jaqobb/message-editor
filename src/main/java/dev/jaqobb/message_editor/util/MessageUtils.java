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

package dev.jaqobb.message_editor.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.jaqobb.message_editor.MessageEditorConstants;
import dev.jaqobb.message_editor.message.MessagePlace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

public final class MessageUtils {

    private static final char[] CHARACTERS = {'q', 'Q', 'w', 'W', 'e', 'E', 'r', 'R', 't', 'T', 'y', 'Y', 'u', 'U', 'i', 'I', 'o', 'O', 'p', 'P', 'a', 'A', 's', 'S', 'd', 'D', 'f', 'F', 'g', 'G', 'h', 'H', 'j', 'J', 'k', 'K', 'l', 'L', 'z', 'Z', 'x', 'X', 'c', 'C', 'v', 'V', 'b', 'B', 'n', 'N', 'm', 'M', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

    private static final BaseComponent[] EMPTY_BASE_COMPONENT_ARRAY = new BaseComponent[0];
    private static final boolean HEX_COLORS_SUPPORTED;

    static {
        boolean hexColorsSupported;
        try {
            ChatColor.class.getDeclaredMethod("of", String.class);
            hexColorsSupported = true;
        } catch (NoSuchMethodException exception) {
            hexColorsSupported = false;
        }
        HEX_COLORS_SUPPORTED = hexColorsSupported;
    }

    private MessageUtils() {
        throw new UnsupportedOperationException("Cannot create instance of this class");
    }

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String translateWithPrefix(String message) {
        return translate(MessageEditorConstants.PREFIX + message);
    }

    public static String generateId(MessagePlace place, String message) {
        String placeId = place.getId();
        String messageHash = message.hashCode() < 0 ? String.valueOf(-message.hashCode() * 2L) : String.valueOf(message.hashCode());
        String messageId = "";
        for (int i = 0; i < messageHash.length(); i++) {
            if (i + 1 < messageHash.length()) {
                String currentNumber = String.valueOf(messageHash.charAt(i));
                String nextNumber = String.valueOf(messageHash.charAt(i + 1));
                int number = Integer.parseInt(currentNumber + nextNumber);
                if (number < CHARACTERS.length) {
                    messageId += CHARACTERS[number];
                } else {
                    messageId += CHARACTERS[Integer.parseInt(currentNumber)];
                    messageId += CHARACTERS[Integer.parseInt(nextNumber)];
                }
                i++;
            } else {
                messageId += CHARACTERS[Integer.parseInt(String.valueOf(messageHash.charAt(i)))];
            }
        }
        return placeId + messageId;
    }

    public static String getLastColors(String message) {
        int length = message.length();
        String colors = "";
        for (int i = length - 1; i > -1; i--) {
            char section = message.charAt(i);
            if (section == ChatColor.COLOR_CHAR && i < length - 1) {
                char character = message.charAt(i + 1);
                if (i - 12 >= 0) {
                    char hexColorSection = message.charAt(i - 12);
                    if (hexColorSection == ChatColor.COLOR_CHAR) {
                        char hexColorCharacter = message.charAt(i - 11);
                        if ((hexColorCharacter == 'x' || hexColorCharacter == 'X') && HEX_COLORS_SUPPORTED) {
                            String hexColor = "";
                            for (int j = -9; j <= 1; j += 2) {
                                hexColor += message.charAt(i + j);
                            }
                            try {
                                i -= 13;
                                colors = ChatColor.of("#" + hexColor) + colors;
                                continue;
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
                ChatColor color = ChatColor.getByChar(character);
                if (color != null) {
                    i--;
                    colors = color + colors;
                    if (color == ChatColor.RESET || (color != ChatColor.MAGIC && color != ChatColor.BOLD && color != ChatColor.STRIKETHROUGH && color != ChatColor.UNDERLINE && color != ChatColor.ITALIC)) {
                        break;
                    }
                }
            }
        }
        return colors;
    }

    public static BaseComponent[] toBaseComponents(String message) {
        List<BaseComponent> components = new ArrayList<>(10);
        String messagePart = "";
        for (int i = 0; i < message.length(); i++) {
            boolean makeComponent = false;
            String newMessagePart = "";
            char character = message.charAt(i);
            if (i == message.length() - 1) {
                makeComponent = true;
                messagePart += character;
            } else if (character != "§".charAt(0)) {
                messagePart += character;
            } else {
                char hexColorCharacter = message.charAt(i + 1);
                if ((hexColorCharacter == 'x' || hexColorCharacter == 'X') && HEX_COLORS_SUPPORTED) {
                    String hexColor = "";
                    for (int j = 3; j <= 13; j += 2) {
                        hexColor += message.charAt(i + j);
                    }
                    try {
                        i += 13;
                        messagePart += ChatColor.of("#" + hexColor);
                        continue;
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                ChatColor color = ChatColor.getByChar(message.charAt(i + 1));
                if (color != null) {
                    i++;
                    if (messagePart.isEmpty() || (color == ChatColor.MAGIC || color == ChatColor.BOLD || color == ChatColor.STRIKETHROUGH || color == ChatColor.UNDERLINE || color == ChatColor.ITALIC || color == ChatColor.RESET)) {
                        messagePart += color.toString();
                    } else {
                        makeComponent = true;
                        newMessagePart += color.toString();
                    }
                } else {
                    messagePart += character;
                }
            }
            if (makeComponent) {
                components.addAll(Arrays.asList(TextComponent.fromLegacyText(messagePart)));
                messagePart = newMessagePart;
            }
        }
        return components.toArray(EMPTY_BASE_COMPONENT_ARRAY);
    }

    // Using ComponentSerializer#toString when the amount of components is greater than 1
    // wraps the message into TextComponent and thus can break plugins where the index
    // of a message component is important.
    public static String toJson(BaseComponent[] components, boolean wrapIntoTextComponent) {
        if (components.length == 1) {
            return ComponentSerializer.toString(components[0]);
        } else if (wrapIntoTextComponent) {
            return ComponentSerializer.toString(components);
        } else {
            StringJoiner componentsJson = new StringJoiner(",", "[", "]");
            for (BaseComponent messageComponent : components) {
                componentsJson.add(ComponentSerializer.toString(messageComponent));
            }
            return componentsJson.toString();
        }
    }

    public static boolean isJson(String string) {
        try {
            JsonParser.parseString(string);
            return true;
        } catch (JsonSyntaxException exception) {
            return false;
        }
    }

    public static void logMessage(
        Logger logger,
        MessagePlace place,
        Player player,
        String messageId,
        boolean json,
        String message
    ) {
        logger.log(Level.INFO, "Place: " + place.getFriendlyName() + " (" + place.name() + ")");
        logger.log(Level.INFO, "Player: " + player.getName());
        if (json) {
            String messageReplaced = message.replaceAll(MessageEditorConstants.SPECIAL_REGEX_CHARACTERS, "\\\\$0");
            String messageClear = BaseComponent.toLegacyText(ComponentSerializer.parse(message));
            logger.log(Level.INFO, "Message JSON: '" + messageReplaced + "'");
            logger.log(Level.INFO, "Message clear: '" + messageClear + "'");
        } else {
            Matcher matcher = MessageEditorConstants.CHAT_COLOR_PATTERN.matcher(message);
            String messageSuffix = matcher.find() ? " (replace & -> § (section sign) in colors)" : "";
            logger.log(Level.INFO, "Message: '" + matcher.replaceAll("&$1").replace("\\", "\\\\") + "'" + messageSuffix);
            logger.log(Level.INFO, "Message clear: '" + matcher.replaceAll("") + "'");
        }
        logger.log(Level.INFO, "Message ID: '" + messageId + "'");
    }

    public static String retrieveMessage(PacketContainer packet, PacketType simulatedPacketType) {
        if (simulatedPacketType == PacketType.Play.Server.CHAT) {
            WrappedChatComponent message = packet.getChatComponents().read(0);
            if (message != null) {
                return message.getJson();
            } else if (packet.getSpecificModifier(BaseComponent[].class).size() == 1) {
                BaseComponent[] messageComponents = packet.getSpecificModifier(BaseComponent[].class).read(0);
                if (messageComponents != null) {
                    return toJson(messageComponents, false);
                }
            }
        } else if (simulatedPacketType == PacketType.Play.Server.SYSTEM_CHAT) {
            return packet.getStrings().read(0);
        }
        return null;
    }

    public static void updateMessage(PacketContainer packet, PacketType simulatedPacketType, String message, boolean json) {
        if (simulatedPacketType == PacketType.Play.Server.CHAT) {
            if (packet.getChatComponents().read(0) != null) {
                if (json) {
                    packet.getChatComponents().write(0, WrappedChatComponent.fromJson(message));
                } else {
                    packet.getChatComponents().write(0, WrappedChatComponent.fromJson(toJson(toBaseComponents(message), true)));
                }
            } else if (packet.getSpecificModifier(BaseComponent[].class).size() == 1) {
                if (json) {
                    packet.getSpecificModifier(BaseComponent[].class).write(0, ComponentSerializer.parse(message));
                } else {
                    packet.getSpecificModifier(BaseComponent[].class).write(0, toBaseComponents(message));
                }
            }
        } else if (simulatedPacketType == PacketType.Play.Server.SYSTEM_CHAT) {
            if (json) {
                packet.getStrings().write(0, message);
            } else {
                packet.getStrings().write(0, toJson(toBaseComponents(message), true));
            }
        }
    }
}
