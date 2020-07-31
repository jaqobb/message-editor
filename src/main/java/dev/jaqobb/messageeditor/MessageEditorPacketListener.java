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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.util.regex.Matcher;
import net.md_5.bungee.api.ChatColor;
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
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();
		WrappedChatComponent message = packet.getChatComponents().read(0);
		String messageJson = message.getJson();
		String newMessage = this.getPlugin().getCachedMessage(messageJson);
		MessageEdit messageEdit = null;
		Matcher messageEditMatcher = null;
		if (newMessage == null) {
			for (MessageEdit currentMessageEdit : this.getPlugin().getMessageEdits()) {
				Matcher currentMessageEditMatcher = currentMessageEdit.getMatcher(messageJson);
				if (currentMessageEditMatcher != null) {
					messageEdit = currentMessageEdit;
					messageEditMatcher = currentMessageEditMatcher;
					break;
				}
			}
		}
		if (newMessage != null || (messageEdit != null && messageEditMatcher != null)) {
			if (newMessage != null) {
				packet.getChatComponents().write(0, WrappedChatComponent.fromJson(newMessage));
			} else {
				String messageAfter = messageEditMatcher.replaceAll(messageEdit.getMessageAfter());
				if (this.getPlugin().isPlaceholderApiPresent()) {
					messageAfter = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, messageAfter);
				}
				if (this.getPlugin().isMvdwPlaceholderApiPresent()) {
					messageAfter = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, messageAfter);
				}
				this.getPlugin().cacheMessage(messageJson, messageAfter);
				packet.getChatComponents().write(0, WrappedChatComponent.fromJson(messageAfter));
			}
		}
		message = packet.getChatComponents().read(0);
		messageJson = message.getJson();
		if (packet.getType() == PacketType.Play.Server.CHAT) {
			// 0 and 1 - chat
			// 2 - action bar
			Byte position = packet.getBytes().readSafely(0);
			if (position == null) {
				position = packet.getChatTypes().read(0).getId();
			}
			if (position != 2 && player.hasPermission("messageeditor.message.copy")) {
				TextComponent messageToSend = new TextComponent(ComponentSerializer.parse(messageJson));
				messageToSend.setHoverEvent(COPY_TO_CLIPBOARD_HOVER_EVENT);
				messageToSend.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, messageJson.replaceAll(SPECIAL_REGEX_CHARACTERS, "\\\\$0")));
				packet.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(messageToSend)));
			}
		}
	}
}
