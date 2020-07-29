#!/usr/bin/env bash

#
# MIT License
#
# Copyright (c) 2020 Jakub Zagórski (jaqobb)
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

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
		./gradlew clean build
		cd "${base_folder}/build/libs"
		plugin_name=$(grep < "${base_folder}/settings.gradle.kts" "rootProject.name =" | cut -d ' ' -f 3 | tr -d \" | head -1)
		plugin_version=$(grep < "${base_folder}/build.gradle.kts" "version =" | cut -d ' ' -f 3 | tr -d \" | head -1)
		plugin_has_shadow=$(grep < "${base_folder}/build.gradle.kts" "id(\"com.github.johnrengelman.shadow\")" | head -1)
		if [ -z "${plugin_has_shadow}" ]; then
			plugin_file="${plugin_name}-${plugin_version}.jar"
		else
			plugin_file="${plugin_name}-${plugin_version}-all.jar"
		fi
		echo "Copying the plugin's jar, ${plugin_file}, to the test server files..."
		cp "${plugin_file}" "../../server/plugins"
		echo "The plugin's jar, ${plugin_file}, has been copied to the test server files."
	)
	;;
	"start")
	(
		set -e
		cd "${base_folder}/server"
		java -Dcom.mojang.eula.agree=true -jar "Paper.jar" --nogui --nojline
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
