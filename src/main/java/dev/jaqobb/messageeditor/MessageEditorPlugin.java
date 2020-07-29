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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageEditorPlugin extends JavaPlugin {

	static {
		ConfigurationSerialization.registerClass(MessageEdit.class);
	}

	private List<MessageEdit> messageEdits;
	private boolean placeholderApiPresent;
	private boolean mvdwPlaceholderApiPresent;

	@SuppressWarnings("unchecked")
	@Override
	public void onLoad() {
		this.getLogger().log(Level.INFO, "Loading configuration...");
		this.saveDefaultConfig();
		this.messageEdits = (List<MessageEdit>) this.getConfig().getList("message-edits");
		this.getLogger().log(Level.INFO, "Checking for placeholder APIs...");
		PluginManager pluginManager = this.getServer().getPluginManager();
		this.placeholderApiPresent = pluginManager.isPluginEnabled("PlaceholderAPI");
		this.mvdwPlaceholderApiPresent = pluginManager.isPluginEnabled("MVdWPlaceholderAPI");
		this.getLogger().log(Level.INFO, "PlaceholderAPI: " + (this.placeholderApiPresent ? "present" : "not present") + ".");
		this.getLogger().log(Level.INFO, "MVdWPlaceholderAPI: " + (this.mvdwPlaceholderApiPresent ? "present" : "not present") + ".");
	}

	@Override
	public void onEnable() {
		this.getLogger().log(Level.INFO, "Registering packet listener...");
		ProtocolLibrary.getProtocolManager().addPacketListener(new MessageEditorPacketListener(this));
	}

	public List<MessageEdit> getMessageEdits() {
		return Collections.unmodifiableList(this.messageEdits);
	}

	public boolean isPlaceholderApiPresent() {
		return this.placeholderApiPresent;
	}

	public boolean isMvdwPlaceholderApiPresent() {
		return this.mvdwPlaceholderApiPresent;
	}
}
