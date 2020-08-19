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
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.jaqobb.messageeditor.command.MessageEditorCommand;
import dev.jaqobb.messageeditor.command.MessageEditorCommandTabCompleter;
import dev.jaqobb.messageeditor.data.edit.MessageEdit;
import dev.jaqobb.messageeditor.data.place.MessagePlace;
import dev.jaqobb.messageeditor.listener.MessageEditorListener;
import dev.jaqobb.messageeditor.listener.MessageEditorPacketListener;
import dev.jaqobb.messageeditor.updater.MessageEditorUpdater;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageEditorPlugin extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(MessageEdit.class);
    }

    private Metrics metrics;
    private MessageEditorUpdater updater;
    private List<MessageEdit> messageEdits;
    private boolean attachSpecialHoverAndClickEvents;
    private boolean placeholderApiPresent;
    private boolean mvdwPlaceholderApiPresent;
    private Cache<String, String> cachedMessages;

    @SuppressWarnings("unchecked")
    @Override
    public void onLoad() {
        MinecraftVersion minimumRequiredMinecraftVersion = null;
        for (MessagePlace messagePlace : MessagePlace.values()) {
            MinecraftVersion messagePlaceMininumRequiredMinecraftVersion = messagePlace.getMinimumRequiredMinecraftVersion();
            if (minimumRequiredMinecraftVersion == null || minimumRequiredMinecraftVersion.compareTo(messagePlaceMininumRequiredMinecraftVersion) > 0) {
                minimumRequiredMinecraftVersion = messagePlaceMininumRequiredMinecraftVersion;
            }
        }
        if (!MinecraftVersion.atOrAbove(minimumRequiredMinecraftVersion)) {
            this.getLogger().log(Level.WARNING, "Your server does not support any message places. The minimum required server version is " + minimumRequiredMinecraftVersion.getVersion() + ". Disabling plugin...");
            this.setEnabled(false);
            return;
        }
        this.getLogger().log(Level.INFO, "Starting metrics...");
        this.metrics = new Metrics(this, 8376);
        this.getLogger().log(Level.INFO, "Loading configuration...");
        this.saveDefaultConfig();
        this.attachSpecialHoverAndClickEvents = this.getConfig().getBoolean("attach-special-hover-and-click-events", true);
        try {
            ClickEvent.Action.valueOf("COPY_TO_CLIPBOARD");
        } catch (IllegalArgumentException exception) {
            if (this.attachSpecialHoverAndClickEvents) {
                this.attachSpecialHoverAndClickEvents = false;
                this.getLogger().log(Level.INFO, "Copying to clipboard is not supported on your server.");
                this.getLogger().log(Level.INFO, "Attaching special hover and click events works only on server version at least 1.15.");
            }
        }
        this.messageEdits = (List<MessageEdit>) this.getConfig().getList("message-edits");
        this.getLogger().log(Level.INFO, "Checking for placeholder APIs...");
        PluginManager pluginManager = this.getServer().getPluginManager();
        this.placeholderApiPresent = pluginManager.isPluginEnabled("PlaceholderAPI");
        this.mvdwPlaceholderApiPresent = pluginManager.isPluginEnabled("MVdWPlaceholderAPI");
        this.getLogger().log(Level.INFO, "PlaceholderAPI: " + (this.placeholderApiPresent ? "present" : "not present") + ".");
        this.getLogger().log(Level.INFO, "MVdWPlaceholderAPI: " + (this.mvdwPlaceholderApiPresent ? "present" : "not present") + ".");
        this.cachedMessages = CacheBuilder.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES)
            .build();
    }

    @Override
    public void onEnable() {
        this.getLogger().log(Level.INFO, "Starting updater...");
        this.updater = new MessageEditorUpdater(this);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, this.updater, 0L, 20L * 60L * 30L);
        this.getLogger().log(Level.INFO, "Registering command...");
        this.getCommand("message-editor").setExecutor(new MessageEditorCommand(this));
        this.getCommand("message-editor").setTabCompleter(new MessageEditorCommandTabCompleter());
        this.getLogger().log(Level.INFO, "Registering listener...");
        this.getServer().getPluginManager().registerEvents(new MessageEditorListener(this), this);
        this.getLogger().log(Level.INFO, "Registering packet listener...");
        ProtocolLibrary.getProtocolManager().addPacketListener(new MessageEditorPacketListener(this));
    }

    public String getPrefix() {
        return ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Message Editor" + ChatColor.DARK_GRAY + "] ";
    }

    public Metrics getMetrics() {
        return this.metrics;
    }

    public MessageEditorUpdater getUpdater() {
        return this.updater;
    }

    public List<MessageEdit> getMessageEdits() {
        return Collections.unmodifiableList(this.messageEdits);
    }

    public boolean isAttachingSpecialHoverAndClickEventsEnabled() {
        return this.attachSpecialHoverAndClickEvents;
    }

    public boolean isPlaceholderApiPresent() {
        return this.placeholderApiPresent;
    }

    public void setPlaceholderApiPresent(boolean present) {
        this.placeholderApiPresent = present;
    }

    public boolean isMvdwPlaceholderApiPresent() {
        return this.mvdwPlaceholderApiPresent;
    }

    public void setMvdwPlaceholderApiPresent(boolean present) {
        this.mvdwPlaceholderApiPresent = present;
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
