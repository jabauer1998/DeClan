function clean_src{
    param($directory)
    Get-ChildItem -Directory "$directory" |
    Foreach-Object {
        clean_src -directory $_.FullName
    }

    Get-ChildItem -File "$directory" |
    ForEach-Object {
        Write-Host "Cleaning file at path -"
        Write-Host "$_.FullName"
        Remove-Item $_.FullName
    }
}
function compile_file_into_ir{
    param($src)
    Write-Host "Compiling Ir with src-"
    Write-Host "$src"
    $out = "$src".replace(".dcl", ".ir").replace("test\declan", "test\ir\linked")
    Write-Host "to output-"
    Write-Host "$out"
    Write-Host "---------Output-Window-----------"
    Invoke-Expression "mvn exec:java -q -f '$PSScriptRoot/../pom.xml' -P ir -Dout='$out' -Dsrc='$src'" -ErrorVariable $errorOutput
    Write-Host "--------End-Output-Window--------"
    if ($errorOutput -eq '') {
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
    Invoke-Expression "mvn exec:java -q -f '$PSScriptRoot/../pom.xml' -P assembly -e -Dout='$out' -Dsrc='$src'" -ErrorVariable $errorOutput
    Write-Host "--------End-Output-Window--------"
    if ($errorOutput -eq '') {
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
    Invoke-Expression "mvn exec:java -f '$PSScriptRoot/../pom.xml' -q -e -P binary -Dout='$out' -Dsrc='$src'" -ErrorVariable $errorOutput
    Write-Host "--------End-Output-Window--------"
    if ($errorOutput -eq '') {
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
    if (!(Get-Command mvn)){
        Write-Error "Mvn command does not exist!!!"
        return -1
    } else {
        Write-Host "Mvn command was found successfully!!!"
        return 0
    }

    if(!(Get-Command java)){
        Write-Error "Java command does not exist!!!"
        return -1
    } else {
        Write-Host "Java command was found successfully!!!"
        return 0
    }
}

$depends = check_dependencies
if ($depends -eq 0) {
    clean_src -directory "$PSScriptRoot/assembly"
    clean_src -directory "$PSScriptRoot/binary"
    clean_src -directory "$PSScriptRoot/ir"

    Get-ChildItem -File "$PSScriptRoot/declan" |
    Foreach-Object {
        compile_file_into_ir -src $_.FullName
        compile_file_into_assembly -src $_.FullName
        compile_file_into_binary -src $_.FullName
    }
} elseif ($depends -eq -1) {
    Write-Error "Dependency Check Failed!!!"
}