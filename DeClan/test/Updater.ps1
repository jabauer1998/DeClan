function compile_ir{
    param($src)
    Write-Host "Compiling Ir with src-\n$src\n"
    $out = $src.replace(".dcl", ".ir").replace("test/declan", "test/ir/linked")
    Write-Host "to output-\n$out\n"
    mvn exec:java -f "$PSScriptRoot/../pom.xml" -P ir -Dout="$out" -Dsrc="$src"
}

function compile_assembly{
    param($src)
    $out = $src.replace(".dcl", ".a").replace("test/declan", "test/assembly")
    mvn exec:java -P assembly -DOut="$out" -DSrc="$src"
}

function compile_binary{
    param($src)
    $out = $src.replace(".dcl", ".bin").replace("test/declan", "test/binary")
    mvn exec:java -P binary -DOut="$out" -DSrc="$src"
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
    compile_ir -src "$PSScriptRoot/declan/conversions.dcl"
    compile_ir -src "$PSScriptRoot/declan/expressions.dcl"
    compile_ir -src "$PSScriptRoot/declan/ForLoopAdvanced.dcl"
    compile_ir -src "$PSScriptRoot/declan/ForLoopBasic.dcl"
    compile_ir -src "$PSScriptRoot/declan/ForLoopBasic2.dcl"
    compile_ir -src "$PSScriptRoot/declan/ForLoopBasic3.dcl"
    compile_ir -src "$PSScriptRoot/declan/IfStatementAdvanced.dcl"
    compile_ir -src "$PSScriptRoot/declan/IntegerDiv.dcl"
    compile_ir -src "$PSScriptRoot/declan/IntegerDiv2.dcl"
    compile_ir -src "$PSScriptRoot/declan/loops.dcl"
    compile_ir -src "$PSScriptRoot/declan/RealAddition.dcl"
    compile_ir -src "$PSScriptRoot/declan/RealAddition2.dcl"
    compile_ir -src "$PSScriptRoot/declan/RealAddition3.dcl"
    compile_ir -src "$PSScriptRoot/declan/RealDivision.dcl"
    compile_ir -src "$PSScriptRoot/declan/RealDivision2.dcl"
    compile_ir -src "$PSScriptRoot/declan/RealMultiplication.dcl"
    compile_ir -src "$PSScriptRoot/declan/RealMultiplication2.dcl"
    compile_ir -src "$PSScriptRoot/declan/RepeatLoopBasic.dcl"
    compile_ir -src "$PSScriptRoot/declan/sample.dcl"
    compile_ir -src "$PSScriptRoot/declan/SingleConversion.dcl"
    compile_ir -src "$PSScriptRoot/declan/test.dcl"
    compile_ir -src "$PSScriptRoot/declan/test2.dcl"
    compile_ir -src "$PSScriptRoot/declan/test3.dcl"
    compile_ir -src "$PSScriptRoot/declan/test4.dcl"
    compile_ir -src "$PSScriptRoot/declan/WhileLoopAdvanced.dcl"
    compile_ir -src "$PSScriptRoot/declan/WhileLoopBasic.dcl"
} elseif ($depends -eq -1) {
    Write-Error "Check dependencies returned -1"
}