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

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageEditorPlugin extends JavaPlugin {

	static {
		ConfigurationSerialization.registerClass(MessageEdit.class);
	}

	private boolean logMessages;
	private boolean attachHoverAndClickEvents;
	private List<MessageEdit> messageEdits;

	private boolean placeholderApiFound;
	private boolean mvdwPlaceholderApiFound;

	private Cache<String, String> cachedMessages;

	@SuppressWarnings("unchecked")
	@Override
	public void onLoad() {
		this.getLogger().log(Level.INFO, "Loading configuration...");
		this.saveDefaultConfig();
		this.logMessages = this.getConfig().getBoolean("log-messages");
		this.attachHoverAndClickEvents = this.getConfig().getBoolean("attach-hover-and-click-events");
		if (this.attachHoverAndClickEvents) {
			this.getLogger().log(Level.INFO, "Checking if copying to clipboard is supported...");
			try {
				ClickEvent.Action.valueOf("COPY_TO_CLIPBOARD");
				this.getLogger().log(Level.INFO, "Copying to clipboard is supported...");
			} catch (IllegalArgumentException exception) {
				this.getLogger().log(Level.INFO, "Copying to clipboard is not supported on your server, disabling attaching hover and click events...");
				this.getLogger().log(Level.INFO, "Attaching hover and click events works only on server versions 1.15 and above.");
				this.attachHoverAndClickEvents = false;
			}
		}
		this.messageEdits = (List<MessageEdit>) this.getConfig().getList("message-edits");
		this.getLogger().log(Level.INFO, "Checking for placeholder APIs...");
		PluginManager pluginManager = this.getServer().getPluginManager();
		this.placeholderApiFound = pluginManager.isPluginEnabled("PlaceholderAPI");
		this.mvdwPlaceholderApiFound = pluginManager.isPluginEnabled("MVdWPlaceholderAPI");
		this.getLogger().log(Level.INFO, "PlaceholderAPI: " + (this.placeholderApiFound ? "found" : "not found") + ".");
		this.getLogger().log(Level.INFO, "MVdWPlaceholderAPI: " + (this.mvdwPlaceholderApiFound ? "found" : "not found") + ".");
		this.cachedMessages = CacheBuilder.newBuilder()
			.expireAfterAccess(15L, TimeUnit.MINUTES)
			.build();
	}

	@Override
	public void onEnable() {
		this.getLogger().log(Level.INFO, "Registering packet listener...");
		ProtocolLibrary.getProtocolManager().addPacketListener(new MessageEditorPacketListener(this));
	}

	public boolean isLoggingMessagesEnabled() {
		return this.logMessages;
	}

	public boolean isAttachingHoverAndClickEventsEnabled() {
		return this.attachHoverAndClickEvents;
	}

	public List<MessageEdit> getMessageEdits() {
		return Collections.unmodifiableList(this.messageEdits);
	}

	public boolean isPlaceholderApiPresent() {
		return this.placeholderApiFound;
	}

	public boolean isMvdwPlaceholderApiPresent() {
		return this.mvdwPlaceholderApiFound;
	}

	public Set<String> getCachedMessages() {
		return Collections.unmodifiableSet(this.cachedMessages.asMap().keySet());
	}

	public String getCachedMessage(String messageBefore) {
		return this.cachedMessages.getIfPresent(messageBefore);
	}

	public void cacheMessage(String messageBefore, String messageAfter) {
		this.cachedMessages.put(messageBefore, messageAfter);
	}

	public void uncacheMessage(String messageBefore) {
		this.cachedMessages.invalidate(messageBefore);
	}

	public void clearCachedMessages() {
		this.cachedMessages.invalidateAll();
	}
}
