$CLASSPATH = "tmp;lib\*"

function clean_src{
    param($directory)
    Get-ChildItem -Directory "$directory" |
    Foreach-Object {
        clean_src -directory $_.FullName
    }

    Get-ChildItem -File "$directory" |
    ForEach-Object {
        Write-Host "Cleaning file at path -"
        Write-Host $_.FullName
        Remove-Item $_.FullName
    }
}
function compile_file_into_ir{
    param($src, $nolink, $optimized)
    Write-Host "Compiling Ir with src-"
    Write-Host "$src"
    if ($nolink){
        $out = "$src".replace(".declib", ".ilib").replace(".dcl", ".ir").replace("standard_library\declan", "standard_library\ir\linkable").replace("test\declan", "test\ir\linkable")
    } elseif ($optimized){
                $out = "$src".replace(".dcl", ".ir").replace("test\declan", "test\ir\optimized")
        } else {
        $out = "$src".replace(".dcl", ".ir").replace("test\declan", "test\ir\linked")
    }
    Write-Host "to output-"
    Write-Host "$out"
    Write-Host "---------Output-Window-----------"
    if($out.Contains(".ir")){
        if (($nolink -eq '') -and ($optimized -eq '')){
            Invoke-Expression "java -cp '$CLASSPATH' declan.driver.MyCompilerDriver -e -p '$src' -f '$out' -std" -ErrorVariable $errorOutput
        } elseif ($nolink -eq ''){
            Invoke-Expression "java -cp '$CLASSPATH' declan.driver.MyCompilerDriver -e -o -p '$src' -f '$out' -std" -ErrorVariable $errorOutput
        } else {
            Invoke-Expression "java -cp '$CLASSPATH' declan.driver.MyCompilerDriver -e -n -p '$src' -f '$out'" -ErrorVariable $errorOutput
        }
    } elseif ($out.Contains(".ilib")){
        Invoke-Expression "java -cp '$CLASSPATH' declan.driver.MyCompilerDriver -e -n -l '$src' -f '$out'" -ErrorVariable $errorOutput
    }
    
    Write-Host "--------End-Output-Window--------"
    if ([string]::IsNullOrWhiteSpace($errorOutput)) {
        Write-Host "Source-"
        Write-Host "$src"
        Write-Host "Compiled to-"
        Write-Host "$out"
        Write-Host "successfully!!!"
    } else {
        Write-Host "Errors detected compilation aborted for ir at -"
        Write-Host "$out"
    }
}

function compile_file_into_assembly{
    param($src)
    Write-Host "Compiling Declan in src-"
    Write-Host "$src"
    $out = "$src".replace(".dcl", ".a").replace("test\declan", "test\assembly")
    Write-Host "to output assembly at-"
    Write-Host "$out"
    Write-Host "---------Output-Window-----------"
    Invoke-Expression "java -cp '$CLASSPATH' declan.driver.MyCompilerDriver -a -p '$src' -f '$out' -std" -ErrorVariable $errorOutput
    Write-Host "--------End-Output-Window--------"
    if ([string]::IsNullOrWhiteSpace($errorOutput)) {
        Write-Host "Source-"
        Write-Host "$src"
        Write-Host "Compiled to-"
        Write-Host "$out"
        Write-Host "successfully!!!"
    } else {
        Write-Host "Errors detected compilation aborted for assembly at -"
        Write-Host "$out"
    }
}

function compile_file_into_binary{
    param($src)
    Write-Host "Compiling Declan in src-"
    Write-Host "$src"
    $out = "$src".replace(".dcl", ".bin").replace("test\declan", "test\binary")
    Write-Host "to output binary at-"
    Write-Host "$out"
    Write-Host "---------Output-Window-----------"
    Invoke-Expression "java -cp '$CLASSPATH' declan.driver.MyCompilerDriver -p '$src' -f '$out' -std" -ErrorVariable $errorOutput
    Write-Host "--------End-Output-Window--------"
    if ([string]::IsNullOrWhiteSpace($errorOutput)) {
        Write-Host "Source-"
        Write-Host "$src"
        Write-Host "Compiled to-"
        Write-Host "$out"
        Write-Host "successfully!!!"
    } else {
        Write-Host "Errors detected compilation aborted for binary at -"
        Write-Host "$out"
    }
}

function check_dependencies(){
    if(!(Get-Command java)){
        Write-Error "Java command does not exist!!!"
        return -1
    } else {
        Write-Host "Java command was found successfully!!!"
    }

    if(!(Test-Path "tmp")){
        Write-Error "Compiled classes not found in tmp directory. Run build first!!!"
        return -1
    } else {
        Write-Host "Compiled classes found in tmp directory!!!"
        return 0
    }
}

function list_contains(){
    param($list, $elem)
    $to_ret = $list -contains "$elem"
    if($to_ret -eq ''){
        $to_ret = $false
    }
    return $to_ret
}

function parse_commands(){
    param($arguments)
    $return_set = @{}
    $containsHelp = list_contains -list $arguments -elem "help"
    $return_set.Add("help", $containsHelp)
    $containsIr = list_contains -list $arguments -elem "ir"
    $return_set.Add("ir", $containsIr)
    $containsAssembly = list_contains -list $arguments -elem "assembly"
    $return_set.Add("assembly", $containsAssembly)
    $containsBinary = list_contains -list $arguments -elem "binary"
    $return_set.Add("binary", $containsBinary)
    $containsAll = list_contains -list $arguments -elem "all"
    $return_set.Add("all", $containsAll)
    $containsClean = list_contains -list $arguments -elem "clean"
    $return_set.Add("clean", $containsClean)
    $containsNoLink = list_contains -list $arguments -elem "no_link"
    $return_set.Add("no_link", $containsNoLink)
    $containsOpt = list_contains -list $arguments -elem "opt"
    $return_set.Add("opt", $containsOpt)
    $containsStdLib = list_contains -list $arguments -elem "std"
    $return_set.Add("std", $containsStdLib)
    return $return_set
}

$commands = parse_commands -arguments $args

if($commands["help"] -eq $true){
    Write-Host "The following is the help message of the Updater.ps1 script."
    Write-Host "The updater script was designed to update the test output files located inside the test directory."
    Write-Host "This script should only be ran after there is a change to the DeClan project that causes one or more of the unit tests to fail"
    Write-Host "and it is known that the change is 100% correct."
    Write-Host "The default way to run this script is with no command line arguments."
    Write-Host "The default behavior is to clean or delete all the previous ir, assembly, and binary"
    Write-Host "then compile all the new ir modules, assembly files, and binary outputs of the files located in test/declan"
    Write-Host "Optionally the Updater script can be ran with input commands that tell the Updater script what to accomplish"
    Write-Host "Here is a summary of the commands-"
    Write-host "1) help - displays the current message"
    Write-Host "2) clean - cleans up all the file source located in the test directory while ignoring the src in test/declan and the current Updater.ps1 script"
    Write-Host "3) ir - compiles the source in test/declan into Ir modules. If supplied with the no_link command it will generate ir modules that are not_linked to anything."
    Write-Host "4) assembly - compiles the source in test/declan into assembly files that are located in test/assembly"
    Write-Host "5) binary - compiles the src located in test/declan into a binary executable located in test/binary"
    Write-Host "6) no_link - designed to be supplied with the ir command the no_link command generates ir modules that are not linked"
    Write-Host "7) opt - optimize the ir code with all available passes"
    Write-Host "8) all - Runs all the commands above. Same as suplying the script with no command line arguments."
} else {
    $noArgs = $args.Count -eq 0
    if(($noArgs -eq $true) -or ($commands["all"] -eq $true) -or ($commands["clean"] -eq $true)){
        clean_src -directory "$PSScriptRoot/assembly"
        clean_src -directory "$PSScriptRoot/binary"
        clean_src -directory "$PSScriptRoot/ir"
        clean_src -directory "$PSScriptRoot/temp"
    }

    $depends = check_dependencies
    if ($depends -eq 0) {
        if($noArgs -or $commands["all"] -or $commands["std"]){
            Get-ChildItem -File "$PSScriptRoot/standard_library/declan" |
            Foreach-Object {
                compile_file_into_ir -src $_.FullName -nolink $true -optimized $false
            }
        }
        Get-ChildItem -File "$PSScriptRoot/test/declan" |
        Foreach-Object {
            if($noArgs -or $commands["all"] -or ($commands["ir"] -and $commands["no_link"])){
                compile_file_into_ir -src $_.FullName -nolink $true -optimized $false
            }
            if($noArgs -or $commands["all"] -or ($commands["ir"] -and $commands["opt"])){
                compile_file_into_ir -src $_.FullName -optimized $true -nolink $false
            }
            if($noArgs -or $commands["all"] -or ($commands["ir"] -and (-not ($commands["no_link"] -or $commands["opt"])))){
                compile_file_into_ir -src $_.FullName -nolink $false -optimized $false
            }
            if($noArgs -or $commands["all"] -or $commands["assembly"]){
                compile_file_into_assembly -src $_.FullName
            }
            if($noArgs -or $commands["all"] -or $commands["binary"]){
                compile_file_into_binary -src $_.FullName
            }
        }
    } elseif ($depends -eq -1) {
        Write-Error "Dependency Check Failed!!!"
    }
}
