#!/bin/bash

CURDIR="$(pwd)"
FULLPATH="$CURDIR/src/main/java/edu/depauw/declan/main"
RELPATH="edu/depauw/declan/main"
LIBDIR="$CURDIR/libs"
TYPE=$1

function SLASH_TO_DOTS
{
    echo $1 | sed 's:/:.:'
}

function CLEAN_SRC
{
    echo "Cleaning Src..."
    find . -type f -name '*.class' -delete
    find . -type f -name '*~' -delete
    find . -type f -name '*#' -delete
    echo "Src Cleaned..."
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
    local tf=$(find . -name '*.class')
    if [[ "$tf" != "" ]]; then
	echo "Already Built Skipping to Run Step..."
    else
	echo "Building SRC..."
	echo "______________________BUILD_LOG___________________________"
	echo ""
	echo ""
	cd "$RELPATH"
	errors=""
	if javac -cp \* ./*.java ../common/*.java ../test/*.java ../common/ast/*.java; then
	    errors="PASS"
	fi
	echo ""
	echo ""
	echo "__________________________________________________________"
	echo ""
	cd ../../../../
	if [[ "$errors" == "PASS" ]]; then
	    echo "SRC Built Succesfully..."
	else
	    echo "SRC did not Build succesfully..."
	    CLEAN_SRC
	    RM_LIBS
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
       echo "___________________PROJECT1_LOG___________________________"
       echo ""
       echo ""
       java -cp "$RELPATH/*:." $LOCPATH.Project1
       echo ""
       echo ""
       echo "__________________________________________________________"
       echo ""
       echo "Project 1 complete..."
    fi
    if [ "$TYPE" == "PROJECT2" ] || [ "$TYPE" == "ALL" ]; then
       echo "Running Project2..."
       echo "___________________PROJECT2_LOG___________________________"
       echo ""
       echo ""
       java -cp "$RELPATH/*:." $LOCPATH.Project2
       echo ""
       echo ""
       echo "__________________________________________________________"
       echo ""
       echo "Project 2 complete..."
    fi
    if [ "$TYPE" == "PROJECT3" ] || [ "$TYPE" == "ALL" ]; then
       echo "Running Project3..."
       echo "___________________PROJECT3_LOG___________________________"
       echo ""
       echo ""
       java -cp "$RELPATH/*:." $LOCPATH.Project3
       echo ""
       echo ""
       echo "__________________________________________________________"
       echo ""
       echo "Project 3 complete..."
    fi
    if [ "$TYPE" == "TEST" ] || [ "$TYPE" == "ALL" ]; then
       echo "Running Test Cases..."
       echo "_______________________TEST_LOG___________________________"
       echo ""
       echo ""
       java -jar $RELPATH/junit-platform-console-standalone-1.7.0.jar -cp "$RELPATH/DeCLanModel-3x.jar:." --scan-class-path
       echo ""
       echo ""
       echo "__________________________________________________________"
       echo ""
       echo "Test cases complete..."
    fi
}



if [[ $# -ne 1 ]]; then
   echo 'You must specify only one argument and the argument can only be "TEST","PROJECT1", "PROJECT2", "PROJECT3", "ALL", "BUILD", or "CLEAN"...'
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
elif [ "$TYPE" == "TEST" ] || [ "$1" == "PROJECT1" ] || [ "$1" == "PROJECT2" ] || [ "$1" == "ALL" ]; then
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
    echo 'You must specify only one argument and the argument can only be "TEST", "PROJECT1", "PROJECT2", "PROJECT3", "ALL", "BUILD" or "CLEAN"...'
    exit 1
fi
