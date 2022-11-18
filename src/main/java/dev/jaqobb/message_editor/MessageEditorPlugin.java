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

package dev.jaqobb.message_editor;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.jaqobb.message_editor.command.MessageEditorCommand;
import dev.jaqobb.message_editor.command.MessageEditorCommandTabCompleter;
import dev.jaqobb.message_editor.listener.packet.BossBarPacketListener;
import dev.jaqobb.message_editor.listener.packet.ChatPacketListener;
import dev.jaqobb.message_editor.listener.packet.DisconnectPacketListener;
import dev.jaqobb.message_editor.listener.packet.EntityNamePacketListener;
import dev.jaqobb.message_editor.listener.packet.InventoryItemsPacketListener;
import dev.jaqobb.message_editor.listener.packet.InventoryTitlePacketListener;
import dev.jaqobb.message_editor.listener.packet.KickPacketListener;
import dev.jaqobb.message_editor.listener.packet.ScoreboardEntryPacketListener;
import dev.jaqobb.message_editor.listener.packet.ScoreboardTitlePacketListener;
import dev.jaqobb.message_editor.listener.player.PlayerChatListener;
import dev.jaqobb.message_editor.listener.player.PlayerInventoryClickListener;
import dev.jaqobb.message_editor.listener.player.PlayerInventoryCloseListener;
import dev.jaqobb.message_editor.listener.player.PlayerJoinListener;
import dev.jaqobb.message_editor.listener.player.PlayerKickListener;
import dev.jaqobb.message_editor.listener.player.PlayerQuitListener;
import dev.jaqobb.message_editor.listener.plugin.PluginDisableListener;
import dev.jaqobb.message_editor.listener.plugin.PluginEnableListener;
import dev.jaqobb.message_editor.menu.MenuManager;
import dev.jaqobb.message_editor.message.MessageData;
import dev.jaqobb.message_editor.message.MessageEdit;
import dev.jaqobb.message_editor.message.MessageEditData;
import dev.jaqobb.message_editor.message.MessagePlace;
import dev.jaqobb.message_editor.updater.Updater;
import java.io.File;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageEditorPlugin extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(MessageEdit.class);
    }

    private Metrics metrics;
    private boolean updateNotify;
    private Updater updater;
    private List<MessageEdit> messageEdits;
    private boolean attachSpecialHoverAndClickEvents;
    private boolean placeholderApiPresent;
    private MenuManager menuManager;
    private Cache<String, Map.Entry<MessageEdit, String>> cachedMessages;
    private Cache<String, MessageData> cachedMessagesData;
    private Map<UUID, MessageEditData> currentMessageEditsData;

    @Override
    public void onLoad() {
        MinecraftVersion requiredVersion = null;
        for (MessagePlace place : MessagePlace.VALUES) {
            MinecraftVersion version = place.getMinimumRequiredMinecraftVersion();
            if (requiredVersion == null || requiredVersion.compareTo(version) > 0) {
                requiredVersion = version;
            }
        }
        if (!MinecraftVersion.atOrAbove(requiredVersion)) {
            this.getLogger().log(Level.WARNING, "Your server does not support any message places.");
            this.getLogger().log(Level.WARNING, "The minimum required server version is " + requiredVersion.getVersion() + ".");
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
        this.placeholderApiPresent = pluginManager.getPlugin(MessageEditorConstants.PLACEHOLDER_API_PLUGIN_NAME) != null;
        this.getLogger().log(Level.INFO, MessageEditorConstants.PLACEHOLDER_API_PLUGIN_NAME + ": " + (this.placeholderApiPresent ? "present" : "not present") + ".");
        this.cachedMessages = CacheBuilder.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES)
            .build();
        this.cachedMessagesData = CacheBuilder.newBuilder()
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
        if (this.getConfig().getBoolean("packet-listeners.chat", true)) {
            protocolManager.addPacketListener(new ChatPacketListener(this));
        }
        if (this.getConfig().getBoolean("packet-listeners.kick", true)) {
            protocolManager.addPacketListener(new KickPacketListener(this));
        }
        if (this.getConfig().getBoolean("packet-listeners.disconnect", true)) {
            protocolManager.addPacketListener(new DisconnectPacketListener(this));
        }
        if (this.getConfig().getBoolean("packet-listeners.bossbar", true)) {
            protocolManager.addPacketListener(new BossBarPacketListener(this));
        }
        if (this.getConfig().getBoolean("packet-listeners.scoreboard-title", true)) {
            protocolManager.addPacketListener(new ScoreboardTitlePacketListener(this));
        }
        if (this.getConfig().getBoolean("packet-listeners.scoreboard-entry", true)) {
            protocolManager.addPacketListener(new ScoreboardEntryPacketListener(this));
        }
        if (this.getConfig().getBoolean("packet-listeners.inventory-title", true)) {
            protocolManager.addPacketListener(new InventoryTitlePacketListener(this));
        }
        if (this.getConfig().getBoolean("packet-listeners.inventory-item", true)) {
            protocolManager.addPacketListener(new InventoryItemsPacketListener(this));
        }
        if (this.getConfig().getBoolean("packet-listeners.entity-name", true)) {
            protocolManager.addPacketListener(new EntityNamePacketListener(this));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.updateNotify = this.getConfig().getBoolean("update.notify", true);
        this.attachSpecialHoverAndClickEvents = this.getConfig().getBoolean("attach-special-hover-and-click-events", true);
        this.messageEdits = (List<MessageEdit>) this.getConfig().getList("message-edits");
        File editsDirectory = new File(this.getDataFolder(), "edits");
        if (!editsDirectory.exists()) {
            if (!editsDirectory.mkdir()) {
                this.getLogger().log(Level.WARNING, "Could not create 'edits' directory.");
                return;
            }
            // TODO: Copy default edits.
        }
        for (File editFile : editsDirectory.listFiles()) {
            String name = editFile.getName();
            if (!name.isEmpty() && name.charAt(0) == '#') {
                continue;
            }
            if (!name.endsWith(".yml")) {
                continue;
            }
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(editFile);
            this.messageEdits.add(MessageEdit.deserialize(configuration.getRoot().getValues(false)));
        }
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

    public boolean isAttachSpecialHoverAndClickEvents() {
        return this.attachSpecialHoverAndClickEvents;
    }

    public boolean isPlaceholderApiPresent() {
        return this.placeholderApiPresent;
    }

    public void setPlaceholderApiPresent(boolean present) {
        this.placeholderApiPresent = present;
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

    public void cacheMessage(String messageBefore, MessageEdit edit, String messageAfter) {
        this.cachedMessages.put(messageBefore, new AbstractMap.SimpleEntry<>(edit, messageAfter));
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

    public MessageData getCachedMessageData(String id) {
        return this.cachedMessagesData.getIfPresent(id);
    }

    public void cacheMessageData(String id, MessageData data) {
        this.cachedMessagesData.put(id, data);
    }

    public void uncacheMessageData(String id) {
        this.cachedMessagesData.invalidate(id);
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

    public void setCurrentMessageEdit(UUID uuid, MessageEditData editData) {
        this.currentMessageEditsData.put(uuid, editData);
    }

    public void removeCurrentMessageEditData(UUID uuid) {
        this.currentMessageEditsData.remove(uuid);
    }

    public void clearCurrentMessageEditsData() {
        this.currentMessageEditsData.clear();
    }
}
