plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "dev.jaqobb"
version = "1.5.3"
description = "Spigot plugin that allows editing in-game messages that were previously unmodifiable"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

bukkit {
    name = "MessageEditor"
    main = "dev.jaqobb.messageeditor.MessageEditorPlugin"
    version = project.version as String
    depend = listOf("ProtocolLib")
    softDepend = listOf("PlaceholderAPI", "MVdWPlaceholderAPI")
    description = project.description
    author = "jaqobb"
    website = "https://jaqobb.dev"
    commands {
        create("message-editor") {
            description = "Message Editor main command"
        }
    }
}

tasks {
    shadowJar {
        relocate("org.bstats.bukkit", "dev.jaqobb.messageeditor.metrics")
    }
}

repositories {
    jcenter()
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
    compileOnly("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.10.9")
    compileOnly("be.maximvdw:MVdWPlaceholderAPI:3.1.1-SNAPSHOT") {
        exclude("org.spigotmc")
    }
    implementation("org.bstats:bstats-bukkit:1.7")
}
