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

package dev.jaqobb.messageeditor;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.jaqobb.messageeditor.command.MessageEditorCommand;
import dev.jaqobb.messageeditor.command.MessageEditorCommandTabCompleter;
import dev.jaqobb.messageeditor.listener.packet.BossBarPacketListener;
import dev.jaqobb.messageeditor.listener.packet.ChatPacketListener;
import dev.jaqobb.messageeditor.listener.packet.DisconnectPacketListener;
import dev.jaqobb.messageeditor.listener.packet.EntityNamePacketListener;
import dev.jaqobb.messageeditor.listener.packet.InventoryItemsPacketListener;
import dev.jaqobb.messageeditor.listener.packet.InventoryTitlePacketListener;
import dev.jaqobb.messageeditor.listener.packet.KickPacketListener;
import dev.jaqobb.messageeditor.listener.packet.ScoreboardEntryPacketListener;
import dev.jaqobb.messageeditor.listener.packet.ScoreboardTitlePacketListener;
import dev.jaqobb.messageeditor.listener.player.PlayerChatListener;
import dev.jaqobb.messageeditor.listener.player.PlayerInventoryClickListener;
import dev.jaqobb.messageeditor.listener.player.PlayerInventoryCloseListener;
import dev.jaqobb.messageeditor.listener.player.PlayerJoinListener;
import dev.jaqobb.messageeditor.listener.player.PlayerKickListener;
import dev.jaqobb.messageeditor.listener.player.PlayerQuitListener;
import dev.jaqobb.messageeditor.listener.plugin.PluginDisableListener;
import dev.jaqobb.messageeditor.listener.plugin.PluginEnableListener;
import dev.jaqobb.messageeditor.menu.MenuManager;
import dev.jaqobb.messageeditor.message.MessageData;
import dev.jaqobb.messageeditor.message.MessageEdit;
import dev.jaqobb.messageeditor.message.MessageEditData;
import dev.jaqobb.messageeditor.message.MessagePlace;
import dev.jaqobb.messageeditor.updater.Updater;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageEditorPlugin extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(MessageEdit.class);
    }

    private Metrics                                       metrics;
    private boolean                                       updateNotify;
    private Updater                                       updater;
    private List<MessageEdit>                             messageEdits;
    private boolean                                       attachSpecialHoverAndClickEvents;
    private boolean                                       placeholderApiPresent;
    private boolean                                       mvdwPlaceholderApiPresent;
    private MenuManager                                   menuManager;
    private Cache<String, Map.Entry<MessageEdit, String>> cachedMessages;
    private Cache<String, MessageData>                    cachedMessagesData;
    private Map<UUID, MessageEditData>                    currentMessageEditsData;

    @Override
    public void onLoad() {
        MinecraftVersion minimumRequiredMinecraftVersion = null;
        for (MessagePlace messagePlace : MessagePlace.VALUES) {
            MinecraftVersion messagePlaceMininumRequiredMinecraftVersion = messagePlace.getMinimumRequiredMinecraftVersion();
            if (minimumRequiredMinecraftVersion == null || minimumRequiredMinecraftVersion.compareTo(messagePlaceMininumRequiredMinecraftVersion) > 0) {
                minimumRequiredMinecraftVersion = messagePlaceMininumRequiredMinecraftVersion;
            }
        }
        if (!MinecraftVersion.atOrAbove(minimumRequiredMinecraftVersion)) {
            this.getLogger().log(Level.WARNING, "Your server does not support any message places.");
            this.getLogger().log(Level.WARNING, "The minimum required server version is " + minimumRequiredMinecraftVersion.getVersion() + ".");
            this.getLogger().log(Level.WARNING, "Disabling plugin...");
            this.setEnabled(false);
            return;
        }
        this.getLogger().log(Level.INFO, "Starting metrics...");
        this.metrics = new Metrics(this, 8376);
        this.getLogger().log(Level.INFO, "Loading configuration...");
        this.saveDefaultConfig();
        this.reloadConfig();
        this.getLogger().log(Level.INFO, "Checking for placeholder APIs...");
        PluginManager pluginManager = this.getServer().getPluginManager();
        this.placeholderApiPresent     = pluginManager.getPlugin(MessageEditorConstants.PLACEHOLDER_API_PLUGIN_NAME) != null;
        this.mvdwPlaceholderApiPresent = pluginManager.getPlugin(MessageEditorConstants.MVDW_PLACEHOLDER_API_PLUGIN_NAME) != null;
        this.getLogger().log(Level.INFO, MessageEditorConstants.PLACEHOLDER_API_PLUGIN_NAME + ": " + (this.placeholderApiPresent ? "present" : "not present") + ".");
        this.getLogger().log(Level.INFO, MessageEditorConstants.MVDW_PLACEHOLDER_API_PLUGIN_NAME + ": " + (this.mvdwPlaceholderApiPresent ? "present" : "not present") + ".");
        this.cachedMessages          = CacheBuilder.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES)
            .build();
        this.cachedMessagesData      = CacheBuilder.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES)
            .build();
        this.currentMessageEditsData = new HashMap<>(16);
    }

    @Override
    public void onEnable() {
        this.getLogger().log(Level.INFO, "Starting updater...");
        this.updater = new Updater(this, 82154);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, this.updater, 0L, 20L * 60L * 30L);
        this.getLogger().log(Level.INFO, "Starting menu manager...");
        this.menuManager = new MenuManager(this);
        this.getLogger().log(Level.INFO, "Registering command...");
        this.getCommand("message-editor").setExecutor(new MessageEditorCommand(this));
        this.getCommand("message-editor").setTabCompleter(new MessageEditorCommandTabCompleter());
        this.getLogger().log(Level.INFO, "Registering listeners...");
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PluginEnableListener(this), this);
        pluginManager.registerEvents(new PluginDisableListener(this), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new PlayerKickListener(this), this);
        pluginManager.registerEvents(new PlayerInventoryCloseListener(this), this);
        pluginManager.registerEvents(new PlayerInventoryClickListener(this), this);
        pluginManager.registerEvents(new PlayerChatListener(this), this);
        this.getLogger().log(Level.INFO, "Registering packet listeners...");
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new ChatPacketListener(this));
        protocolManager.addPacketListener(new KickPacketListener(this));
        protocolManager.addPacketListener(new DisconnectPacketListener(this));
        protocolManager.addPacketListener(new BossBarPacketListener(this));
        protocolManager.addPacketListener(new ScoreboardTitlePacketListener(this));
        protocolManager.addPacketListener(new ScoreboardEntryPacketListener(this));
        protocolManager.addPacketListener(new InventoryTitlePacketListener(this));
        protocolManager.addPacketListener(new InventoryItemsPacketListener(this));
        protocolManager.addPacketListener(new EntityNamePacketListener(this));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.updateNotify                     = this.getConfig().getBoolean("update.notify", true);
        this.attachSpecialHoverAndClickEvents = this.getConfig().getBoolean("attach-special-hover-and-click-events", true);
        this.messageEdits                     = (List<MessageEdit>) this.getConfig().getList("message-edits");
    }

    public Metrics getMetrics() {
        return this.metrics;
    }

    public boolean isUpdateNotify() {
        return this.updateNotify;
    }

    public Updater getUpdater() {
        return this.updater;
    }

    public List<MessageEdit> getMessageEdits() {
        return Collections.unmodifiableList(this.messageEdits);
    }

    public void addMessageEdit(MessageEdit messageEdit) {
        this.messageEdits.add(messageEdit);
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

    public MenuManager getMenuManager() {
        return this.menuManager;
    }

    public Set<String> getCachedMessages() {
        return Collections.unmodifiableSet(this.cachedMessages.asMap().keySet());
    }

    public Map.Entry<MessageEdit, String> getCachedMessage(String messageBefore) {
        return this.cachedMessages.getIfPresent(messageBefore);
    }

    public void cacheMessage(String messageBefore, MessageEdit messageEdit, String messageAfter) {
        this.cachedMessages.put(messageBefore, new AbstractMap.SimpleEntry<>(messageEdit, messageAfter));
    }

    public void uncacheMessage(String messageBefore) {
        this.cachedMessages.invalidate(messageBefore);
    }

    public void clearCachedMessages() {
        this.cachedMessages.invalidateAll();
    }

    public Set<String> getCachedMessagesData() {
        return Collections.unmodifiableSet(this.cachedMessagesData.asMap().keySet());
    }

    public MessageData getCachedMessageData(String messageId) {
        return this.cachedMessagesData.getIfPresent(messageId);
    }

    public void cacheMessageData(String messageId, MessageData messageData) {
        this.cachedMessagesData.put(messageId, messageData);
    }

    public void uncacheMessageData(String messageId) {
        this.cachedMessagesData.invalidate(messageId);
    }

    public void clearCachedMessagesData() {
        this.cachedMessagesData.invalidateAll();
    }

    public Map<UUID, MessageEditData> getCurrentMessageEditsData() {
        return Collections.unmodifiableMap(this.currentMessageEditsData);
    }

    public MessageEditData getCurrentMessageEditData(UUID uuid) {
        return this.currentMessageEditsData.get(uuid);
    }

    public void setCurrentMessageEdit(UUID uuid, MessageEditData messageEditData) {
        this.currentMessageEditsData.put(uuid, messageEditData);
    }

    public void removeCurrentMessageEditData(UUID uuid) {
        this.currentMessageEditsData.remove(uuid);
    }

    public void clearCurrentMessageEditsData() {
        this.currentMessageEditsData.clear();
    }
}
