plugins {
	java
	id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

group = "dev.jaqobb"
version = "1.0.0"
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
	description = project.description
	author = "jaqobb"
	website = "https://jaqobb.dev"
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
}

dependencies {
	compileOnly("org.spigotmc:spigot-api:1.16.1-R0.1-SNAPSHOT")
	compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0-SNAPSHOT")
}
