#!/bin/bash

location=$(pwd)

echo "Checking if Java Exists..."
javaExists=$(java -version 2>&1 | head -1)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

if [ -n "$javaExists" ]; then
    echo "Java Found..."
    echo "Searching for Command..."
    if [ $# -eq 0 ]; then
        echo "No command found, expected 1 argument..."
        echo "Example of command can be 'build', 'test', 'publish' or 'clean'"
    else
        command=$1
        echo "Command is $command"
        if [ "$command" = "build" ]; then
            srcRoot=$(pwd)
            echo "Generating ANTLR sources..."
            ANTLR_OUT="src/java/declan/backend/assembler"
            for g4 in src/antlr/*.g4; do
                if [ -f "$g4" ]; then
                    g4name=$(basename "$g4")
                    cp "$g4" "$ANTLR_OUT/$g4name"
                    java -jar lib/antlr-4.13.2-complete.jar "$ANTLR_OUT/$g4name" -package declan.backend.assembler -visitor -listener
                    rm -f "$ANTLR_OUT/$g4name"
                    rm -f "$ANTLR_OUT"/*.interp "$ANTLR_OUT"/*.tokens
                fi
            done
            if [ -f build/BuildList.txt ]; then
                rm -f build/BuildList.txt
            fi
            touch build/BuildList.txt
            find "$srcRoot/src" -type f -name "*.java" ! -name "#*" ! -name "*~" > build/BuildList.txt
            for line in $(cat build/BuildList.txt); do
                if [ -f "$line" ]; then
                    sed -i '1s/^\xEF\xBB\xBF//' "$line"
                fi
            done
            cat "build/BuildList.txt"
            mkdir -p tmp
            mkdir -p bin
            javac "@build/BuildList.txt" -d "./tmp" -sourcepath "./src" -cp "./lib/antlr-4.13.2-complete.jar" -encoding "UTF-8"
            cd tmp && jar xf "../lib/antlr-4.13.2-complete.jar" && cd ..
            if [ -f "./tmp/META-INF/MANIFEST.MF" ]; then
                rm -f "./tmp/META-INF/MANIFEST.MF"
            fi
            jar cf "./bin/Declan.jar" -C "./tmp" "."
            rm -rf ./tmp/*
        elif [ "$command" = "test" ]; then
            echo "Building main sources first..."
            bash "$SCRIPT_DIR/LinuxBuild.sh" build
            srcRoot=$(pwd)
            if [ -f build/TestBuildList.txt ]; then
                rm -f build/TestBuildList.txt
            fi
            touch build/TestBuildList.txt
            find "$srcRoot/test/java" -type f -name "*.java" ! -name "#*" ! -name "*~" > build/TestBuildList.txt
            for line in $(cat build/TestBuildList.txt); do
                if [ -f "$line" ]; then
                    sed -i '1s/^\xEF\xBB\xBF//' "$line"
                fi
            done
            cat "build/TestBuildList.txt"
            CLASSPATH="./bin/*:./lib/junit-platform-console-standalone-6.0.3.jar"
            mkdir -p tmp
            javac "@build/TestBuildList.txt" -sourcepath "./test/java" -classpath "$CLASSPATH" -d "tmp" -encoding "UTF-8"
            rm -f build/TestBuildList.txt
            java -jar lib/junit-platform-console-standalone-6.0.3.jar execute --classpath "./bin/Declan.jar:./lib/junit-platform-console-standalone-6.0.3.jar:./tmp" --scan-classpath
            rm -rf ./tmp/*
        elif [ "$command" = "clean" ]; then
            find ./src -name "*.class" -type f -delete
            rm -rf bin/*
            rm -rf tmp/*
        elif [ "$command" = "publish" ]; then
            :
        else
            echo "Unknown command '$command'"
        fi
    fi
fi

cd "$location"
