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

package dev.jaqobb.message_editor.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.jaqobb.message_editor.MessageEditorConstants;
import dev.jaqobb.message_editor.message.MessagePlace;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

public final class MessageUtils {

    public static final int MESSAGE_LENGTH = 40;

    private static final Random ID_NUMBER_GENERATOR = new SecureRandom();
    private static final char[] ID_CHARACTERS = "_-0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();
    private static final int ID_LENGTH = 8;
    @SuppressWarnings("UnnecessaryExplicitNumericCast")
    // Explicit cast is actually necessary.
    private static final int ID_MASK = (2 << (int) Math.floor(StrictMath.log(ID_CHARACTERS.length - 1) / StrictMath.log(2))) - 1;
    private static final int ID_STEP = (int) Math.ceil(1.6D * ID_MASK * ID_LENGTH / ID_CHARACTERS.length);

    private static final boolean HEX_COLORS_SUPPORTED;
    private static final boolean ADVENTURE_PRESENT;

    static {
        HEX_COLORS_SUPPORTED = methodExists(ChatColor.class, "of", String.class);
        ADVENTURE_PRESENT = classExists("net.kyori.adventure.Adventure");
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

    public static List<String> splitMessage(String message, boolean json) {
        List<String> result = new ArrayList<>();
        for (String messageData : message.split(json ? "\\n" : "\\\\n")) {
            String[] messageDataChunk = messageData.split(" ");
            String messageChunk = "";
            for (int index = 0; index < messageDataChunk.length; index += 1) {
                if (index > 0 && index < messageDataChunk.length && !messageChunk.isEmpty()) {
                    messageChunk += " ";
                }
                messageChunk += messageDataChunk[index];
                if (index == messageDataChunk.length - 1 || messageChunk.length() >= MESSAGE_LENGTH) {
                    if (result.isEmpty()) {
                        result.add(messageChunk);
                    } else {
                        result.add(getLastColors(result.get(result.size() - 1)) + messageChunk);
                    }
                    messageChunk = "";
                }
            }
        }
        return result;
    }

    // https://github.com/aventrix/jnanoid/blob/develop/src/main/java/com/aventrix/jnanoid/jnanoid/NanoIdUtils.java
    public static String generateId(MessagePlace place) {
        StringBuilder idBuilder = new StringBuilder(place.getId());
        while (true) {
            byte[] bytes = new byte[ID_STEP];
            ID_NUMBER_GENERATOR.nextBytes(bytes);
            for (int index = 0; index < ID_STEP; index++) {
                int characterIndex = bytes[index] & ID_MASK;
                if (characterIndex < ID_CHARACTERS.length) {
                    idBuilder.append(ID_CHARACTERS[characterIndex]);
                    if (idBuilder.length() == ID_LENGTH) {
                        return idBuilder.toString();
                    }
                }
            }
        }
    }

    public static String getLastColors(String message) {
        int length = message.length();
        String colors = "";
        for (int index = length - 1; index > -1; index -= 1) {
            char section = message.charAt(index);
            if (section == ChatColor.COLOR_CHAR && index < length - 1) {
                char character = message.charAt(index + 1);
                if (index - 12 >= 0) {
                    char hexColorSection = message.charAt(index - 12);
                    if (hexColorSection == ChatColor.COLOR_CHAR) {
                        char hexColorCharacter = message.charAt(index - 11);
                        if ((hexColorCharacter == 'x' || hexColorCharacter == 'X') && HEX_COLORS_SUPPORTED) {
                            String hexColor = "";
                            for (int j = -9; j <= 1; j += 2) {
                                hexColor += message.charAt(index + j);
                            }
                            try {
                                index -= 13;
                                colors = ChatColor.of("#" + hexColor) + colors;
                                continue;
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
                ChatColor color = ChatColor.getByChar(character);
                if (color != null) {
                    index -= 1;
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
        TextComponent finalComponent = new TextComponent();
        TextComponent component = new TextComponent();
        ChatColor componentNewColor = null;
        boolean firstColor = true;
        for (int index = 0; index < message.length(); index += 1) {
            boolean makeComponent = false;
            char character = message.charAt(index);
            if (index == message.length() - 1) {
                makeComponent = true;
                component.setText(component.getText() + character);
            } else if (character != '§') {
                component.setText(component.getText() + character);
            } else {
                char hexColorCharacter = message.charAt(index + 1);
                if ((hexColorCharacter == 'x' || hexColorCharacter == 'X') && HEX_COLORS_SUPPORTED) {
                    String hexColor = "";
                    for (int j = 3; j <= 13; j += 2) {
                        hexColor += message.charAt(index + j);
                    }
                    try {
                        index += 13;
                        component.setColor(ChatColor.of("#" + hexColor));
                        firstColor = false;
                        continue;
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                ChatColor color = ChatColor.getByChar(message.charAt(index + 1));
                if (color != null) {
                    index += 1;
                    if ((color != component.getColor() && firstColor) || color == ChatColor.MAGIC || color == ChatColor.BOLD || color == ChatColor.STRIKETHROUGH || color == ChatColor.UNDERLINE || color == ChatColor.ITALIC) {
                        if (color == ChatColor.MAGIC) {
                            component.setObfuscated(true);
                        } else if (color == ChatColor.BOLD) {
                            component.setBold(true);
                        } else if (color == ChatColor.STRIKETHROUGH) {
                            component.setStrikethrough(true);
                        } else if (color == ChatColor.UNDERLINE) {
                            component.setUnderlined(true);
                        } else if (color == ChatColor.ITALIC) {
                            component.setItalic(true);
                        } else {
                            component.setColor(color);
                            firstColor = false;
                        }
                    } else {
                        makeComponent = true;
                        componentNewColor = color;
                    }
                } else {
                    component.setText(component.getText() + character);
                }
            }
            if (makeComponent) {
                finalComponent.addExtra(component);
                component = new TextComponent();
                if (componentNewColor != null) {
                    component.setColor(componentNewColor);
                    componentNewColor = null;
                    firstColor = false;
                } else {
                    firstColor = true;
                }
            }
        }
        return new BaseComponent[] {finalComponent};
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
            StringJoiner json = new StringJoiner(",", "[", "]");
            for (BaseComponent component : components) {
                json.add(ComponentSerializer.toString(component));
            }
            return json.toString();
        }
    }

    public static boolean isJson(String string) {
        if (string == null || string.trim().isEmpty()) {
            return false;
        }
        try {
            // Streams is being used instead of JsonParser
            // because JsonParser parses the string in lenient mode
            // which we don't want.
            Streams.parse(new JsonReader(new StringReader(string)));
            return true;
        } catch (JsonParseException exception) {
            return false;
        }
    }

    public static void logMessage(Logger logger, MessagePlace place, Player player, String messageId, boolean json, String message) {
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
            WrappedChatComponent message = packet.getChatComponents().readSafely(0);
            if (message != null) {
                return message.getJson();
            } else if (packet.getSpecificModifier(BaseComponent[].class).size() == 1) {
                BaseComponent[] messageComponents = packet.getSpecificModifier(BaseComponent[].class).readSafely(0);
                if (messageComponents != null) {
                    return toJson(messageComponents, false);
                }
            }
        } else if (simulatedPacketType == PacketType.Play.Server.SYSTEM_CHAT) {
            if (ADVENTURE_PRESENT) {
                // Additional check for safety in case adventure is detected but packets do not use it.
                // If it is not used, fall back to the general method.
                Component component = packet.getSpecificModifier(Component.class).readSafely(0);
                if (component != null) {
                    return GsonComponentSerializer.gson().serialize(component);
                }
            }
            return packet.getStrings().readSafely(0);
        }
        return null;
    }

    public static void updateMessage(PacketContainer packet, PacketType simulatedPacketType, String message, boolean json) {
        if (simulatedPacketType == PacketType.Play.Server.CHAT) {
            if (packet.getChatComponents().readSafely(0) != null) {
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
            if (ADVENTURE_PRESENT) {
                // Additional check for safety in case adventure is detected but packets do not use it.
                // If it is not used, fall back to the general method.
                if (packet.getSpecificModifier(Component.class).readSafely(0) != null) {
                    packet.getSpecificModifier(Component.class).write(0, GsonComponentSerializer.gson().deserialize(message));
                    return;
                }
            }
            if (json) {
                packet.getStrings().write(0, message);
            } else {
                packet.getStrings().write(0, toJson(toBaseComponents(message), true));
            }
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    private static boolean methodExists(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            clazz.getDeclaredMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException exception) {
            return false;
        }
    }
}
