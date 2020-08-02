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

package dev.jaqobb.messageeditor.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dev.jaqobb.messageeditor.data.MessageAnalyzePlace;
import dev.jaqobb.messageeditor.data.MessageEdit;
import dev.jaqobb.messageeditor.MessageEditorPlugin;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

public final class MessageEditorPacketListener extends PacketAdapter {

	private static final String SPECIAL_REGEX_CHARACTERS = "[{}()\\[\\].+*?^$\\\\|]";

	@SuppressWarnings("deprecation")
	private static final HoverEvent COPY_TO_CLIPBOARD_HOVER_EVENT = new HoverEvent(
		HoverEvent.Action.SHOW_TEXT,
		TextComponent.fromLegacyText(ChatColor.GRAY + "Click to copy this message's JSON to your clipboard.")
	);

	public MessageEditorPacketListener(MessageEditorPlugin plugin) {
		super(
			plugin,
			ListenerPriority.HIGHEST,
			PacketType.Login.Server.DISCONNECT,
			PacketType.Play.Server.KICK_DISCONNECT,
			PacketType.Play.Server.CHAT
		);
	}

	@Override
	public MessageEditorPlugin getPlugin() {
		return (MessageEditorPlugin) super.getPlugin();
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		PacketContainer oldPacket = event.getPacket();
		PacketContainer newPacket = this.copyPacketContent(oldPacket, ProtocolLibrary.getProtocolManager().createPacket(oldPacket.getType()));
		WrappedChatComponent message = newPacket.getChatComponents().read(0);
		// TODO: Pretty sure 'message' should not be null at any point. I.e. /plugins command (and probably more) causes 'message' to be null for some reason.
		if (message == null) {
			return;
		}
		String cachedMessage = this.getPlugin().getCachedMessage(message.getJson());
		MessageEdit messageEdit = null;
		Matcher messageEditMatcher = null;
		if (cachedMessage == null) {
			for (MessageEdit currentMessageEdit : this.getPlugin().getMessageEdits()) {
				Matcher currentMessageEditMatcher = currentMessageEdit.getMatcher(message.getJson());
				if (currentMessageEditMatcher != null) {
					messageEdit = currentMessageEdit;
					messageEditMatcher = currentMessageEditMatcher;
					break;
				}
			}
		}
		if (cachedMessage != null || (messageEdit != null && messageEditMatcher != null)) {
			if (cachedMessage != null) {
				message.setJson(cachedMessage);
			} else {
				String messageAfter = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
				if (this.getPlugin().isPlaceholderApiPresent()) {
					messageAfter = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, messageAfter);
				}
				if (this.getPlugin().isMvdwPlaceholderApiPresent()) {
					messageAfter = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, messageAfter);
				}
				this.getPlugin().cacheMessage(message.getJson(), messageAfter);
				message.setJson(messageAfter);
			}
		}
		MessageAnalyzePlace messageAnalyzePlace;
		if (newPacket.getType() == PacketType.Play.Server.CHAT) {
			// 0 and 1 - chat
			// 2 - action bar
			Byte position = newPacket.getBytes().readSafely(0);
			if (position == null) {
				position = newPacket.getChatTypes().read(0).getId();
			}
			messageAnalyzePlace = MessageAnalyzePlace.fromPacketType(newPacket.getType(), position);
		} else {
			messageAnalyzePlace = MessageAnalyzePlace.fromPacketType(newPacket.getType());
		}
		if (messageAnalyzePlace != null && this.getPlugin().isMessageAnalyzePlaceActive(messageAnalyzePlace)) {
			String messageClear = "";
			for (BaseComponent component : ComponentSerializer.parse(message.getJson())) {
				messageClear += component.toPlainText();
			}
			String messageJson = message.getJson().replaceAll(SPECIAL_REGEX_CHARACTERS, "\\\\$0");
			this.logPacketContent(messageAnalyzePlace, player, messageClear, messageJson);
		}
		if (newPacket.getType() == PacketType.Play.Server.CHAT) {
			// 0 and 1 - chat
			// 2 - action bar
			Byte position = newPacket.getBytes().readSafely(0);
			if (position == null) {
				position = newPacket.getChatTypes().read(0).getId();
			}
			if (position != 2 && player.hasPermission("messageeditor.use") && this.getPlugin().isAttachingSpecialHoverAndClickEventsEnabled()) {
				TextComponent messageToSend = new TextComponent(ComponentSerializer.parse(message.getJson()));
				messageToSend.setHoverEvent(COPY_TO_CLIPBOARD_HOVER_EVENT);
				messageToSend.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, message.getJson().replaceAll(SPECIAL_REGEX_CHARACTERS, "\\\\$0")));
				message.setJson(ComponentSerializer.toString(messageToSend));
			}
		}
		newPacket.getChatComponents().write(0, message);
		event.setPacket(newPacket);
	}

	private PacketContainer copyPacketContent(PacketContainer oldPacket, PacketContainer newPacket) {
		newPacket.getChatComponents().write(0, oldPacket.getChatComponents().read(0));
		if (newPacket.getType() == PacketType.Play.Server.CHAT) {
			Byte position = oldPacket.getBytes().readSafely(0);
			if (position == null) {
				position = oldPacket.getChatTypes().read(0).getId();
			}
			newPacket.getBytes().writeSafely(0, position);
			if (EnumWrappers.getChatTypeClass() != null) {
				Byte finalPosition = position;
				Arrays.stream(EnumWrappers.ChatType.values())
					.filter(type -> type.getId() == finalPosition)
					.findAny()
					.ifPresent(type -> newPacket.getChatTypes().writeSafely(0, type));
			}
		}
		return newPacket;
	}

	private void logPacketContent(MessageAnalyzePlace messageAnalyzePlace, Player receiver, String messageClear, String messageJson) {
		this.getPlugin().getLogger().log(Level.INFO, "Place: " + messageAnalyzePlace.name() + ".");
		this.getPlugin().getLogger().log(Level.INFO, "Receiver: " + receiver.getName() + ".");
		this.getPlugin().getLogger().log(Level.INFO, "Message clear: " + messageClear);
		this.getPlugin().getLogger().log(Level.INFO, "Message JSON: " + messageJson);
	}
}
