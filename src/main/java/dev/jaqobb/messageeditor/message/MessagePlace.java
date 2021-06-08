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

package dev.jaqobb.messageeditor.message;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import dev.jaqobb.messageeditor.util.MessageUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public enum MessagePlace {

    GAME_CHAT("GC", "Game Chat", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.CHAT, (byte) 0, EnumWrappers.ChatType.CHAT) {
        @Override
        public String getMessage(PacketContainer packet) {
            WrappedChatComponent message = packet.getChatComponents().read(0);
            if (message != null) {
                return message.getJson();
            } else if (packet.getSpecificModifier(BaseComponent[].class).size() == 1) {
                BaseComponent[] messageComponents = packet.getSpecificModifier(BaseComponent[].class).read(0);
                if (messageComponents != null) {
                    return MessageUtils.toJson(messageComponents, false);
                }
            }
            return null;
        }

        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            if (packet.getChatComponents().read(0) != null) {
                if (messageJson) {
                    packet.getChatComponents().write(0, WrappedChatComponent.fromJson(message));
                } else {
                    packet.getChatComponents().write(0, WrappedChatComponent.fromJson(MessageUtils.toJson(MessageUtils.toBaseComponents(message), true)));
                }
            } else if (packet.getSpecificModifier(BaseComponent[].class).size() == 1) {
                if (messageJson) {
                    packet.getSpecificModifier(BaseComponent[].class).write(0, ComponentSerializer.parse(message));
                } else {
                    packet.getSpecificModifier(BaseComponent[].class).write(0, MessageUtils.toBaseComponents(message));
                }
            }
        }
    },
    SYSTEM_CHAT("SC", "System Chat", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.CHAT, (byte) 1, EnumWrappers.ChatType.SYSTEM) {
        @Override
        public String getMessage(PacketContainer packet) {
            return GAME_CHAT.getMessage(packet);
        }

        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            GAME_CHAT.setMessage(packet, message, messageJson);
        }
    },
    ACTION_BAR("AB", "Action Bar", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.CHAT, (byte) 2, EnumWrappers.ChatType.GAME_INFO) {
        @Override
        public String getMessage(PacketContainer packet) {
            return GAME_CHAT.getMessage(packet);
        }

        @Override
        public void setMessage(
            PacketContainer packet,
            String message,
            boolean messageJson
        ) {
            GAME_CHAT.setMessage(packet, message, messageJson);
        }
    },
    KICK("K", "Kick", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.KICK_DISCONNECT) {
        @Override
        public String getMessage(PacketContainer packet) {
            return packet.getChatComponents().read(0).getJson();
        }

        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            if (messageJson) {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(message));
            } else {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(MessageUtils.toJson(MessageUtils.toBaseComponents(message), true)));
            }
        }
    },
    DISCONNECT("D", "Disconnect", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Login.Server.DISCONNECT) {
        @Override
        public String getMessage(PacketContainer packet) {
            return packet.getChatComponents().read(0).getJson();
        }

        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            if (messageJson) {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(message));
            } else {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(MessageUtils.toJson(MessageUtils.toBaseComponents(message), true)));
            }
        }
    },
    BOSS_BAR("BB", "Boss Bar", MinecraftVersion.COMBAT_UPDATE, PacketType.Play.Server.BOSS) {
        @Override
        public String getMessage(PacketContainer packet) {
            return packet.getChatComponents().read(0).getJson();
        }

        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            if (messageJson) {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(message));
            } else {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(MessageUtils.toJson(MessageUtils.toBaseComponents(message), true)));
            }
        }
    },
    SCOREBOARD_TITLE("ST", "Scoreboard Title", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
        @Override
        public String getMessage(PacketContainer packet) {
            if (packet.getStrings().size() == 2) {
                return packet.getStrings().read(1);
            }
            return packet.getChatComponents().read(0).getJson();
        }

        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            if (packet.getStrings().size() == 2) {
                packet.getStrings().write(1, message);
                return;
            }
            if (messageJson) {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(message));
            } else {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(MessageUtils.toJson(MessageUtils.toBaseComponents(message), true)));
            }
        }
    },
    SCOREBOARD_ENTRY("SE", "Scoreboard Entry", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.SCOREBOARD_SCORE) {
        @Override
        public String getMessage(PacketContainer packet) {
            return packet.getStrings().read(0);
        }

        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            if (messageJson) {
                packet.getStrings().write(0, BaseComponent.toLegacyText(ComponentSerializer.parse(message)));
            } else {
                packet.getStrings().write(0, message);
            }
        }
    },
    INVENTORY_TITLE("IT", "Inventory Title", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.OPEN_WINDOW) {
        @Override
        public String getMessage(PacketContainer packet) {
            return packet.getChatComponents().read(0).getJson();
        }

        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            if (messageJson) {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(message));
            } else {
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(MessageUtils.toJson(MessageUtils.toBaseComponents(message), true)));
            }
        }
    },
    INVENTORY_ITEM_NAME("ITN", "Inventory Item Name", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.WINDOW_ITEMS) {
        // Items are an exception and do not use this.
        @Override
        public String getMessage(PacketContainer packet) {
            throw new UnsupportedOperationException();
        }

        // Items are an exception and do not use this.
        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            throw new UnsupportedOperationException();
        }
    },
    INVENTORY_ITEM_LORE("ITL", "Inventory Item Lore", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.WINDOW_ITEMS) {
        // Items are an exception and do not use this.
        @Override
        public String getMessage(PacketContainer packet) {
            throw new UnsupportedOperationException();
        }

        // Items are an exception and do not use this.
        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            throw new UnsupportedOperationException();
        }
    },
    ENTITY_NAME("EN", "Entity Name", MinecraftVersion.BOUNTIFUL_UPDATE, PacketType.Play.Server.ENTITY_METADATA) {
        @Override
        public String getMessage(PacketContainer packet) {
            List<WrappedWatchableObject> watchableObjects = packet.getWatchableCollectionModifier().read(0);
            if (watchableObjects == null) {
                return null;
            }
            for (WrappedWatchableObject watchableObject : watchableObjects) {
                if (watchableObject.getIndex() != 2) {
                    continue;
                }
                Object value = watchableObject.getValue();
                if (!(value instanceof Optional)) {
                    continue;
                }
                Optional<?> nameOptional = (Optional<?>) value;
                if (nameOptional.isPresent()) {
                    return WrappedChatComponent.fromHandle(nameOptional.get()).getJson();
                }
            }
            return null;
        }

        @Override
        public void setMessage(PacketContainer packet, String message, boolean messageJson) {
            List<WrappedWatchableObject> watchableObjects = packet.getWatchableCollectionModifier().read(0);
            if (watchableObjects == null) {
                return;
            }
            for (WrappedWatchableObject watchableObject : watchableObjects) {
                if (watchableObject.getIndex() != 2) {
                    continue;
                }
                if (messageJson) {
                    watchableObject.setValue(Optional.of(WrappedChatComponent.fromJson(message).getHandle()));
                } else {
                    watchableObject.setValue(Optional.of(WrappedChatComponent.fromJson(MessageUtils.toJson(MessageUtils.toBaseComponents(message), true)).getHandle()));
                }
                return;
            }
        }
    };

    public static final MessagePlace[] VALUES = values();

    private final String                id;
    private final String                friendlyName;
    private final MinecraftVersion      minimumRequiredMinecraftVersion;
    private final PacketType            packetType;
    private final Byte                  chatType;
    private final EnumWrappers.ChatType chatTypeEnum;
    private final boolean               supported;
    private       boolean               analyzingActivated;

    private MessagePlace(
        String id,
        String friendlyName,
        MinecraftVersion minimumRequiredMinecraftVersion,
        PacketType packetType
    ) {
        this(id, friendlyName, minimumRequiredMinecraftVersion, packetType, null, null);
    }

    private MessagePlace(
        String id,
        String friendlyName,
        MinecraftVersion minimumRequiredMinecraftVersion,
        PacketType packetType,
        Byte chatType,
        EnumWrappers.ChatType chatTypeEnum
    ) {
        this.id                              = id;
        this.friendlyName                    = friendlyName;
        this.minimumRequiredMinecraftVersion = minimumRequiredMinecraftVersion;
        this.packetType                      = packetType;
        this.chatType                        = chatType;
        this.chatTypeEnum                    = chatTypeEnum;
        this.supported                       = MinecraftVersion.atOrAbove(this.minimumRequiredMinecraftVersion);
        this.analyzingActivated              = false;
    }

    public String getId() {
        return this.id;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public MinecraftVersion getMinimumRequiredMinecraftVersion() {
        return this.minimumRequiredMinecraftVersion;
    }

    public PacketType getPacketType() {
        return this.packetType;
    }

    public Byte getChatType() {
        return this.chatType;
    }

    public EnumWrappers.ChatType getChatTypeEnum() {
        return this.chatTypeEnum;
    }

    public boolean isSupported() {
        return this.supported;
    }

    public boolean isAnalyzingActivated() {
        return this.analyzingActivated;
    }

    public void setAnalyzingActivated(boolean activated) {
        this.analyzingActivated = activated;
    }

    public abstract String getMessage(PacketContainer packet);

    public abstract void setMessage(
        PacketContainer packet,
        String message,
        boolean messageJson
    );

    public static MessagePlace fromName(String name) {
        return Arrays.stream(VALUES)
            .filter(place -> place.name().equalsIgnoreCase(name) || place.friendlyName.equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public static MessagePlace fromPacket(PacketContainer packet) {
        if (packet.getType() != PacketType.Play.Server.CHAT) {
            return fromPacketType(packet.getType());
        }
        if (packet.getBytes().size() == 1) {
            return fromPacketType(packet.getType(), packet.getBytes().read(0));
        }
        return fromPacketType(packet.getType(), packet.getChatTypes().read(0));
    }

    public static MessagePlace fromPacketType(PacketType packetType) {
        return Arrays.stream(VALUES)
            .filter(place -> place.packetType == packetType)
            .findFirst()
            .orElse(null);
    }

    public static MessagePlace fromPacketType(PacketType packetType, byte chatType) {
        return Arrays.stream(VALUES)
            .filter(place -> place.packetType == packetType)
            .filter(place -> place.chatType != null && place.chatType == chatType)
            .findFirst()
            .orElse(null);
    }

    public static MessagePlace fromPacketType(PacketType packetType, EnumWrappers.ChatType chatTypeEnum) {
        return Arrays.stream(VALUES)
            .filter(place -> place.packetType == packetType)
            .filter(place -> place.chatTypeEnum != null && place.chatTypeEnum == chatTypeEnum)
            .findFirst()
            .orElse(null);
    }
}
