# Clean up the bin folder
Remove-Item -Path ".\bin" -Recurse -Force

# Compile all java files
javac -d bin *.java

cd bin
Start-Process java -ArgumentList "Main"
cd ..