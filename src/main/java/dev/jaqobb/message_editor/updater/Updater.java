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

package dev.jaqobb.message_editor.updater;

import dev.jaqobb.message_editor.MessageEditorPlugin;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import javax.net.ssl.HttpsURLConnection;

public final class Updater implements Runnable {

    private final MessageEditorPlugin plugin;
    private final int pluginId;
    private final String currentVersion;
    private String latestVersion;
    private Integer versionDifference;

    public Updater(MessageEditorPlugin plugin, int pluginId) {
        this.plugin = plugin;
        this.pluginId = pluginId;
        this.currentVersion = this.plugin.getDescription().getVersion();
        this.latestVersion = null;
        this.versionDifference = null;
    }

    public String getCurrentVersion() {
        return this.currentVersion;
    }

    public String getLatestVersion() {
        return this.latestVersion;
    }

    public Integer getVersionDifference() {
        return this.versionDifference;
    }

    public String getUpdateMessage() {
        if (this.currentVersion.contains("-SNAPSHOT")) {
            return "&cYou are running a development version of &7Message Editor &c(&7" + this.currentVersion + "&c). Development versions are not heavily tested so avoid running them on production servers.";
        } else if (this.versionDifference == null || this.latestVersion == null) {
            return "&cCould not retrieve the latest version of &7Message Editor&c.";
        } else if (this.versionDifference < 0) {
            return "&7You are running an outdated version of &eMessage Editor &7(&e" + this.currentVersion + " &7< &e" + this.latestVersion + "&7). Consider updating to receive new features, bug fixes and more.";
        } else {
            return "&7You are running the latest version of &eMessage Editor&7.";
        }
    }

    @Override
    public void run() {
        if (this.currentVersion.contains("-SNAPSHOT")) {
            return;
        }
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.pluginId).openConnection();
            connection.setRequestMethod("GET");
            try (
                InputStream input = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
            ) {
                this.latestVersion = reader.readLine();
                String[] currentVersionData = this.currentVersion.split("\\.");
                String[] latestVersionData = this.latestVersion.split("\\.");
                if (currentVersionData.length == 3 && latestVersionData.length == 3) {
                    int majorDifference = Integer.compare(
                        Integer.parseInt(currentVersionData[0]),
                        Integer.parseInt(latestVersionData[0])
                    );
                    if (majorDifference != 0) {
                        this.versionDifference = majorDifference;
                    } else {
                        int minorDifference = Integer.compare(
                            Integer.parseInt(currentVersionData[1]),
                            Integer.parseInt(latestVersionData[1])
                        );
                        if (minorDifference != 0) {
                            this.versionDifference = minorDifference;
                        } else {
                            this.versionDifference = Integer.compare(
                                Integer.parseInt(currentVersionData[2]),
                                Integer.parseInt(latestVersionData[2])
                            );
                        }
                    }
                }
            }
            connection.disconnect();
        } catch (Exception exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not retrieve the latest version data.", exception);
        }
    }
}
