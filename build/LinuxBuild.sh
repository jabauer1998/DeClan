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
            CLASSPATH=""
            for jar in lib/*.jar; do
                if [ -f "$jar" ]; then
                    if [ -n "$CLASSPATH" ]; then
                        CLASSPATH="$CLASSPATH:"
                    fi
                    CLASSPATH="$CLASSPATH$jar"
                fi
            done
            mkdir -p tmp
            if [ -n "$CLASSPATH" ]; then
                javac "@build/BuildList.txt" -sourcepath "./src" -classpath "$CLASSPATH" -d "tmp" -encoding "UTF-8"
            else
                javac "@build/BuildList.txt" -sourcepath "./src" -d "tmp" -encoding "UTF-8"
            fi
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
            CLASSPATH="tmp"
            for jar in lib/*.jar; do
                if [ -f "$jar" ]; then
                    CLASSPATH="$CLASSPATH:$jar"
                fi
            done
            javac "@build/TestBuildList.txt" -sourcepath "./test/java" -classpath "$CLASSPATH" -d "tmp" -encoding "UTF-8"
            rm -f build/TestBuildList.txt
            RUNPATH="tmp"
            for jar in lib/*.jar; do
                if [ -f "$jar" ]; then
                    RUNPATH="$RUNPATH:$jar"
                fi
            done
            java -jar lib/junit-platform-console-standalone-6.0.3.jar execute --classpath "$RUNPATH" --scan-classpath tmp
        elif [ "$command" = "clean" ]; then
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
