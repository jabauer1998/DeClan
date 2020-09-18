CURDIR="$(pwd)"
FULLPATH=$CURDIR/edu/depauw/declan
RELPATH=./edu/depauw/declan

TYPE=$1

function SLASH_TO_DOTS
{
    echo $1 | sed 's:/:.:'
}

function CLEAN_SRC
{
    rm -rf *~ *.class *#
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
       echo "SRC Found"
    else
	echo "SRC not found make sure you have Java jdk installed and the necissary environment variables set up on your machine..."
	exit 1
    fi	    
}

function BUILD_SRC
{
    if [ -a "$SRCPATH/Project1.class" ] && [ -a "$SRCPATH/Test.class" ]; then
	echo "Already Compiled Skipping to Run Step..."
    else
	echo "Compiling SRC..."
	cd edu/depauw/declan/
	local ERRORS=$(javac -cp DeCLanModel-1x.jar:. ./*.java ./common/*.java | grep "errors")2> /dev/null
	cd ../../../
	if [[ "$ERRORS" == "" ]]; then
	    echo "SRC compiled succesfully..."
	else
	    echo "SRC did not Build succesfully..."
	    echo "Exiting Program..."
	    exit 1
	fi
	
    fi
}

function RUN_SRC
{
    if [[ "$TYPE" == "PROGRAM1" ]]; then
       echo "Running Program1..."
       echo "__________________________________________________________"
       echo ""
       echo ""
       local LOCPATH="$(SLASH_TO_DOTS RELPATH)"
       java -cp $RELPATH/DeCLanModel-1x.jar:. $LOCPATH.Program1
       echo ""
       echo ""
       echo "__________________________________________________________"
       echo "Program 1 complete..."
    elif [[ "$TYPE" == "TEST" ]]; then
       echo "Running Test Cases..."
       echo "__________________________________________________________"
       echo ""
       echo ""
       echo "Test commadn should be placed here..."
       echo ""
       echo ""
       echo "__________________________________________________________"
       echo "Test cases complete..."
    fi
}



if [[ $# -ne 1 ]]; then
   echo 'You must specify only one argument and the argument can only be "TEST","PROGRAM1", or "CLEAN"...'
   exit 1
fi

if [[ $1 == "CLEAN" ]]; then
    CLEAN_SRC
    exit 1
fi

echo "Entering directory..."
cd ./src/main/java
   
SRC_CHECK
BUILD_SRC
RUN_SRC

echo "Leaving Directory..."
cd ../../../
