plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "dev.jaqobb"
version = "2.4.5-SNAPSHOT"
description = "Edit in-game messages that were previously unmodifiable"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

bukkit {
    name = "MessageEditor"
    main = "dev.jaqobb.messageeditor.MessageEditorPlugin"
    version = project.version as String
    apiVersion = "1.13"
    depend = listOf("ProtocolLib")
    softDepend = listOf("PlaceholderAPI", "MVdWPlaceholderAPI")
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
        relocate("com.cryptomorin.xseries", "dev.jaqobb.messageeditor.library.xseries")
        relocate("org.bstats.bukkit", "dev.jaqobb.messageeditor.metrics")
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
    maven("https://libraries.minecraft.net") {
        content {
            includeGroup("com.mojang")
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
    maven("http://repo.mvdw-software.be/content/groups/public/") {
        content {
            includeGroup("be.maximvdw")
        }
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        content {
            includeGroup("org.bstats")
        }
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:2.0.27")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
    compileOnly("me.clip:placeholderapi:2.10.9")
    compileOnly("be.maximvdw:MVdWPlaceholderAPI:3.1.1-SNAPSHOT") {
        exclude("org.spigotmc")
    }
    implementation("com.github.cryptomorin:XSeries:7.6.0.0.1")
    implementation("org.bstats:bstats-bukkit:1.7")
}
