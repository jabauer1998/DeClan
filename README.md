# CSC426 Public Repository ~ ***IMPORTANT UPDATE*** ~ PLEASE READ 
I added a build script to the code to make compilation easier for future projects on the command line/shell. This is a bourne again shell script. How this script works is you provide it an argument and it will do the required task.<br><br>

**There are 5 tasks available:**<br><br>

*CLEAN => cleans up all of the .class files in the directory* <br>
*BUILD => builds the src in the directory but doesnt run anything*<br>
*PROJECT1 => builds and runs project1*<br>
*TEST => builds and runs the junit testcases*<br>
*ALL => buids and runs the project file aswell as the junit testcases*<br><br>

*EX command: =>* `bash BuildScript.sh CLEAN`<br>
**(NOTE: this command was executed inside the DeClan directory as this is where the build script lies)**<br><br>

The advantages of the build script is it will check if you have all of the required dependencies as well. For instance java installed on your machine. It also displayes the results in a very readable format.<br><br>

Starter projects for the Fall 2020 Compilers class at DePauw University
