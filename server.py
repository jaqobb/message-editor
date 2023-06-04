import sys
import os
import shutil
import requests


def main():
    arguments = sys.argv[1:]
    if len(arguments) == 0:
        print_help()
        return
    command = arguments[0].casefold()
    command_arguments = arguments[1:]
    if command == "download-spigot":
        download_spigot(command_arguments)
        return
    if command == "download-paper":
        download_paper(command_arguments)
        return
    if command == "copy":
        copy()
        return
    if command == "start":
        start()
        return
    if command == "clean":
        clean()
        return
    if command == "prepare":
        prepare()
        return
    print_help()
    return


def download_spigot(arguments):
    if len(arguments) == 0:
        print("Please specify a Minecraft version you want to download the latest version of Spigot for.")
        return
    minecraft_version = arguments[0]
    print(f"Downloading the latest version of Spigot for Minecraft {minecraft_version}...")
    open("server/buildtools/BuildTools.jar", "wb").write(requests.get("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar").content)
    os.chdir("server/buildtools")
    os.system(f"java -jar BuildTools.jar --rev {minecraft_version}")
    shutil.copyfile(f"spigot-{minecraft_version}.jar", f"../server.jar")
    print(f"The latest version of Spigot for Minecraft {minecraft_version} has been downloaded.")
    return


def download_paper(arguments):
    if len(arguments) < 2:
        print("Please specify a Minecraft version you want to download Paper for and a build of Paper.")
        return
    minecraft_version = arguments[0]
    paper_build = arguments[1]
    print(f"Downloading Paper-{paper_build} for Minecraft {minecraft_version}...")
    open("server/server.jar", "wb").write(requests.get(f"https://api.papermc.io/v2/projects/paper/versions/{minecraft_version}/builds/{paper_build}/downloads/paper-{minecraft_version}-{paper_build}.jar").content)
    print(f"Paper-{paper_build} for Minecraft {minecraft_version} has been downloaded.")
    return


def copy():
    plugin_name = ""
    plugin_version = ""
    plugin_has_shadow = False
    plugin_file = ""
    for line in open("settings.gradle.kts", "r"):
        if line.startswith("rootProject.name ="):
            plugin_name = line[len("rootProject.name =") + 2:-2]
            pass
    for line in open("build.gradle.kts", "r"):
        if line.startswith("version ="):
            plugin_version = line[len("version =") + 2:-2]
            pass
        if "id(\"com.github.johnrengelman.shadow\")" in line:
            plugin_has_shadow = True
            pass
    if plugin_has_shadow:
        plugin_file = f"{plugin_name}-{plugin_version}-all.jar"
        os.system("gradlew clean build shadowJar")
    else:
        plugin_file = f"{plugin_name}-{plugin_version}.jar"
        os.system("gradlew clean build")
    print(f"Copying plugin jar, {plugin_file}, to test server files...")
    shutil.copyfile(f"build/libs/{plugin_file}", f"server/plugins/{plugin_file}")
    print(f"Plugin jar, {plugin_file}, has been copied to test server files.")
    return


def start():
    print("Starting test server...")
    os.chdir("server")
    os.system("java -Dcom.mojang.eula.agree=true -jar server.jar --nogui --nojline")
    return


def clean():
    print("Cleaning test server files...")
    if os.path.exists("server"):
        print("Deleting server directory...")
        shutil.rmtree("server")
    print("Test server files cleaned.")
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
    print(" * download-spigot <Minecraft version>         | Downloads latest version of Spigot for specified Minecraft version.")
    print(" * download-paper  <Minecraft version> <build> | Downloads specified build of Paper for specified Minecraft version.")
    print(" * copy                                        | Compiles and copies plugin to test server files.")
    print(" * start                                       | Starts test server.")
    print(" * clean                                       | Cleans test server files.")
    print(" * prepare                                     | Creates test server empty core directories.")
    return


if __name__ == "__main__":
    main()
