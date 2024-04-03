#!/bin/bash
export PATH=/home/tw1/bin:/home/tw1/.sdkman/candidates/kscript/current/bin:/home/tw1/.sdkman/candidates/kotlin/current/bin:/home/tw1/.sdkman/candidates/gradle/current/bin:/usr/local/bin:/usr/bin:/bin:/usr/local/games:/usr/games:$PATH
cd /home/tw1/twd1-kotlin
echo "Fetching, resetting and cleaning repository"
git fetch
git reset --hard HEAD
git clean -df
rm -f /home/tw1/twd1-kotlin/*.pdf
git rebase
rm -f build.main.jar
echo "Compiling script into jar"
/bin/bash compile-script.sh
echo "Running script as a jar"
kotlin build.main.kts
echo "Examples updated"
xdg-open "jetbrains://idea/navigate/reference?project=twd1-kotlin&path=twd1-kotlin.adoc:52"
read -n 1 -s