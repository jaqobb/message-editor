import os
import shutil
import sys
import platform

import requests


SPIGOT_BUILD_TOOLS_DOWNLOAD_URL = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
PAPER_DOWNLOAD_URL = "https://api.papermc.io/v2/projects/paper/versions/{minecraft_version}/builds/{paper_build}/downloads/paper-{minecraft_version}-{paper_build}.jar"


def main():
    arguments = sys.argv[1:]
    if len(arguments) == 0:
        print_help()
        return
    command_name = arguments[0].casefold()
    command_arguments = arguments[1:]
    commands = {
        "download-spigot": download_spigot,
        "download-paper": download_paper,
        "copy-plugin": copy_plugin,
        "start": start,
        "delete": delete,
        "prepare": prepare,
    }
    commands.get(command_name, print_help)(*command_arguments)


def download_spigot(minecraft_version):
    print(f"Downloading Spigot for Minecraft {minecraft_version}...")
    request_content = requests.get(SPIGOT_BUILD_TOOLS_DOWNLOAD_URL).content
    with open("server/buildtools/BuildTools.jar", "wb") as file:
        file.write(request_content)
    os.chdir("server/buildtools")
    os.system(f"java -jar BuildTools.jar --rev {minecraft_version}")
    shutil.copyfile(f"spigot-{minecraft_version}.jar", f"../server.jar")
    print(f"Spigot for Minecraft {minecraft_version} has been downloaded.")
    return


def download_paper(minecraft_version, paper_build):
    print(f"Downloading Paper b{paper_build} for Minecraft {minecraft_version}...")
    request_url = PAPER_DOWNLOAD_URL.format(
        minecraft_version=minecraft_version,
        paper_build=paper_build
    )
    request_content = requests.get(request_url).content
    with open("server/server.jar", "wb") as file:
        file.write(request_content)
    print(f"Paper b{paper_build} for Minecraft {minecraft_version} has been downloaded.")
    return


def copy_plugin():
    plugin_name = ""
    plugin_version = ""
    plugin_has_shadow = False
    with open("settings.gradle.kts", "r") as file:
        for line in file:
            if line.startswith("rootProject.name ="):
                plugin_name = line[len("rootProject.name =") + 2:-2]
                pass
    with open("build.gradle.kts", "r") as file:
        for line in file:
            if line.startswith("version ="):
                plugin_version = line[len("version =") + 2:-2]
                pass
            if 'id("com.github.johnrengelman.shadow")' in line:
                plugin_has_shadow = True
                pass
    if plugin_has_shadow:
        plugin_file = f"{plugin_name}-{plugin_version}-all.jar"
        if platform.system() == "Windows":
            os.system("gradlew clean build shadowJar")
        else:
            os.system("./gradlew clean build shadowJar")
    else:
        plugin_file = f"{plugin_name}-{plugin_version}.jar"
        if platform.system() == "Windows":
            os.system("gradlew clean build")
        else:
            os.system("./gradlew clean build")
    print(f"Copying plugin jar, {plugin_file}, to test server files...")
    shutil.copyfile(f"build/libs/{plugin_file}", f"server/plugins/{plugin_file}")
    print(f"Plugin jar, {plugin_file}, has been copied to test server files.")
    return


def start():
    print("Starting test server...")
    os.chdir("server")
    os.system("java -Dcom.mojang.eula.agree=true -jar server.jar --nogui --nojline")
    return


def delete():
    print("Deleting test server files...")
    if os.path.exists("server"):
        print("Deleting server directory...")
        shutil.rmtree("server")
    print("Test server files deleted.")
    return


def prepare():
    print("Preparing test server's empty core directories...")
    if not os.path.exists("server"):
        print("Server directory does not exist, creating...")
        os.mkdir("server")
    if not os.path.exists("server/buildtools"):
        print("BuildTools directory does not exist, creating...")
        os.mkdir("server/buildtools")
    if not os.path.exists("server/plugins"):
        print("Plugins directory does not exist, creating...")
        os.mkdir("server/plugins")
    print("Test server's empty core directiories prepared.")
    return


def print_help():
    print("Available commands:")
    print(" * download-spigot <Minecraft version>")
    print(" * download-paper  <Minecraft version> <build>")
    print(" * copy-plugin")
    print(" * start")
    print(" * delete")
    print(" * prepare")
    return


if __name__ == "__main__":
    main()
