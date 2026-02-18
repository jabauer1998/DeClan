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
        echo "Example of command can be 'build', 'publish' or 'clean'"
    else
        command=$1
        echo "Command is $command"
        if [ "$command" = "build" ]; then
            srcRoot=$(pwd)
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
