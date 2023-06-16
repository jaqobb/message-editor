plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.jaqobb"
version = "2.5.3-SNAPSHOT"
description = "Edit in-game messages that were previously unmodifiable"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

bukkit {
    name = "MessageEditor"
    main = "dev.jaqobb.message_editor.MessageEditorPlugin"
    version = project.version as String
    apiVersion = "1.13"
    depend = listOf("ProtocolLib")
    softDepend = listOf("PlaceholderAPI")
    description = project.description
    author = "jaqobb"
    website = "https://jaqobb.dev"
    commands {
        create("message-editor") {
            description = "Message Editor main command"
            aliases = listOf("messageeditor")
        }
    }
}

tasks {
    shadowJar {
        exclude("com/cryptomorin/xseries/messages/*")
        exclude("com/cryptomorin/xseries/particles/*")
        exclude("com/cryptomorin/xseries/unused/*")
        exclude("com/cryptomorin/xseries/NMSExtras*")
        exclude("com/cryptomorin/xseries/NoteBlockMusic*")
        exclude("com/cryptomorin/xseries/ReflectionUtils*")
        exclude("com/cryptomorin/xseries/SkullCacheListener*")
        exclude("com/cryptomorin/xseries/SkullUtils*")
        exclude("com/cryptomorin/xseries/XBiome*")
        exclude("com/cryptomorin/xseries/XBlock*")
        exclude("com/cryptomorin/xseries/XEnchantment*")
        exclude("com/cryptomorin/xseries/XEntity*")
        exclude("com/cryptomorin/xseries/XItemStack*")
        exclude("com/cryptomorin/xseries/XPotion*")
        exclude("com/cryptomorin/xseries/XTag*")
        relocate("com.cryptomorin.xseries", "dev.jaqobb.message_editor.library.xseries")
        relocate("org.bstats", "dev.jaqobb.message_editor.metrics")
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.spigotmc")
        }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        content {
            includeGroup("net.md-5")
        }
    }
    maven("https://repo.dmulloy2.net/nexus/repository/public/") {
        content {
            includeGroup("com.comphenix.protocol")
        }
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        content {
            includeGroup("me.clip")
        }
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        content {
            includeGroup("org.bstats")
        }
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")
    compileOnly("me.clip:placeholderapi:2.11.3")
    implementation("com.github.cryptomorin:XSeries:9.4.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")
}
