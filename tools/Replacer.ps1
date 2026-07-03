$rootArg = $args[0]
$fromArg = $args[1]
$toArg = $args[2]

Get-ChildItem -Path $rootArg -File -Recurse | ForEach-Object {
    $filePath = $_.FullName
    (Get-Content -Path $filePath -Raw) -replace "$fromArg", "$toArg" | Set-Content -Path $filePath
}

