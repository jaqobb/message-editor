base_folder=$(echo "${PWD}")

case "${1}" in
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
  (
    echo "This script provides a variety of commands to build and manage the test server."
    echo ""
    echo "Available commands:"
    echo " * copy  | Compiles and copies the plugin to the test server files."
    echo " * start | Starts the test server."
    echo " * clean | Cleans the test server files."
  )
  ;;
esac

unset RCPATH
unset SOURCE
unset base_folder
