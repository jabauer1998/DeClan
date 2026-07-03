Write-Host "Collecting Port Information..."
$port = $args[0]
Write-Host "Attaching Debugger..."
jdb -sourcepath ./src/java -connect com.sun.jdi.SocketAttach:port=$port
Write-Host "Closing up debugger..."
