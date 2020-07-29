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
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageEditorPlugin extends JavaPlugin {

	static {
		ConfigurationSerialization.registerClass(MessageEdit.class);
	}

	private List<MessageEdit> messageEdits;

	@SuppressWarnings("unchecked")
	@Override
	public void onLoad() {
		this.getLogger().log(Level.INFO, "Loading configuration...");
		this.saveDefaultConfig();
		this.messageEdits = (List<MessageEdit>) this.getConfig().getList("message-edits");
	}

	@Override
	public void onEnable() {
		this.getLogger().log(Level.INFO, "Registering packet listener...");
		ProtocolLibrary.getProtocolManager().addPacketListener(new MessageEditorPacketListener(this));
	}

	public List<MessageEdit> getMessageEdits() {
		return Collections.unmodifiableList(this.messageEdits);
	}
}
