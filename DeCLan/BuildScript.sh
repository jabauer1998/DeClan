#!/bin/bash

CURDIR="$(pwd)"
FULLPATH="$CURDIR/src/main/java/edu/depauw/declan"
RELPATH="edu/depauw/declan"
LIBDIR="$CURDIR/libs"
TYPE=$1

function SLASH_TO_DOTS
{
    echo $1 | sed 's:/:.:'
}

function CLEAN_SRC
{
    rm -f "$FULLPATH/"*~ "$FULLPATH/"*.class "$FULLPATH/"*# "$FULLPATH/common/"*~ "$FULLPATH/common/"*.class "$FULLPATH/common/"*#
}

function SRC_CHECK
{
    local tf="F"
    if [[ $(java --version | grep "openjdk") != "" ]]; then
       tf="T"
    elif [[ $(java --version | grep "oracle") != "" ]]; then
       tf="T"    
    fi

    if [[ "$tf" == "T" ]]; then
       echo "SRC Found..."
    else
	echo "SRC not found make sure you have Java jdk installed and the necissary environment variables set up on your machine..."
	exit 1
    fi	    
}

function COPY_LIBS
{
    echo "Copying over libraries/jar files..."
    cp -r "$LIBDIR/"*.jar "$FULLPATH"
    echo "Coppied over libraries..."
}

function RM_LIBS
{
    echo "Removing Libraries..."
    rm -f "$FULLPATH/"*.jar
    echo "Removed Libraries..."
}

function BUILD_SRC
{
    local tf=$(ls "$FULLPATH"/*.class)
    if [[ "$tf" != "" ]]; then
	echo "Already Built Skipping to Run Step..."
    else
	echo "Building SRC..."
	echo "______________________BUILD_LOG___________________________"
	echo ""
	echo ""
	cd "$RELPATH"
	local ERRORS=$(javac -cp \* ./*.java ./common/*.java | grep "errors")
	echo ""
	echo ""
	echo "__________________________________________________________"
	echo ""
	cd ../../../
	if [[ "$ERRORS" == "" ]]; then
	    echo "SRC Built Succesfully..."
	else
	    echo "SRC did not Build succesfully..."
	    echo "Exiting Program..."
	    exit 1
	fi
    fi
}

function RUN_SRC
{
    local LOCPATH="$(SLASH_TO_DOTS $RELPATH)"
    if [ "$TYPE" == "PROJECT1" ] || [ "$TYPE" == "ALL" ]; then
       echo "Running Program1..."
       echo "________________________RUN_LOG___________________________"
       echo ""
       echo ""
       java -cp "$RELPATH/*:." $LOCPATH.Project1
       echo ""
       echo ""
       echo "__________________________________________________________"
       echo ""
       echo "Project 1 complete..."
    fi
    if [ "$TYPE" == "TEST" ] || [ "$TYPE" == "ALL" ]; then
       echo "Running Test Cases..."
       echo "_______________________TEST_LOG___________________________"
       echo ""
       echo ""
       java -cp "$RELPATH/*:." -jar $RELPATH/junit-platform-console-standalone-1.7.0.jar -cp "$RELPATH/*:." --scan-class-path
       echo ""
       echo ""
       echo "__________________________________________________________"
       echo ""
       echo "Test cases complete..."
    fi
}



if [[ $# -ne 1 ]]; then
   echo 'You must specify only one argument and the argument can only be "TEST","PROJECT1", "ALL", "BUILD", or "CLEAN"...'
   exit 1
fi



if [[ "$TYPE" == "CLEAN" ]]; then
    CLEAN_SRC
    exit 1
elif [[ "$TYPE" == "BUILD" ]]; then
    echo "Entering directory..."
    cd ./src/main/java
    SRC_CHECK
    COPY_LIBS
    BUILD_SRC
    RM_LIBS
    echo "Leaving Directory..."
    cd ../../../
elif [ "$TYPE" == "TEST" ] || [ "$1" == "PROJECT1" ] || [ "$1" == "ALL" ]; then
    echo "Entering directory..."
    cd ./src/main/java
    SRC_CHECK
    COPY_LIBS
    BUILD_SRC
    RUN_SRC
    RM_LIBS
    echo "Leaving Directory..."
    cd ../../../
else
    echo "Invalid argument: $1"
    echo 'You must specify only one argument and the argument can only be "TEST","PROJECT1", "ALL",  "BUILD" or "CLEAN"...'
    exit 1
fi
