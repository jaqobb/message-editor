case "$(echo "${SHELL}" | sed -E 's|/usr(/local)?||g')" in
	"/bin/zsh")
		RCPATH="${HOME}/.zshrc"
		SOURCE="${BASH_SOURCE[0]:-${(%):-%N}}"
	;;

	*)
		RCPATH="${HOME}/.bashrc"
		if [[ -f "${HOME}/.bash_aliases" ]]; then
			RCPATH="${HOME}/.bash_aliases"
		fi
		SOURCE="${BASH_SOURCE[0]}"
	;;
esac

while [ -h "${SOURCE}" ]; do
	DIRECTORY="$(cd -P "$(dirname "${SOURCE}")" && pwd)"
	SOURCE="$(readlink "${SOURCE}")"
	if [[ "${SOURCE}" != /* ]]; then
		SOURCE="${DIRECTORY}/${SOURCE}"
	fi
done

SOURCE=$([[ "${SOURCE}" = /* ]] && echo "${SOURCE}" || echo "${PWD}/${SOURCE#./}")
base_folder=$(dirname "${SOURCE}")

case "${1}" in
	"download")
	(
		if [ -z "${2}" ]; then
			echo "You have to specify a Minecraft version you want to download the latest version of Paper for."
		else
			set -e
			cd "${base_folder}"
			mkdir -p "server/plugins"
			echo "Downloading the latest version of Paper for the Minecraft version ${2}..."
			curl "https://papermc.io/api/v1/paper/${2}/latest/download" --output "server/Paper.jar"
			echo "The latest version of Paper for the Minecraft version ${2} has been downloaded."
		fi
	)
	;;
	"copy")
	(
		set -e
		cd "${base_folder}"
		plugin_name=$(grep < "${base_folder}/settings.gradle.kts" "rootProject.name =" | cut -d ' ' -f 3 | tr -d \" | head -1)
		plugin_version=$(grep < "${base_folder}/build.gradle.kts" "version =" | cut -d ' ' -f 3 | tr -d \" | head -1)
		plugin_has_shadow=$(grep < "${base_folder}/build.gradle.kts" "id(\"com.github.johnrengelman.shadow\")" | head -1)
		if [ -z "${plugin_has_shadow}" ]; then
			plugin_file="${plugin_name}-${plugin_version}.jar"
			./gradlew clean build
		else
			plugin_file="${plugin_name}-${plugin_version}-all.jar"
			./gradlew clean build shadowJar
		fi
		cd "${base_folder}/build/libs"
		echo "Copying the plugin's jar, ${plugin_file}, to the test server files..."
		cp "${plugin_file}" "../../server/plugins"
		echo "The plugin's jar, ${plugin_file}, has been copied to the test server files."
	)
	;;
	"start")
	(
		set -e
		cd "${base_folder}/server"
		java -Dcom.mojang.eula.agree=true -jar "server.jar" --nogui --nojline
	)
	;;
	"clean")
	(
		set -e
		cd "${base_folder}"
		rm -rf "server"
		mkdir -p "server/plugins"
		echo "The test server files have been cleaned."
	)
	;;
	*)
		echo "This script provides a variety of commands to build and manage the test server."
		echo ""
		echo "Available commands:"
		echo " * download <Minecraft version> | Downloads the latest version of Paper for the specified Minecraft version."
		echo " * copy                         | Compiles and copies the plugin to the test server files."
		echo " * start                        | Starts the test server."
		echo " * clean                        | Cleans the test server files."
  ;;
esac

unset RCPATH
unset SOURCE
unset base_folder
