#!/bin/bash
# Builds the Spigot versions KiwiCosmetics compiles against into your local Maven repository (~/.m2).
# Needs Java 25 on your PATH. Takes a while the first time.
set -e

VERSIONS="26.1.2 26.2 26.3"
DIR="spigotInstall"
mkdir -p "$DIR" && cd "$DIR"

if ! java -version 2>&1 | grep -q 'version "25'; then
   echo "Java 25 is required (found: $(java -version 2>&1 | head -1))."
   exit 1
fi

curl -fsSL -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar

for version in $VERSIONS; do
   if [ -f "$HOME/.m2/repository/org/spigotmc/spigot/$version-R0.1-SNAPSHOT/spigot-$version-R0.1-SNAPSHOT.jar" ]; then
      echo "Spigot $version is already installed, skipping."
      continue
   fi
   echo "Building Spigot $version..."
   java -jar BuildTools.jar --rev "$version" --remapped
done
echo "Done! Now run: ./gradlew clean build"
