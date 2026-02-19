$location = Get-Location

Write-Host "Checking if Java Exists..."
$javaExists = (Get-Command java | Select-Object -ExpandProperty Version).tostring()

$runPath = $PSScriptRoot
cd $runPath/..

if($javaExists -ne ""){
    Write-Host "Java Found..."
    Write-Host "Searching for Command..."
    if ($args.Length -eq 0){
        Write-Host "No command found, expected 1 argument..."
        Write-Host "Example of command can be 'build', 'test', 'publish' or 'clean'"
    } else {
        $command = $args[0]
        Write-Host "Command is $command"
        if($command -eq "build"){
            $srcRoot = Get-Location
            Write-Host "Generating ANTLR sources..."
            $antlrOut = "src\java\declan\backend\assembler"
            $g4Files = Get-ChildItem -Path "src\antlr" -Filter *.g4 -File
            foreach ($g4 in $g4Files) {
                Copy-Item $g4.FullName -Destination "$antlrOut\$($g4.Name)"
                java -jar lib\antlr-4.13.2-complete.jar "$antlrOut\$($g4.Name)" -package declan.backend.assembler -visitor -listener
                Remove-Item -Force "$antlrOut\$($g4.Name)"
                Remove-Item -Force "$antlrOut\*.interp", "$antlrOut\*.tokens" -ErrorAction SilentlyContinue
            }
            if(Test-Path -Path build\BuildList.txt){
                Remove-Item -Force build\BuildList.txt
            }
            New-Item -Path "build\BuildList.txt" -ItemType File
            Get-ChildItem -Path "$srcRoot/src/java" -Recurse -File -Filter *.java | Where-Object { $_.Name -notlike "#*" -and $_.Name -notlike "*~" } | Select-Object -ExpandProperty FullName > build\BuildList.txt
            foreach ($line in Get-Content -Path 'build\BuildList.txt') {
                $content = Get-Content -Path $line -Raw
    
                # Write the content back without BOM using .NET method
                $Utf8NoBomEncoding = New-Object System.Text.UTF8Encoding $False
                [System.IO.File]::WriteAllText($line, $content, $Utf8NoBomEncoding)
            }
            cat "build/BuildList.txt"
            javac "@build/BuildList.txt" -d "./tmp" -sourcepath "./src" -cp "./lib/*" -encoding "UTF-8"
            $dependencyJars = Get-ChildItem -Path "./lib" -Filter *.jar
            Write-Host $dependencyJars
            foreach ($jar in $dependencyJars) {
                # Extract contents of each dependency JAR into the temp directory
                jar xf $jar.FullName -C "./tmp"
                if (Test-Path ".\tmp\META-INF\MANIFEST.MF") {
                    Remove-Item ".\tmp\META-INF\MANIFEST.MF"
                }
            }
            jar cf "./bin/Declan.jar" -C "./tmp" "."
            Remove-Item -Path ./tmp/* -Recurse -Force
        } elseif ($command -eq "clean"){
            Get-ChildItem -Path './src' -Include *.class -Recurse | Remove-Item -Force
            Get-ChildItem -Path './bin' -Include * -Recurse | Remove-Item -Force
            Get-ChildItem -Path './tmp' -Include * -Recurse | Remove-Item -Force
            Remove-Item -Recurse -Force "*~"
            Remove-Item -Recurse -Force "*#" 
        } elseif ($command -eq "test") {
            Write-Host "Building main sources first..."
            & "$runPath\WindowsBuild.ps1" build
            $srcRoot = Get-Location
            if(Test-Path -Path build\TestBuildList.txt){
                Remove-Item -Force build\TestBuildList.txt
            }
            New-Item -Path "build\TestBuildList.txt" -ItemType File
            Get-ChildItem -Path "$srcRoot/test/java" -Recurse -File -Filter *.java | Where-Object { $_.Name -notlike "#*" -and $_.Name -notlike "*~" } | Select-Object -ExpandProperty FullName > build\TestBuildList.txt
            foreach ($line in Get-Content -Path 'build\TestBuildList.txt') {
                $content = Get-Content -Path $line -Raw
                $Utf8NoBomEncoding = New-Object System.Text.UTF8Encoding $False
                [System.IO.File]::WriteAllText($line, $content, $Utf8NoBomEncoding)
            }
            cat "build/TestBuildList.txt"
            javac "@build/TestBuildList.txt" -sourcepath "./test/java" -classpath "./bin/*" -d "tmp" -encoding "UTF-8"
            Remove-Item -Force build\TestBuildList.txt
            java -jar lib/junit-platform-console-standalone-6.0.3.jar execute --classpath "./bin/Declan.jar;./tmp" --scan-classpath
	    Remove-Item -Force -Recurse -Path "./tmp/*"
        } else {
            Write-Host "Unknown command '$command'"
        }
    }
}

# At end of script go back to current location
cd $location

